package ru.olympusnsp.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.olympusnsp.library.dto.BookSaving;
import ru.olympusnsp.library.model.Author;
import ru.olympusnsp.library.model.Book;
import ru.olympusnsp.library.service.BookService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private Book sampleBook;

    private List<Author> authors;
    @BeforeEach
    void setUp() {
        sampleBook = new Book();
        sampleBook.setId(2);
        sampleBook.setTitle("Test Book");
        sampleBook.setCount(3);
        sampleBook.setYear((short)2025);
        sampleBook.setDescription("Test Book Description");
        sampleBook.setReserve(1);
        sampleBook.setAvailable(1);

        authors = new ArrayList<>();
        authors.add(Author.builder().id(5).fullname("Author").build());
        authors.add(Author.builder().id(6).fullname("Author2").build());

    }

    @Test
    void testGetAllBooks() throws Exception {
        Page<Book> page = new PageImpl<>(List.of(sampleBook));
        Mockito.when(bookService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/book")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Book"))
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].authors[0].id").value(5))
                .andExpect(jsonPath("$.content[0].authors[0].fullname").value("Author"));
    }

    @Test
    void testSaveBook() throws Exception {
        BookSaving bookSaving = new BookSaving();
        bookSaving.setTitle("Test Book");
        bookSaving.setCount(3);
        bookSaving.setYear((short)2025);
        bookSaving.setDescription("Test Book Description");
        bookSaving.setAuthorsId(List.of(5,6));
        // заполни нужные поля
        Book savedBook = sampleBook;

        Mockito.when(bookService.save(bookSaving)).thenReturn(savedBook);

        mockMvc.perform(post("/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookSaving)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book 2"))
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.description").value("Test Book Description 2"))
        ;
    }

    @Test
    void testFindById() throws Exception {
        Mockito.when(bookService.findById(1)).thenReturn(sampleBook);

        mockMvc.perform(get("/book/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    void testFindByTitle_Valid() throws Exception {
        Page<Book> page = new PageImpl<>(List.of(sampleBook));
        Mockito.when(bookService.findAllByTitleContains(eq("Test"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/book/search")
                        .param("title", "Test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Book"));
    }

    @Test
    void testFindByTitle_TooShort() throws Exception {
        mockMvc.perform(get("/book/search")
                        .param("title", "abc")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError()); // можно изменить на isBadRequest() при кастомной обработке
    }
}