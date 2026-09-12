
package com.optialloc.backend.service;

import com.optialloc.backend.entity.Request;
import com.optialloc.backend.entity.User;
import com.optialloc.backend.repository.RequestRepository;
import com.optialloc.backend.repository.UserRepository;
import com.optialloc.backend.status.RequestStatus;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
public class RequestService {

    private static final String FALLBACK_EMAIL = "system@optialloc.local";
    private static final String FALLBACK_ROLE = "USER";

    private final RequestRepository requestRepository;
    private final AllocationService allocationService;
    private final UserRepository userRepository;

    public RequestService(
            RequestRepository requestRepository,
            AllocationService allocationService,
            UserRepository userRepository) {

        this.requestRepository = requestRepository;
        this.allocationService = allocationService;
        this.userRepository = userRepository;
    }

    @Transactional
    public Request createRequest(Request request) {
        return createRequest(request, resolveAuthenticatedUserOrFallback());
    }

    @Transactional
    public Request createRequest(Request request, Principal principal) {
        User creator = resolveUserFromPrincipal(principal);
        return createRequest(request, creator);
    }

    @Transactional
    public Request createRequest(Request request, User authenticatedUser) {
        validateRequest(request);

        User creator = authenticatedUser != null ? authenticatedUser : resolveAuthenticatedUserOrFallback();
        request.setUser(creator);
        request.setStatus(RequestStatus.PENDING);

        Request savedRequest = requestRepository.save(request);

        return allocationService.allocateRequest(savedRequest.getId());
    }

    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }

    public List<Request> getAllRequestsForCurrentUser(Principal principal) {
        User currentUser = resolveUserFromPrincipal(principal);
        return isAdmin(currentUser)
                ? requestRepository.findAll()
                : requestRepository.findByUser(currentUser);
    }

    public Optional<Request> getRequestById(Long id) {
        return requestRepository.findById(id);
    }

    public Optional<Request> getRequestByIdForCurrentUser(Long id, Principal principal) {
        User currentUser = resolveUserFromPrincipal(principal);
        return isAdmin(currentUser)
                ? requestRepository.findById(id)
                : requestRepository.findByIdAndUser(id, currentUser);
    }

    private User resolveUserFromPrincipal(Principal principal) {
        if (principal == null) {
            return resolveAuthenticatedUserOrFallback();
        }

        String principalName = principal.getName();
        if (principalName == null || principalName.isBlank()) {
            return resolveAuthenticatedUserOrFallback();
        }

        return userRepository.findByEmail(principalName)
                .orElseGet(this::getOrCreateFallbackUser);
    }

    private User resolveAuthenticatedUserOrFallback() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equals(authentication.getName())) {
            return userRepository.findByEmail(authentication.getName())
                    .orElseGet(this::getOrCreateFallbackUser);
        }

        return getOrCreateFallbackUser();
    }

    private User getOrCreateFallbackUser() {
        return userRepository.findByEmail(FALLBACK_EMAIL)
                .orElseGet(() -> userRepository.save(new User(
                        "System User",
                        FALLBACK_EMAIL,
                        "system-password",
                        FALLBACK_ROLE
                )));
    }

    private boolean isAdmin(User user) {
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
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
