package ru.olympusnsp.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.olympusnsp.library.dto.JwtAuthenticationResponse;
import ru.olympusnsp.library.dto.SignInRequest;
import ru.olympusnsp.library.dto.SignUpRequest;
import ru.olympusnsp.library.service.AuthenticationService;

@RestController
@RequestMapping("/auth")
@Tag(name = "Аутентификация")
public class AuthController {

    AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    private final AuthenticationService authenticationService;

    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/sign-up")
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }
}
