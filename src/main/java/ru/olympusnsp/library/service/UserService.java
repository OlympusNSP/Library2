package ru.olympusnsp.library.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.olympusnsp.library.model.User;

import java.util.Optional;

public interface UserService {
    UserDetailsService userDetailsService();
    User getByUsername(String username);
    User save(User user);
    User getCurrentUser();
    User findById(Integer id);
}
