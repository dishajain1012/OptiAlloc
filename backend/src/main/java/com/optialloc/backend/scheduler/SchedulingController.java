package com.optialloc.backend.scheduler;

import com.optialloc.backend.entity.Booking;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class SchedulingController {

    private final SchedulingService schedulingService;

    public SchedulingController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @PostMapping("/{requestId}")
    public ResponseEntity<Booking> schedule(
            @PathVariable Long requestId) {

        return ResponseEntity.ok(
                schedulingService.scheduleRequest(requestId)
        );
    }

    @PostMapping("/generate")
    public ResponseEntity<List<Booking>> generateSchedule() {

        return ResponseEntity.ok(
                schedulingService.schedulePendingRequests()
        );
    }
}