package com.optialloc.backend.service;

import com.optialloc.backend.dto.AuthResponse;
import com.optialloc.backend.dto.LoginRequest;
import com.optialloc.backend.dto.RegisterRequest;
import com.optialloc.backend.entity.User;
import com.optialloc.backend.exception.EmailAlreadyExistsException;
import com.optialloc.backend.exception.InvalidCredentialsException;
import com.optialloc.backend.repository.UserRepository;
import com.optialloc.backend.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Normal users must be USER.
        // Do not allow public registration to create ADMIN accounts.
        user.setRole("USER");

        userRepository.save(user);

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return new AuthResponse(
                "Registration successful",
                token,
                user.getRole()
        );
    }

    public AuthResponse login(LoginRequest request) {

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return new AuthResponse(
                "Login successful",
                token,
                user.getRole()
        );
    }
}