package ru.olympusnsp.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import ru.olympusnsp.library.model.Genre;
import ru.olympusnsp.library.service.GenreService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/genre")
@Tag(name="Жанры")
public class GenreController {

    @Autowired
    private GenreService genreService;

    @GetMapping("/")
    @Operation(summary = "Список всех жанров")
    List<Genre> getAllGenres() {
        return genreService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить жанр по идентификатору")
    Genre getById(@PathVariable Integer id) {
        return genreService.findById(id);
    }

    @PostMapping("/")
    @Operation(summary = "Сохранение жанра")
    Genre save(@RequestBody Genre genre) {
        return genreService.save(genre);
    }
}
