package com.optialloc.backend;

import com.optialloc.backend.dto.RegisterRequest;
import com.optialloc.backend.entity.User;
import com.optialloc.backend.exception.EmailAlreadyExistsException;
import com.optialloc.backend.exception.InvalidCredentialsException;
import com.optialloc.backend.repository.UserRepository;
import com.optialloc.backend.security.JwtService;
import com.optialloc.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void duplicateEmailRegistrationShouldThrowEmailAlreadyExists() {
        String email = "duplicate@example.com";

        RegisterRequest request = new RegisterRequest();
        request.setName("Duplicate User");
        request.setEmail(email);
        request.setPassword("secret123");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyExistsException.class,
                () -> authService.register(request));
    }

    @Test
    void invalidCredentialsShouldThrowInvalidCredentials() {
        String email = "missing@example.com";

        com.optialloc.backend.dto.LoginRequest request =
                new com.optialloc.backend.dto.LoginRequest();
        request.setEmail(email);
        request.setPassword("wrongpass");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
    }
}
