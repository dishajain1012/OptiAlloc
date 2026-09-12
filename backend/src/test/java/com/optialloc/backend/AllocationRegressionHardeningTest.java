package com.optialloc.backend;

import com.optialloc.backend.entity.Booking;
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
public class AllocationRegressionHardeningTest {

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(new User("Tester", "alloc-test-" + UUID.randomUUID() + "@test.com", "pass", "USER"));
    }

    private Principal principalFor(User user) {
        return () -> user.getEmail();
    }

    @Test
    void testBestFitSelectsSmallestSuitableResource() {
        String type = "BEST_FIT_" + UUID.randomUUID();

        // Save 3 resources of increasing capacity: 100, 30, 50
        Resource large = resourceRepository.save(new Resource("Large Room", type, 100, "B1", ResourceStatus.AVAILABLE));
        Resource small = resourceRepository.save(new Resource("Small Room", type, 30, "B2", ResourceStatus.AVAILABLE));
        Resource medium = resourceRepository.save(new Resource("Medium Room", type, 50, "B3", ResourceStatus.AVAILABLE));

        // Request capacity of 25 -> Best fit should pick Small Room (capacity 30)
        Request request = new Request(
                LocalDateTime.of(2026, 12, 20, 10, 0),
                LocalDateTime.of(2026, 12, 20, 12, 0),
                25,
                1,
                type,
                RequestStatus.PENDING
        );

        Request allocated = requestService.createRequest(request, principalFor(testUser));

        assertEquals(RequestStatus.ALLOCATED, allocated.getStatus());
        assertNotNull(allocated.getAllocatedResource());
        assertEquals(small.getId(), allocated.getAllocatedResource().getId());
    }

    @Test
    void testMaintenanceAndInactiveResourcesExcluded() {
        String type = "EXCLUDE_STATUS_" + UUID.randomUUID();

        Resource maintenance = resourceRepository.save(new Resource("Maint Room", type, 50, "B1", ResourceStatus.MAINTENANCE));
        Resource inactive = resourceRepository.save(new Resource("Inactive Room", type, 50, "B2", ResourceStatus.INACTIVE));

        Request request = new Request(
                LocalDateTime.of(2026, 12, 21, 10, 0),
                LocalDateTime.of(2026, 12, 21, 12, 0),
                20,
                1,
                type,
                RequestStatus.PENDING
        );

        Request result = requestService.createRequest(request, principalFor(testUser));

        assertEquals(RequestStatus.CONFLICT, result.getStatus());
        assertNull(result.getAllocatedResource());
    }

    @Test
    void testBackToBackBookingsAllowedOnSameResource() {
        String type = "BACK_TO_BACK_" + UUID.randomUUID();
        Resource resource = resourceRepository.save(new Resource("Shared Room", type, 40, "B1", ResourceStatus.AVAILABLE));

        // Booking 1: 09:00 to 10:00
        Request req1 = new Request(
                LocalDateTime.of(2026, 12, 22, 9, 0),
                LocalDateTime.of(2026, 12, 22, 10, 0),
                20,
                1,
                type,
                RequestStatus.PENDING
        );
        Request allocated1 = requestService.createRequest(req1, principalFor(testUser));
        assertEquals(RequestStatus.ALLOCATED, allocated1.getStatus());

        // Booking 2: 10:00 to 11:00 (Starts exactly when Booking 1 ends)
        Request req2 = new Request(
                LocalDateTime.of(2026, 12, 22, 10, 0),
                LocalDateTime.of(2026, 12, 22, 11, 0),
                20,
                1,
                type,
                RequestStatus.PENDING
        );
        Request allocated2 = requestService.createRequest(req2, principalFor(testUser));

        assertEquals(RequestStatus.ALLOCATED, allocated2.getStatus());
        assertEquals(resource.getId(), allocated2.getAllocatedResource().getId());
    }

    @Test
    void testSuccessfulAllocationCreatesExactlyOneBookingRecord() {
        String type = "BOOKING_REC_" + UUID.randomUUID();
        Resource resource = resourceRepository.save(new Resource("Rec Room", type, 40, "B1", ResourceStatus.AVAILABLE));

        Request request = new Request(
                LocalDateTime.of(2026, 12, 23, 14, 0),
                LocalDateTime.of(2026, 12, 23, 16, 0),
                20,
                1,
                type,
                RequestStatus.PENDING
        );

        Request allocated = requestService.createRequest(request, principalFor(testUser));

        assertEquals(RequestStatus.ALLOCATED, allocated.getStatus());

        List<Booking> bookings = bookingRepository.findAll();
        long matchingBookings = bookings.stream()
                .filter(b -> b.getRequest().getId().equals(allocated.getId()))
                .count();

        assertEquals(1, matchingBookings);
    }

    @Test
    void testResourceAdministrativeStatusRemainsAvailableAfterAllocation() {
        String type = "STATUS_PRESERVED_" + UUID.randomUUID();
        Resource resource = resourceRepository.save(new Resource("Preserved Room", type, 40, "B1", ResourceStatus.AVAILABLE));

        Request request = new Request(
                LocalDateTime.of(2026, 12, 24, 14, 0),
                LocalDateTime.of(2026, 12, 24, 16, 0),
                20,
                1,
                type,
                RequestStatus.PENDING
        );

        requestService.createRequest(request, principalFor(testUser));

        Resource reloadedResource = resourceRepository.findById(resource.getId()).orElseThrow();
        assertEquals(ResourceStatus.AVAILABLE, reloadedResource.getStatus());
    }
}
