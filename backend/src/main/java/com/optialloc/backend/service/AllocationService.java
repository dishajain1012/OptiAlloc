
package com.optialloc.backend.service;

import com.optialloc.backend.entity.Booking;
import com.optialloc.backend.entity.Request;
import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.repository.BookingRepository;
import com.optialloc.backend.repository.RequestRepository;
import com.optialloc.backend.repository.ResourceRepository;
import com.optialloc.backend.status.RequestStatus;
import com.optialloc.backend.status.ResourceStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class AllocationService {

    private final ResourceRepository resourceRepository;
    private final RequestRepository requestRepository;
    private final BookingRepository bookingRepository;

    public AllocationService(
            ResourceRepository resourceRepository,
            RequestRepository requestRepository,
            BookingRepository bookingRepository) {

        this.resourceRepository = resourceRepository;
        this.requestRepository = requestRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Request allocateRequest(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        validateRequestForAllocation(request);

        List<Resource> resources = resourceRepository.findByStatusAndTypeAndCapacityGreaterThanEqual(
                ResourceStatus.AVAILABLE,
                request.getResourceType(),
                request.getCapacityRequired()
        );

        if (resources.isEmpty()) {
            request.setStatus(RequestStatus.CONFLICT);
            return requestRepository.save(request);
        }

        resources.sort(Comparator.comparing(Resource::getCapacity));

        Resource selectedResource = null;

        for (Resource resource : resources) {
            boolean hasConflict = requestRepository.existsOverlappingAllocation(
                    resource.getId(),
                    request.getStartTime(),
                    request.getEndTime()
            );

            if (!hasConflict) {
                selectedResource = resource;
                break;
            }
        }

        if (selectedResource == null) {
            request.setStatus(RequestStatus.CONFLICT);
            return requestRepository.save(request);
        }

        request.setAllocatedResource(selectedResource);
        request.setStatus(RequestStatus.ALLOCATED);
        Request savedRequest = requestRepository.save(request);

        Booking booking = new Booking(
                savedRequest,
                selectedResource,
                savedRequest.getStartTime(),
                savedRequest.getEndTime(),
                "CONFIRMED"
        );
        bookingRepository.save(booking);

        return savedRequest;
    }

    private void validateRequestForAllocation(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        if (request.getCapacityRequired() == null || request.getCapacityRequired() <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero");
        }

        if (request.getResourceType() == null || request.getResourceType().isBlank()) {
            throw new IllegalArgumentException("Resource type is required");
        }

        if (request.getPriority() == null || request.getPriority() < 1 || request.getPriority() > 4) {
            throw new IllegalArgumentException("Priority must be between 1 and 4");
        }

        if (request.getStatus() != null) {
            String normalizedStatus = request.getStatus().trim().toUpperCase();
            if (!RequestStatus.isValid(normalizedStatus)) {
                throw new IllegalArgumentException("Request status must be PENDING, ALLOCATED, or CONFLICT");
            }
        }
    }
}

