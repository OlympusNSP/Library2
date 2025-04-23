package ru.olympusnsp.library.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.olympusnsp.library.model.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorService {
    Optional<Author> findByFullname(String fullName);
    Page<Author> findAllByFullnameStartsWith(String fullName, Pageable pageable);
    Author findById(Integer id);
    Page<Author> findAll(Pageable pageable);
    Author save(Author author);
    void deleteById(Integer id);
}
