package com.optialloc.backend.scheduler;

import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.repository.BookingRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ConflictDetector {

    private final BookingRepository bookingRepository;

    public ConflictDetector(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public boolean hasConflict(
            Resource resource,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        return !bookingRepository
                .findByResourceAndStartTimeLessThanAndEndTimeGreaterThan(
                        resource,
                        endTime,
                        startTime
                )
                .isEmpty();
    }
}