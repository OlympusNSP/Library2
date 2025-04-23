package ru.olympusnsp.library.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.olympusnsp.library.model.Author;

import java.util.Optional;

@Repository
public interface AuthorRepository extends PagingAndSortingRepository<Author, Integer>, CrudRepository<Author,Integer> {
    Optional<Author> findByFullname(String fullname);
    Page<Author> findAllByFullnameStartsWith(String fullname, Pageable pageable);
}
