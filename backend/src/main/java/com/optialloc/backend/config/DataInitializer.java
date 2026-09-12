package com.optialloc.backend.config;

import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.entity.User;
import com.optialloc.backend.repository.ResourceRepository;
import com.optialloc.backend.repository.UserRepository;
import com.optialloc.backend.status.ResourceStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            ResourceRepository resourceRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Ensure test user exists with correct encoded password
        userRepository.findByEmail("test123@gmail.com").ifPresentOrElse(
                user -> {
                    user.setPassword(passwordEncoder.encode("123456"));
                    userRepository.save(user);
                },
                () -> {
                    User testUser = new User(
                            "Test User",
                            "test123@gmail.com",
                            passwordEncoder.encode("123456"),
                            "USER"
                    );
                    userRepository.save(testUser);
                }
        );

        // Ensure admin user exists with correct encoded password and ADMIN role
        userRepository.findByEmail("admin@optialloc.com").ifPresentOrElse(
                admin -> {
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRole("ADMIN");
                    userRepository.save(admin);
                },
                () -> {
                    User adminUser = new User(
                            "Admin User",
                            "admin@optialloc.com",
                            passwordEncoder.encode("admin123"),
                            "ADMIN"
                    );
                    userRepository.save(adminUser);
                }
        );

        // Ensure sample available resources exist if database is empty
        if (resourceRepository.count() == 0) {
            resourceRepository.save(new Resource(
                    "Classroom A-101",
                    "CLASSROOM",
                    60,
                    "Building A - Floor 1",
                    ResourceStatus.AVAILABLE
            ));

            resourceRepository.save(new Resource(
                    "Science Lab B-204",
                    "LAB",
                    35,
                    "Building B - Floor 2",
                    ResourceStatus.AVAILABLE
            ));

            resourceRepository.save(new Resource(
                    "Executive Meeting Room C",
                    "MEETING_ROOM",
                    25,
                    "Building C - Floor 3",
                    ResourceStatus.AVAILABLE
            ));

            resourceRepository.save(new Resource(
                    "Projector & AV Kit 01",
                    "EQUIPMENT",
                    10,
                    "Tech Store",
                    ResourceStatus.AVAILABLE
            ));
        }
    }
}
