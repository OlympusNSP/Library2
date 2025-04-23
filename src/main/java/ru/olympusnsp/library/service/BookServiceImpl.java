package ru.olympusnsp.library.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.olympusnsp.library.dto.BookSaving;
import ru.olympusnsp.library.dto.BooksReturn;
import ru.olympusnsp.library.exeption.NotFoundEntity;
import ru.olympusnsp.library.model.*;
import ru.olympusnsp.library.repository.BookRepository;

import java.sql.Array;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class BookServiceImpl implements BookService {

    private final AuthorService authorService;
    private final GenreService genreService;
    private final UserService userService;

    @Value("${setting.max-day}")
    private Integer maxDaysInRent;

    @Value("${setting.max-violations}")
    private Integer maxViolations;

    public BookServiceImpl(BookRepository bookRepository, AuthorService authorService, GenreService genreService, UserService userService) {
        this.bookRepository = bookRepository;
        this.authorService = authorService;
        this.genreService = genreService;
        this.userService = userService;
    }

    private final BookRepository bookRepository;

    /**
     * ПОлучение всех книг
     *
     * @param pageable страница
     * @return страничная выдача книг
     */
    @Override
    public Page<Book> findAll(Pageable pageable) {
       return bookRepository.findAll(pageable);
    }

    /**
     * Поиск книги по названию, совпадение части строки
     *
     * @param title строка поиска
     * @param pageable страница
     * @return страница книг
     */
    @Override
    public Page<Book> findAllByTitleContains(String title, Pageable pageable) {
        return bookRepository.findAllByTitleContaining(title, pageable);
    }

    /**
     * Поиск книги по идентифкатору
     *
     * @param id идентификарток
     * @return  книг
     */
    @Override
    @Transactional
    public Book findById(Integer id) {
        return bookRepository.findById(id).orElseThrow(()-> new NotFoundEntity("Book with id " + id.toString() + " not found"));
    }

    /**
     * Сохрание книги, в виде DTO, для внешних сервисов
     *
     * @param bookDTO книги
     * @return возврат сохранненной книги
     */
    @Override
    @Transactional
    public Book save(BookSaving bookDTO) {
        var b = new Book();
        b.setReserve(0);
        b.setTitle(bookDTO.getTitle());
        b.setCount(bookDTO.getCount());
        b.setDescription(bookDTO.getDescription());
        b.setAvailable(bookDTO.getCount());
        b.setYear(bookDTO.getYear());
        var bookWithID =  bookRepository.save(b);
        var list = new ArrayList<Author>();
        for(Integer a : bookDTO.getAuthorsId()){
            var author = authorService.findById(a);
            list.add(author);
        }
        if (bookDTO.getGenresId()!=null)
        {
            var listGenre = new ArrayList<Genre>();
            for(Integer a : bookDTO.getAuthorsId()){
                var genre = genreService.findById(a);
                listGenre.add(genre);
            }
            b.setGenres(listGenre);
        }
        bookWithID.setAuthors(list);

        return bookRepository.save(bookWithID);
    }


    /**
     * Сохранение книги, без DTO, для обеспечение работы внутренних сервисов
     * @param book
     * @return
     */
    @Transactional
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    /**
     * Резервирование книги по id
     *
     * @param id идентификтор книги
     * @return boolean, true - если резерв успешен
     */
    @Transactional
    @Override
    public Boolean reserveBookById(Integer id) {
        Optional<Book> oBook = bookRepository.findById(id);
        if(oBook.isPresent() && oBook.get().getAvailable()>0){
            Book book = oBook.get();
            book.setAvailable(book.getAvailable()-1);
            book.setReserve(book.getReserve()+1);
            bookRepository.save(book);
            return true;
        }
        return false;
    }

    /**
     * Удаление книг по идентификатору
     * @param id идентификатор
     */
    public void deleteById(Integer id) {
        if (!bookRepository.existsById(id)) {
            throw new NotFoundEntity("Book with id " + id + " not found");
        }
        bookRepository.deleteById(id);
    }

    /**
     * Возврат книги
     *
     * @param booksReturn BookReturn DTO
     */
    public void returnBook(BooksReturn booksReturn){
        var oBook = bookRepository.findById(booksReturn.getBook_id());
        if (oBook.isPresent()){
            var book = oBook.get();
            book.setAvailable(book.getAvailable()+1);
            var user = userService.findById(booksReturn.getUser_id());
            var rentalBookd =  user.getRentalBooks();
            var newRentalSet = new HashSet<RentalBook>(rentalBookd);
            for (var i :rentalBookd){
                if (i.getUser().equals(user)){
                    if( ChronoUnit.DAYS.between(i.getDateRentedStart(), LocalDate.now())>maxDaysInRent){
                        user.setViolations(user.getViolations() + 1);
                        if (user.getViolations()>maxViolations){
                            user.setStatusBlock(true);
                        }
                    }
                    newRentalSet.remove(i);
                }
            }
            user.setRentalBooks(newRentalSet);
            userService.save(user);
        }
    }
}
