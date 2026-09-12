
package com.optialloc.backend.service;

import com.optialloc.backend.entity.Request;
import com.optialloc.backend.repository.RequestRepository;
import com.optialloc.backend.status.RequestStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final AllocationService allocationService;

    public RequestService(
            RequestRepository requestRepository,
            AllocationService allocationService) {

        this.requestRepository = requestRepository;
        this.allocationService = allocationService;
    }

    @Transactional
    public Request createRequest(Request request) {
        validateRequest(request);

        request.setStatus(RequestStatus.PENDING);

        Request savedRequest = requestRepository.save(request);

        return allocationService.allocateRequest(savedRequest.getId());
    }

    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }

    public Optional<Request> getRequestById(Long id) {
        return requestRepository.findById(id);
    }

    public void validateRequest(Request request) {
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

        request.setStatus(RequestStatus.PENDING);
    }
}
