package ru.olympusnsp.library.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import ru.olympusnsp.library.model.Book;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = BookRepository.class))
class BookRepositoryTest {

//    @Autowired
//    private BookRepository bookRepository;
//
//    private Book testBook;
//
//    @BeforeEach
//    void setUp() {
//        testBook = new Book();
//        testBook.setYear(2010);
//        testBook.setDescription("Description");
//        testBook.setTitle("Title");
//        testBook.
//        boo
//    }
//    @Test
//    void findAllByTitleContaining() {
//    }
}