package ru.olympusnsp.library.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.olympusnsp.library.dto.JwtAuthenticationResponse;
import ru.olympusnsp.library.dto.SignInRequest;
import ru.olympusnsp.library.dto.SignUpRequest;
import ru.olympusnsp.library.model.Role;
import ru.olympusnsp.library.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    @Autowired
    private  UserService userService;
    @Autowired
    private  JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Регистрация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signUp(SignUpRequest request) {

        var user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setViolations(0);
        user.setStatusBlock(false);
        user.setRole(Role.ROLE_USER);
        user.setBookRented(0);

        userService.save(user);

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        logger.info("Authentication000");
        logger.info(request.getUsername());
        logger.info(request.getPassword());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));
        logger.info("Authentication111");
        var user = userService
                .userDetailsService()
                .loadUserByUsername(request.getUsername());
        logger.info("AUthentification222");
        var jwt = jwtService.generateToken(user);
        logger.info("AUthentification333");
        return new JwtAuthenticationResponse(jwt);
    }
}
