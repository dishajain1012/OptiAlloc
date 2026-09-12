package com.optialloc.backend.service;

import com.optialloc.backend.dto.BookingDTO;
import com.optialloc.backend.entity.Booking;
import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.entity.User;
import com.optialloc.backend.exception.BookingConflictException;
import com.optialloc.backend.repository.BookingRepository;
import com.optialloc.backend.repository.ResourceRepository;
import com.optialloc.backend.repository.UserRepository;
import com.optialloc.backend.scheduler.ConflictDetector;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public class BookingService {

    private static final String FALLBACK_EMAIL = "system@optialloc.local";
    private static final String FALLBACK_ROLE = "USER";

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final ConflictDetector conflictDetector;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            ResourceRepository resourceRepository,
            ConflictDetector conflictDetector,
            UserRepository userRepository) {

        this.bookingRepository = bookingRepository;
        this.resourceRepository = resourceRepository;
        this.conflictDetector = conflictDetector;
        this.userRepository = userRepository;
    }

    @Transactional
    public Booking createBooking(Booking booking) {

        // Lock the resource row before checking for conflicts
        Resource resource = resourceRepository
                .findByIdForUpdate(booking.getResource().getId())
                .orElseThrow(() ->
                        new RuntimeException("Resource not found"));

        // Check for overlapping bookings while holding the lock
        boolean conflict = conflictDetector.hasConflict(
                resource,
                booking.getStartTime(),
                booking.getEndTime()
        );

        if (conflict) {
            throw new BookingConflictException(
                    "Resource is already booked for the requested time"
            );
        }

        // Always use the managed/locked resource entity
        booking.setResource(resource);

        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<BookingDTO> getBookingsForCurrentUser(Principal principal) {
        User currentUser = resolveUserFromPrincipal(principal);
        List<Booking> bookings = isAdmin(currentUser)
                ? bookingRepository.findAll()
                : bookingRepository.findByRequest_User(currentUser);

        return bookings.stream()
                .map(BookingDTO::new)
                .toList();
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
}