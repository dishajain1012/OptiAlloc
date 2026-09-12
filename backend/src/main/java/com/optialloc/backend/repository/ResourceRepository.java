package com.optialloc.backend.repository;

import com.optialloc.backend.entity.Resource;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByStatusAndTypeAndCapacityGreaterThanEqual(
            String status,
            String type,
            Integer capacity
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Resource r WHERE r.id = :id")
    Optional<Resource> findByIdForUpdate(@Param("id") Long id);

    long countByStatus(String status);
}