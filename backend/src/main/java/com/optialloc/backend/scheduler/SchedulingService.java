package com.optialloc.backend.scheduler;

import com.optialloc.backend.entity.Booking;
import com.optialloc.backend.entity.Request;
import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.exception.ResourceUnavailableException;
import com.optialloc.backend.repository.BookingRepository;
import com.optialloc.backend.repository.RequestRepository;
import com.optialloc.backend.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SchedulingService {

    private final RequestRepository requestRepository;
    private final ResourceRepository resourceRepository;
    private final BookingRepository bookingRepository;
    private final ConflictDetector conflictDetector;

    public SchedulingService(
            RequestRepository requestRepository,
            ResourceRepository resourceRepository,
            BookingRepository bookingRepository,
            ConflictDetector conflictDetector) {

        this.requestRepository = requestRepository;
        this.resourceRepository = resourceRepository;
        this.bookingRepository = bookingRepository;
        this.conflictDetector = conflictDetector;
    }

@Transactional(noRollbackFor = ResourceUnavailableException.class)
public Booking scheduleRequest(Long requestId) {

    Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));

    List<Resource> resources =
            resourceRepository.findByStatusAndTypeAndCapacityGreaterThanEqual(
                    "AVAILABLE",
                    request.getResourceType(),
                    request.getCapacityRequired()
            );

    List<Resource> availableResources = resources.stream()
            .filter(resource ->
                    !conflictDetector.hasConflict(
                            resource,
                            request.getStartTime(),
                            request.getEndTime()
                    ))
            .toList();

    if (availableResources.isEmpty()) {
        request.setStatus("REJECTED");
        requestRepository.save(request);

        throw new ResourceUnavailableException(
                "No suitable resource available for the requested time"
        );
    }

    Resource bestResource = availableResources.stream()
            .min(
                    Comparator
                            .comparingInt((Resource resource) ->
                                    resource.getCapacity()
                                            - request.getCapacityRequired()
                            )
                            .thenComparingLong(resource ->
                                    bookingRepository.countByResourceAndStatus(
                                            resource,
                                            "CONFIRMED"
                                    )
                            )
            )
            .orElseThrow();

    // Lock the selected resource before the final conflict check
    Resource lockedResource = resourceRepository
            .findByIdForUpdate(bestResource.getId())
            .orElseThrow(() ->
                    new RuntimeException("Resource not found"));

    // Re-check conflict after acquiring the database lock
    boolean conflict = conflictDetector.hasConflict(
            lockedResource,
            request.getStartTime(),
            request.getEndTime()
    );

    if (conflict) {
        request.setStatus("REJECTED");
        requestRepository.save(request);

        throw new ResourceUnavailableException(
                "Resource became unavailable during scheduling"
        );
    }

    Booking booking = new Booking(
            request,
            lockedResource,
            request.getStartTime(),
            request.getEndTime(),
            "CONFIRMED"
    );

    request.setStatus("ALLOCATED");
    requestRepository.save(request);

    return bookingRepository.save(booking);
}

    public List<Booking> schedulePendingRequests() {

        List<Request> pendingRequests =
                requestRepository.findAll()
                        .stream()
                        .filter(request ->
                                "PENDING".equals(request.getStatus()))
                        .sorted(
                                Comparator.comparing(
                                        Request::getPriority
                                ).reversed()
                        )
                        .toList();

        List<Booking> bookings = new ArrayList<>();

        for (Request request : pendingRequests) {

            try {
                Booking booking =
                        scheduleRequest(request.getId());

                bookings.add(booking);

            } catch (RuntimeException e) {
                // Continue scheduling other requests
            }
        }

        return bookings;
    }
}