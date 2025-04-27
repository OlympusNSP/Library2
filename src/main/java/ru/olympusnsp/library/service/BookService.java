package ru.olympusnsp.library.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.olympusnsp.library.dto.BookSaving;
import ru.olympusnsp.library.dto.BooksReturn;
import ru.olympusnsp.library.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookService {
    Page<Book> findAll(Pageable pageable);
    Page<Book> findAllByTitleContains(String title, Pageable pageable);
    Book findById(Integer id);
    Book save(BookSaving book);
    Book save(Book book);
    Boolean reserveBookById(Integer id);
    void deleteById(Integer id);
    void returnBook(BooksReturn booksReturn);
    Page<Book> findAllWithGenreId(Integer genreId,Pageable pagable);
}
