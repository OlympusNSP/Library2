package ru.olympusnsp.library.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.olympusnsp.library.model.Book;

import java.util.List;

@Repository
public interface BookRepository extends PagingAndSortingRepository<Book, Integer>, CrudRepository<Book, Integer> {
    Page<Book> findAllByTitleContaining(String title, Pageable pageable);

}
