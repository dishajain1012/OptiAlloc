package com.optialloc.backend.repository;

import com.optialloc.backend.entity.Booking;
import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByResourceAndStartTimeLessThanAndEndTimeGreaterThan(
            Resource resource,
            LocalDateTime endTime,
            LocalDateTime startTime
    );
    long countByResourceAndStatus(Resource resource, String status);

    List<Booking> findByRequest_User(User user);
}