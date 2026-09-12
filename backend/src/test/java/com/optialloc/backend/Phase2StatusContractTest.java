package com.optialloc.backend;

import com.optialloc.backend.entity.Request;
import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.exception.ResourceUnavailableException;
import com.optialloc.backend.repository.BookingRepository;
import com.optialloc.backend.repository.RequestRepository;
import com.optialloc.backend.repository.ResourceRepository;
import com.optialloc.backend.scheduler.SchedulingService;
import com.optialloc.backend.service.AllocationService;
import com.optialloc.backend.service.RequestService;
import com.optialloc.backend.status.RequestStatus;
import com.optialloc.backend.status.ResourceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Phase2StatusContractTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void availableResourceCanBeAllocatedAndKeepsResourceStatusAvailable() {
        String type = "PHASE2_AVAILABLE_" + UUID.randomUUID();
        Resource resource = resourceRepository.save(new Resource(
                "Available Room",
                type,
                10,
                "A1",
                ResourceStatus.AVAILABLE
        ));

        Request request = new Request(
                LocalDateTime.of(2026, 9, 27, 9, 0),
                LocalDateTime.of(2026, 9, 27, 11, 0),
                5,
                2,
                type,
                RequestStatus.PENDING
        );

        Request allocated = requestService.createRequest(request);

        assertEquals(RequestStatus.ALLOCATED, allocated.getStatus());
        assertNotNull(allocated.getAllocatedResource());
        assertEquals(resource.getId(), allocated.getAllocatedResource().getId());

        Resource persisted = resourceRepository.findById(resource.getId()).orElseThrow();
        assertEquals(ResourceStatus.AVAILABLE, persisted.getStatus());
    }

    @Test
    void maintenanceAndInactiveResourcesCannotBeAllocated() {
        String type = "PHASE2_MAINTENANCE_" + UUID.randomUUID();
        resourceRepository.save(new Resource("Room Maint", type, 10, "A1", ResourceStatus.MAINTENANCE));
        resourceRepository.save(new Resource("Room Inactive", type, 12, "A2", ResourceStatus.INACTIVE));

        Request request = new Request(
                LocalDateTime.of(2026, 9, 28, 9, 0),
                LocalDateTime.of(2026, 9, 28, 11, 0),
                5,
                2,
                type,
                RequestStatus.PENDING
        );

        Request saved = requestRepository.save(request);
        Request allocated = allocationService.allocateRequest(saved.getId());

        assertEquals(RequestStatus.CONFLICT, allocated.getStatus());
        assertNull(allocated.getAllocatedResource());
    }

    @Test
    void conflictingOverlapSetsRequestStatusToConflict() {
        String type = "PHASE2_CONFLICT_" + UUID.randomUUID();
        Resource resource = resourceRepository.save(new Resource("Shared Room", type, 12, "B1", ResourceStatus.AVAILABLE));

        Request first = requestRepository.save(new Request(
                LocalDateTime.of(2026, 9, 29, 10, 0),
                LocalDateTime.of(2026, 9, 29, 12, 0),
                8,
                1,
                type,
                RequestStatus.PENDING
        ));

        Request second = requestRepository.save(new Request(
                LocalDateTime.of(2026, 9, 29, 11, 0),
                LocalDateTime.of(2026, 9, 29, 13, 0),
                8,
                1,
                type,
                RequestStatus.PENDING
        ));

        Request firstAllocated = requestService.createRequest(first);
        assertEquals(RequestStatus.ALLOCATED, firstAllocated.getStatus());

        Request secondRequest = new Request(
                second.getStartTime(),
                second.getEndTime(),
                second.getCapacityRequired(),
                second.getPriority(),
                second.getResourceType(),
                RequestStatus.PENDING
        );

        Request secondAllocated = requestService.createRequest(secondRequest);
        assertEquals(RequestStatus.CONFLICT, secondAllocated.getStatus());
        assertEquals(ResourceStatus.AVAILABLE, resourceRepository.findById(resource.getId()).orElseThrow().getStatus());
    }

    @Test
    void backToBackBookingsAreAllowed() {
        String type = "PHASE2_BACK_TO_BACK_" + UUID.randomUUID();
        Resource resource = resourceRepository.save(new Resource("Back to Back Room", type, 10, "C1", ResourceStatus.AVAILABLE));

        Request first = requestRepository.save(new Request(
                LocalDateTime.of(2026, 9, 30, 9, 0),
                LocalDateTime.of(2026, 9, 30, 10, 0),
                8,
                1,
                type,
                RequestStatus.PENDING
        ));

        Request second = requestRepository.save(new Request(
                LocalDateTime.of(2026, 9, 30, 10, 0),
                LocalDateTime.of(2026, 9, 30, 11, 0),
                8,
                1,
                type,
                RequestStatus.PENDING
        ));

        Request firstAllocated = requestService.createRequest(first);
        Request secondAllocated = requestService.createRequest(second);

        assertEquals(RequestStatus.ALLOCATED, firstAllocated.getStatus());
        assertEquals(RequestStatus.ALLOCATED, secondAllocated.getStatus());

        long createdBookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getResource() != null)
                .filter(booking -> booking.getResource().getId().equals(resource.getId()))
                .count();

        assertEquals(2, createdBookings);
    }

    @Test
    void bestFitAllocationStillChoosesSmallestAvailableMatchingResource() {
        String type = "PHASE2_BEST_FIT_" + UUID.randomUUID();
        Resource smaller = resourceRepository.save(new Resource("Smaller Room", type, 6, "D1", ResourceStatus.AVAILABLE));
        Resource larger = resourceRepository.save(new Resource("Larger Room", type, 9, "D2", ResourceStatus.AVAILABLE));

        Request request = new Request(
                LocalDateTime.of(2026, 10, 1, 9, 0),
                LocalDateTime.of(2026, 10, 1, 11, 0),
                5,
                2,
                type,
                RequestStatus.PENDING
        );

        Request allocated = requestService.createRequest(request);

        assertEquals(RequestStatus.ALLOCATED, allocated.getStatus());
        assertEquals(smaller.getId(), allocated.getAllocatedResource().getId());
    }

    @Test
    void invalidRequestValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> requestService.validateRequest(new Request(
                LocalDateTime.of(2026, 10, 2, 12, 0),
                LocalDateTime.of(2026, 10, 2, 11, 0),
                5,
                1,
                "TYPE",
                RequestStatus.PENDING
        )));

        assertThrows(IllegalArgumentException.class, () -> requestService.validateRequest(new Request(
                LocalDateTime.of(2026, 10, 2, 9, 0),
                LocalDateTime.of(2026, 10, 2, 11, 0),
                0,
                1,
                "TYPE",
                RequestStatus.PENDING
        )));

        assertThrows(IllegalArgumentException.class, () -> requestService.validateRequest(new Request(
                LocalDateTime.of(2026, 10, 2, 9, 0),
                LocalDateTime.of(2026, 10, 2, 11, 0),
                5,
                1,
                "",
                RequestStatus.PENDING
        )));

        assertThrows(IllegalArgumentException.class, () -> requestService.validateRequest(new Request(
                LocalDateTime.of(2026, 10, 2, 9, 0),
                LocalDateTime.of(2026, 10, 2, 11, 0),
                5,
                0,
                "TYPE",
                RequestStatus.PENDING
        )));

        assertThrows(IllegalArgumentException.class, () -> requestService.validateRequest(new Request(
                LocalDateTime.of(2026, 10, 2, 9, 0),
                LocalDateTime.of(2026, 10, 2, 11, 0),
                5,
                5,
                "TYPE",
                RequestStatus.PENDING
        )));
    }
}
