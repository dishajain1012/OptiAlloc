package com.optialloc.backend.service;

import com.optialloc.backend.dto.LoginRequest;
import com.optialloc.backend.dto.RegisterRequest;
import com.optialloc.backend.entity.User;
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

    public String register(RegisterRequest request) {

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Encrypt password before storing
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Set role from registration request
        user.setRole(request.getRole());

        // Save user in database
        userRepository.save(user);

        // Generate JWT token
        return jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );
    }

    public String login(LoginRequest request) {

        // Find user by email
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid email or password");
        }

        // Generate JWT using user's actual database role
        return jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );
    }
}