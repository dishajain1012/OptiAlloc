package com.optialloc.backend;

import com.optialloc.backend.controller.AdminController;
import com.optialloc.backend.controller.RequestController;
import com.optialloc.backend.controller.ResourceController;
import com.optialloc.backend.entity.Request;
import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.entity.User;
import com.optialloc.backend.repository.BookingRepository;
import com.optialloc.backend.repository.RequestRepository;
import com.optialloc.backend.repository.ResourceRepository;
import com.optialloc.backend.repository.UserRepository;
import com.optialloc.backend.service.AllocationService;
import com.optialloc.backend.service.RequestService;
import com.optialloc.backend.status.RequestStatus;
import com.optialloc.backend.status.ResourceStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class Phase6ValidationAndErrorTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private ResourceController resourceController;

    @Autowired
    private RequestController requestController;

    @Autowired
    private AdminController adminController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(new User("User Test", "phase6user-" + UUID.randomUUID() + "@test.com", "pass123", "USER"));
        adminUser = userRepository.save(new User("Admin Test", "phase6admin-" + UUID.randomUUID() + "@test.com", "pass123", "ADMIN"));
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

    // 1. Invalid request data (null/blank type)
    @Test
    void testInvalidRequestDataNullType() {
        setAuth(testUser);
        Request invalid = new Request(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                10,
                1,
                "",
                RequestStatus.PENDING
        );
        assertThrows(IllegalArgumentException.class, () -> requestService.createRequest(invalid, principalFor(testUser)));
    }

    // 2. Invalid date range (endTime before startTime)
    @Test
    void testInvalidDateRange() {
        setAuth(testUser);
        Request invalid = new Request(
                LocalDateTime.now().plusHours(5),
                LocalDateTime.now().plusHours(2),
                10,
                1,
                "CLASSROOM",
                RequestStatus.PENDING
        );
        assertThrows(IllegalArgumentException.class, () -> requestService.createRequest(invalid, principalFor(testUser)));
    }

    // 3. Invalid capacity (capacity <= 0)
    @Test
    void testInvalidCapacity() {
        setAuth(testUser);
        Request invalid = new Request(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                0,
                1,
                "CLASSROOM",
                RequestStatus.PENDING
        );
        assertThrows(IllegalArgumentException.class, () -> requestService.createRequest(invalid, principalFor(testUser)));
    }

    // 4. Resource not found (404 / Optional.empty)
    @Test
    void testResourceNotFound() {
        setAuth(testUser);
        ResponseEntity<Resource> response = resourceController.getResourceById(999999L);
        assertEquals(404, response.getStatusCode().value());
    }

    // 5. Request not found (404 / Optional.empty)
    @Test
    void testRequestNotFound() {
        setAuth(testUser);
        ResponseEntity<Request> response = requestController.getRequestById(999999L, principalFor(testUser));
        assertEquals(403, response.getStatusCode().value());
    }

    // 6. Allocation conflict/error response
    @Test
    void testAllocationConflictHandled() {
        setAuth(testUser);
        String type = "PHASE6_CONFLICT_" + UUID.randomUUID();
        Resource resource = resourceRepository.save(new Resource("Room A", type, 20, "Building 1", ResourceStatus.AVAILABLE));

        LocalDateTime start = LocalDateTime.of(2026, 12, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 10, 12, 0);

        Request req1 = new Request(start, end, 10, 1, type, RequestStatus.PENDING);
        Request created1 = requestService.createRequest(req1, principalFor(testUser));
        assertEquals(RequestStatus.ALLOCATED, created1.getStatus());

        Request req2 = new Request(start, end, 10, 1, type, RequestStatus.PENDING);
        Request created2 = requestService.createRequest(req2, principalFor(testUser));
        assertEquals(RequestStatus.CONFLICT, created2.getStatus());
        assertNull(created2.getAllocatedResource());
    }

    // 7. Unauthorized access check (unauthenticated principal fallback)
    @Test
    void testUnauthorizedAccess() {
        Optional<Request> result = requestService.getRequestByIdForCurrentUser(999999L, null);
        assertTrue(result.isEmpty());
    }

    // 8. Forbidden USER -> ADMIN endpoint
    @Test
    void testForbiddenUserAccessToAdminEndpoint() {
        setAuth(testUser);
        assertThrows(AccessDeniedException.class, () -> adminController.getAdminStatistics());
    }

    // 9. Valid request still succeeds
    @Test
    void testValidRequestSucceeds() {
        setAuth(testUser);
        String type = "PHASE6_VALID_" + UUID.randomUUID();
        resourceRepository.save(new Resource("Valid Room", type, 50, "Building V", ResourceStatus.AVAILABLE));

        Request request = new Request(
                LocalDateTime.of(2026, 12, 15, 14, 0),
                LocalDateTime.of(2026, 12, 15, 16, 0),
                30,
                2,
                type,
                RequestStatus.PENDING
        );

        Request result = requestService.createRequest(request, principalFor(testUser));
        assertNotNull(result.getId());
        assertEquals(testUser.getId(), result.getUser().getId());
        assertEquals(RequestStatus.ALLOCATED, result.getStatus());
    }

    // 10. Existing ownership tests check
    @Test
    void testOwnershipIsolationEnforced() {
        User alice = userRepository.save(new User("Alice", "alice6-" + UUID.randomUUID() + "@test.com", "pass", "USER"));
        User bob = userRepository.save(new User("Bob", "bob6-" + UUID.randomUUID() + "@test.com", "pass", "USER"));

        String type = "PHASE6_OWNERSHIP_" + UUID.randomUUID();
        Request reqAlice = new Request(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 5, 1, type, RequestStatus.PENDING);
        Request savedAlice = requestService.createRequest(reqAlice, principalFor(alice));

        List<Request> bobRequests = requestService.getAllRequestsForCurrentUser(principalFor(bob));
        assertTrue(bobRequests.stream().noneMatch(r -> r.getId().equals(savedAlice.getId())));
    }
}
