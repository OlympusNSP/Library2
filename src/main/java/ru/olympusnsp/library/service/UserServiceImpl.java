package ru.olympusnsp.library.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import ru.olympusnsp.library.dto.BooksReturn;
import ru.olympusnsp.library.exeption.NotFoundEntity;
import ru.olympusnsp.library.exeption.NotFoundUser;
import ru.olympusnsp.library.model.User;
import ru.olympusnsp.library.repository.UserRepository;

import java.util.HashSet;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private final UserRepository userRepository;

    /** Предоставление UserDetailsService
     *
     * @return UserDetailsService
     */
    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    /**
     *  Получение пользователя по имени
     * @param username
     * @return пользователь или null
     */
    public User getByUsername(String username){
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Получение текущенго пользователя из контекста
     * @return пользователь
     */
    public User getCurrentUser() {
        // Получение имени пользователя из контекста Spring Security
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }

    /**
     * Сохранение пользователя
     * @param user
     * @return сохраненный пользователь
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Получение пользователя по идентификтору
     *
     * @param id идентификатор
     * @return пользователь опционально
     */
    public User findById(Integer id){
        return userRepository.findById(id).orElseThrow(()-> new NotFoundUser("User with id "+id.toString()+" not found"));
    }

}
