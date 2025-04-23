package ru.olympusnsp.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.olympusnsp.library.exeption.NotFoundEntity;
import ru.olympusnsp.library.model.Genre;
import ru.olympusnsp.library.repository.GenreRepository;

import java.util.List;
import java.util.Optional;

@Service
public class GenreServiceImpl implements GenreService {

    GenreServiceImpl(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    private final GenreRepository genreRepository;

    /**
     * Поиск жанра по идентификатору
     * @param id идентификатор
     * @return Жанр
     */
    @Override
    public Genre findById(Integer id) {

        return genreRepository.findById(id).orElseThrow(()-> new NotFoundEntity("Genre with id " + id.toString()+ " not found"));
    }

    /**
     * Сохранение жарна
     * @param genre жанр
     * @return сохраненный жанр
     */
    @Override
    public Genre save(Genre genre) {
        return genreRepository.save(genre);
    }

    @Override
    public List<Genre> findAll() {
        return genreRepository.findAll();
    }

    public void deleteById(Integer id){
        if(!genreRepository.existsById(id)){
            throw new NotFoundEntity("Genre with id " + id.toString() + " not found");
        }
        genreRepository.deleteById(id);
    }
}
