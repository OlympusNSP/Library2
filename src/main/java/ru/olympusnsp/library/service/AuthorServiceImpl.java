package ru.olympusnsp.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.olympusnsp.library.exeption.NotFoundEntity;
import ru.olympusnsp.library.model.Author;
import ru.olympusnsp.library.repository.AuthorRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorServiceImpl implements AuthorService {

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    private final AuthorRepository authorRepository;

    /**
     * Поиск автора по полному имени
     *
     * @param fullName имя
     * @return автор
     */
   public Optional<Author> findByFullname(String fullName){
        return authorRepository.findByFullname(fullName);
    }

    /**
     * Поиск автора по совпадению с началом полного имени
     *
     * @param fullName имя,
     * @param pageable  страницы
     * @return страница с найденными авторами
     */
    public Page<Author> findAllByFullnameStartsWith(String fullName, Pageable pageable){
        return authorRepository.findAllByFullnameStartsWith(fullName,pageable);
    }

    /**
     * Поиск автора по id
     *
     * @param id имя
     * @return страница с найденными авторами
     */
    public Author findById(Integer id){
        return authorRepository.findById(id).orElseThrow(() -> new NotFoundEntity("Author with id " + id.toString() + " not found"));
    }

    /**
     * Получение всех авторов
     *
     * @param pageable запрос страницы
     * @return страница со списком авторов
     */
    public Page<Author> findAll(Pageable pageable){
       return authorRepository.findAll(pageable);
    }

    /**
     * Сохрание автора
     *
     * @param author автор
     * @return возврат автора
     */
    public Author save(Author author){
       return authorRepository.save(author);
    }

    /**
     * Удаление автора
     *
     * @param id идентификатор автора
     * @return возврат автора
     */
    public void deleteById(Integer id) {
        if (!authorRepository.existsById(id)) {
            throw new NotFoundEntity("Author with id " + id + " not found");
        }
        authorRepository.deleteById(id);
    }
}
