package ru.olympusnsp.library.service;

import ru.olympusnsp.library.dto.JwtAuthenticationResponse;
import ru.olympusnsp.library.dto.SignInRequest;
import ru.olympusnsp.library.dto.SignUpRequest;

public interface AuthenticationService {
    public JwtAuthenticationResponse signUp(SignUpRequest request);
    public JwtAuthenticationResponse signIn(SignInRequest request);
}

