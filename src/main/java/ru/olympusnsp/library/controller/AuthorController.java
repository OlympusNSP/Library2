package ru.olympusnsp.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ParameterOutOfBoundsException;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.olympusnsp.library.exeption.NotFoundEntity;
import ru.olympusnsp.library.model.Author;
import ru.olympusnsp.library.service.AuthorService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/author")
@Tag(name = "Контроллер авторов книг")
public class AuthorController {

    @Autowired
    private  AuthorService authorService;


    @GetMapping("")
    @Operation(summary = "Постраничная выдача всех авторов")
    public Page<Author> all(@PageableDefault(page=0,size=100) Pageable pageable) {
        return authorService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Предоставление автора по идентификатору")
    public Author getAuthor(@PathVariable Integer id) {
         return authorService.findById(id);
    }


    @GetMapping("/searchByName")
    @Operation(summary = "Поиск автора по имени")
    public Page<Author> findByNameStart(@Param("search")String fullname, @PageableDefault(size=50) Pageable pageable) {
        if (fullname.length()<3){
        throw new ResponseStatusException(
                HttpStatus.LENGTH_REQUIRED, "search params too small", null);
        }
        return authorService.findAllByFullnameStartsWith(fullname,pageable);
    }
    @PostMapping("/")
    public Author save(@RequestBody Author author) {
        return authorService.save(author);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Integer id) {
        authorService.deleteById(id);
    }
}
