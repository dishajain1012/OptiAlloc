package com.optialloc.backend;

import com.optialloc.backend.entity.Request;
import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.entity.User;
import com.optialloc.backend.repository.BookingRepository;
import com.optialloc.backend.repository.RequestRepository;
import com.optialloc.backend.repository.ResourceRepository;
import com.optialloc.backend.repository.UserRepository;
import com.optialloc.backend.service.RequestService;
import com.optialloc.backend.status.RequestStatus;
import com.optialloc.backend.status.ResourceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class UserOwnershipTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private RequestService requestService;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        requestRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void requestCreationAssociatesAuthenticatedUser() {
        User owner = saveUser("owner", "owner-" + UUID.randomUUID() + "@example.com");
        String resourceType = "OWNERSHIP_CREATE_" + UUID.randomUUID();
        Resource resource = resourceRepository.save(new Resource(
                "Owned Room",
                resourceType,
                10,
                "A1",
                ResourceStatus.AVAILABLE
        ));

        Request payload = new Request(
                LocalDateTime.of(2026, 11, 1, 9, 0),
                LocalDateTime.of(2026, 11, 1, 11, 0),
                2,
                1,
                resourceType,
                RequestStatus.PENDING
        );

        Request created = requestService.createRequest(payload, principalFor(owner));

        assertEquals(owner.getId(), created.getUser().getId());
        assertEquals(RequestStatus.ALLOCATED, created.getStatus());
        assertNotNull(created.getAllocatedResource());
        assertEquals(resource.getId(), created.getAllocatedResource().getId());
    }

    @Test
    void userCanRetrieveOwnRequestsAndNotOthers() {
        User userA = saveUser("alice", "alice-" + UUID.randomUUID() + "@example.com");
        User userB = saveUser("bob", "bob-" + UUID.randomUUID() + "@example.com");

        String userAType = "OWNERSHIP_A_" + UUID.randomUUID();
        String userBType = "OWNERSHIP_B_" + UUID.randomUUID();

        Request requestA = saveRequest(userA, userAType, LocalDateTime.of(2026, 11, 2, 9, 0), LocalDateTime.of(2026, 11, 2, 11, 0));
        Request requestB = saveRequest(userB, userBType, LocalDateTime.of(2026, 11, 2, 12, 0), LocalDateTime.of(2026, 11, 2, 14, 0));

        List<Request> requestsForA = requestService.getAllRequestsForCurrentUser(principalFor(userA));
        assertEquals(1, requestsForA.size());
        assertEquals(requestA.getId(), requestsForA.get(0).getId());

        Optional<Request> deniedLookup = requestService.getRequestByIdForCurrentUser(requestB.getId(), principalFor(userA));
        assertTrue(deniedLookup.isEmpty());

        Optional<Request> allowedLookup = requestService.getRequestByIdForCurrentUser(requestA.getId(), principalFor(userA));
        assertTrue(allowedLookup.isPresent());
        assertEquals(requestA.getId(), allowedLookup.get().getId());
    }

    @Test
    void userCannotSeeOtherUsersRequestsInListEndpoint() {
        User userA = saveUser("alice2", "alice2-" + UUID.randomUUID() + "@example.com");
        User userB = saveUser("bob2", "bob2-" + UUID.randomUUID() + "@example.com");

        String userBType = "OWNERSHIP_LIST_B_" + UUID.randomUUID();
        saveRequest(userB, userBType, LocalDateTime.of(2026, 11, 3, 9, 0), LocalDateTime.of(2026, 11, 3, 11, 0));

        List<Request> result = requestService.getAllRequestsForCurrentUser(principalFor(userA));

        assertTrue(result.stream().noneMatch(request -> request.getUser() != null && request.getUser().getId().equals(userB.getId())));
    }

    @Test
    void allocationBehaviorStillWorksWithUserOwnership() {
        User owner = saveUser("allocator", "allocator-" + UUID.randomUUID() + "@example.com");
        String type = "OWNERSHIP_ALLOC_" + UUID.randomUUID();

        Resource resource = resourceRepository.save(new Resource("Allocator Room", type, 10, "A1", ResourceStatus.AVAILABLE));
        Request request = new Request(
                LocalDateTime.of(2026, 11, 5, 9, 0),
                LocalDateTime.of(2026, 11, 5, 11, 0),
                2,
                1,
                type,
                RequestStatus.PENDING
        );

        Request saved = requestService.createRequest(request, principalFor(owner));

        assertEquals(owner.getId(), saved.getUser().getId());
        assertEquals(RequestStatus.ALLOCATED, saved.getStatus());
        assertNotNull(saved.getAllocatedResource());
        assertEquals(resource.getId(), saved.getAllocatedResource().getId());
    }

    private Request saveRequest(User user, String type, LocalDateTime start, LocalDateTime end) {
        Resource resource = resourceRepository.save(new Resource("Owned Room", type, 8, "A1", ResourceStatus.AVAILABLE));
        Request request = new Request(start, end, 2, 1, type, RequestStatus.PENDING);
        request.setUser(user);
        Request saved = requestRepository.save(request);
        saved.setAllocatedResource(resource);
        saved.setStatus(RequestStatus.ALLOCATED);
        return requestRepository.save(saved);
    }

    private User saveUser(String name, String email) {
        return userRepository.save(new User(name, email, "secret123", "USER"));
    }

    private Principal principalFor(User user) {
        return () -> user.getEmail();
    }
}
