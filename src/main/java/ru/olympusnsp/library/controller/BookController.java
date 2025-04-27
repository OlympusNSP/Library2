package ru.olympusnsp.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.olympusnsp.library.dto.BookSaving;
import ru.olympusnsp.library.dto.BooksReturn;
import ru.olympusnsp.library.exeption.SearchStringTooSmall;
import ru.olympusnsp.library.model.Book;
import ru.olympusnsp.library.service.BookService;

import java.util.List;
import java.util.Optional;

@RestController
@Tag(name = "Кники")
@RequestMapping("/book")
public class BookController {

    BookController(BookService bookService) {
        this.bookService = bookService;
    }
    private final BookService bookService;

    Logger logger = LoggerFactory.getLogger(BookController.class);


    @GetMapping("")
    @Operation(summary = "Постраничная выдача всех книг")
    public Page<Book> all(Pageable page){
        return bookService.findAll(page);
    }

    @PostMapping("")
    @Operation(summary = "Сохранение книги")
    public Book save(@RequestBody @Valid BookSaving book){
        logger.info("Save Book: {}", book);
        return bookService.save(book);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение книги по идентификатору")
    public Book findById(@PathVariable Integer id){
        logger.info("Get book with id");
        return bookService.findById(id);
    }
    @GetMapping("/search")
    @Operation(summary = "Получение страницы книг по совпадению с названием")
    public Page<Book> findByTitle(@Param("title") String title, Pageable page){
        if (title.length()<4){
            logger.warn("Search string too small");
            throw new SearchStringTooSmall("Title String too small");
        }
        return bookService.findAllByTitleContains(title, page);
    }

    @GetMapping("/genre/{id}")
    @Operation(summary = "Получение страницы книг по совпадению с жанром")
    public Page<Book> findByGenre(@PathVariable Integer id, Pageable page){
        return bookService.findAllWithGenreId(id,page);
    }


}
