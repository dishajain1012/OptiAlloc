package com.optialloc.backend;

import com.optialloc.backend.controller.*;
import com.optialloc.backend.dto.AdminStatsDTO;

import com.optialloc.backend.dto.BookingDTO;
import com.optialloc.backend.dto.LoginRequest;

import com.optialloc.backend.dto.RegisterRequest;

import com.optialloc.backend.entity.Request;
import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.entity.User;
import com.optialloc.backend.repository.ResourceRepository;
import com.optialloc.backend.repository.UserRepository;
import com.optialloc.backend.status.ResourceStatus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ApiControllerIntegrationTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private RequestController requestController;

    @Autowired
    private BookingController bookingController;

    @Autowired
    private ResourceController resourceController;

    @Autowired
    private AdminController adminController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        String userEmail = "apiuser-" + UUID.randomUUID() + "@test.com";
        String adminEmail = "apiadmin-" + UUID.randomUUID() + "@test.com";

        testUser = userRepository.save(new User("API User", userEmail, passwordEncoder.encode("password123"), "USER"));
        adminUser = userRepository.save(new User("API Admin", adminEmail, passwordEncoder.encode("password123"), "ADMIN"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Principal principalFor(User user) {
        return () -> user.getEmail();
    }

    private void setAuth(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testAuthRegisterAndLoginEndpoints() {
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setName("New Registrant");
        registerReq.setEmail("newreg-" + UUID.randomUUID() + "@test.com");
        registerReq.setPassword("password123");

        ResponseEntity<?> regResponse = authController.register(registerReq);
        assertEquals(HttpStatus.OK, regResponse.getStatusCode());

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(registerReq.getEmail());
        loginReq.setPassword("password123");

        ResponseEntity<?> loginResponse = authController.login(loginReq);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
    }

    @Test
    void testRequestControllerEndpoints() {
        setAuth(testUser);

        Resource res = resourceRepository.save(new Resource("API Room", "CLASSROOM", 50, "B1", ResourceStatus.AVAILABLE));

        Request newReq = new Request(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3),
                30,
                1,
                "CLASSROOM",
                "PENDING"
        );

        Request created = requestController.createRequest(newReq, principalFor(testUser));
        assertNotNull(created.getId());

        List<Request> userRequests = requestController.getAllRequests(principalFor(testUser));
        assertFalse(userRequests.isEmpty());

        ResponseEntity<Request> getByIdResponse = requestController.getRequestById(created.getId(), principalFor(testUser));
        assertEquals(HttpStatus.OK, getByIdResponse.getStatusCode());
        assertNotNull(getByIdResponse.getBody());
    }

    @Test
    void testBookingControllerEndpoint() {
        setAuth(testUser);

        List<BookingDTO> bookings = bookingController.getAllBookings(principalFor(testUser));
        assertNotNull(bookings);
    }

    @Test
    void testResourceControllerCrudEndpoints() {
        setAuth(adminUser);

        Resource res = new Resource("Admin Resource", "LAB", 40, "Building C", ResourceStatus.AVAILABLE);
        Resource created = resourceController.createResource(res);
        assertNotNull(created.getId());

        List<Resource> allResources = resourceController.getAllResources();
        assertTrue(allResources.stream().anyMatch(r -> r.getId().equals(created.getId())));

        created.setName("Admin Resource Updated");
        Resource updated = resourceController.updateResource(created.getId(), created);
        assertEquals("Admin Resource Updated", updated.getName());

        ResponseEntity<Void> deleteResponse = resourceController.deleteResource(created.getId());
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
    }

    @Test
    void testAdminControllerEndpointsAsAdmin() {
        setAuth(adminUser);

        AdminStatsDTO stats = adminController.getAdminStatistics();
        assertNotNull(stats);

        List<Request> allRequests = adminController.getAllRequests();
        assertNotNull(allRequests);
    }

    @Test
    void testAdminControllerEndpointsForbiddenForNormalUser() {
        setAuth(testUser);

        assertThrows(AccessDeniedException.class, () -> adminController.getAdminStatistics());
        assertThrows(AccessDeniedException.class, () -> adminController.getAllRequests());
    }
}
