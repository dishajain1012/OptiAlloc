package com.optialloc.backend.service;

import com.optialloc.backend.entity.Booking;
import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.repository.BookingRepository;
import com.optialloc.backend.repository.ResourceRepository;
import com.optialloc.backend.scheduler.ConflictDetector;
import com.optialloc.backend.exception.BookingConflictException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final ConflictDetector conflictDetector;

    public BookingService(
            BookingRepository bookingRepository,
            ResourceRepository resourceRepository,
            ConflictDetector conflictDetector) {

        this.bookingRepository = bookingRepository;
        this.resourceRepository = resourceRepository;
        this.conflictDetector = conflictDetector;
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
}