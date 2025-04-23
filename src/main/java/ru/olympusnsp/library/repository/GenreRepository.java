package ru.olympusnsp.library.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.olympusnsp.library.model.Genre;

import java.util.List;

@Repository
public interface GenreRepository extends CrudRepository<Genre, Integer> {
    List<Genre> findAll();
}
