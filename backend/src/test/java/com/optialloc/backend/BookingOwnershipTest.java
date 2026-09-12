package com.optialloc.backend;

import com.optialloc.backend.dto.BookingDTO;
import com.optialloc.backend.entity.Request;
import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.entity.User;
import com.optialloc.backend.repository.BookingRepository;
import com.optialloc.backend.repository.RequestRepository;
import com.optialloc.backend.repository.ResourceRepository;
import com.optialloc.backend.repository.UserRepository;
import com.optialloc.backend.service.BookingService;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingOwnershipTest {

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

    @Autowired
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        requestRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void userCanRetrieveOwnBookingsAndNotOthers() {
        User userA = saveUser("alice_b", "alice_b-" + UUID.randomUUID() + "@example.com");
        User userB = saveUser("bob_b", "bob_b-" + UUID.randomUUID() + "@example.com");

        String typeA = "BK_OWN_A_" + UUID.randomUUID();
        String typeB = "BK_OWN_B_" + UUID.randomUUID();

        resourceRepository.save(new Resource("Room A", typeA, 10, "Building A", ResourceStatus.AVAILABLE));
        resourceRepository.save(new Resource("Room B", typeB, 20, "Building B", ResourceStatus.AVAILABLE));

        Request reqA = new Request(
                LocalDateTime.of(2026, 12, 1, 9, 0),
                LocalDateTime.of(2026, 12, 1, 11, 0),
                5,
                1,
                typeA,
                RequestStatus.PENDING
        );
        Request createdA = requestService.createRequest(reqA, principalFor(userA));
        assertEquals(RequestStatus.ALLOCATED, createdA.getStatus());

        Request reqB = new Request(
                LocalDateTime.of(2026, 12, 1, 12, 0),
                LocalDateTime.of(2026, 12, 1, 14, 0),
                15,
                1,
                typeB,
                RequestStatus.PENDING
        );
        Request createdB = requestService.createRequest(reqB, principalFor(userB));
        assertEquals(RequestStatus.ALLOCATED, createdB.getStatus());

        List<BookingDTO> bookingsForA = bookingService.getBookingsForCurrentUser(principalFor(userA));
        assertEquals(1, bookingsForA.size());
        assertEquals(createdA.getId(), bookingsForA.get(0).getRequestId());
        assertEquals("Room A", bookingsForA.get(0).getResourceName());

        List<BookingDTO> bookingsForB = bookingService.getBookingsForCurrentUser(principalFor(userB));
        assertEquals(1, bookingsForB.size());
        assertEquals(createdB.getId(), bookingsForB.get(0).getRequestId());
        assertEquals("Room B", bookingsForB.get(0).getResourceName());
    }

    @Test
    void userBookingListIsIsolated() {
        User userA = saveUser("alice_iso", "alice_iso-" + UUID.randomUUID() + "@example.com");
        User userB = saveUser("bob_iso", "bob_iso-" + UUID.randomUUID() + "@example.com");

        String typeB = "BK_ISO_B_" + UUID.randomUUID();
        resourceRepository.save(new Resource("Room B Iso", typeB, 20, "Building B", ResourceStatus.AVAILABLE));

        Request reqB = new Request(
                LocalDateTime.of(2026, 12, 2, 12, 0),
                LocalDateTime.of(2026, 12, 2, 14, 0),
                15,
                1,
                typeB,
                RequestStatus.PENDING
        );
        requestService.createRequest(reqB, principalFor(userB));

        List<BookingDTO> bookingsForA = bookingService.getBookingsForCurrentUser(principalFor(userA));
        assertTrue(bookingsForA.isEmpty());
    }

    private User saveUser(String name, String email) {
        return userRepository.save(new User(name, email, "secret123", "USER"));
    }

    private Principal principalFor(User user) {
        return user::getEmail;
    }
}
