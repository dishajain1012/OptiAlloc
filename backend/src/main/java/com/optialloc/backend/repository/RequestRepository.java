
package com.optialloc.backend.repository;

import com.optialloc.backend.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByStatus(String status);

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Request r
        WHERE r.allocatedResource.id = :resourceId
          AND r.status = 'ALLOCATED'
          AND r.startTime < :endTime
          AND r.endTime > :startTime
    """)
    boolean existsOverlappingAllocation(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}

