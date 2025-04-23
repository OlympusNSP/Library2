package ru.olympusnsp.library.service;

import ru.olympusnsp.library.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreService {
    Genre findById(Integer id);
    Genre save(Genre genre);
    List<Genre> findAll();
    void deleteById(Integer id);
}
