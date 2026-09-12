
package com.optialloc.backend.controller;

import com.optialloc.backend.entity.Request;
import com.optialloc.backend.service.AllocationService;
import com.optialloc.backend.service.RequestService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private final RequestService requestService;
    private final AllocationService allocationService;

    public RequestController(
            RequestService requestService,
            AllocationService allocationService) {

        this.requestService = requestService;
        this.allocationService = allocationService;
    }

    @PostMapping
    public Request createRequest(
            @RequestBody Request request) {

        return requestService.createRequest(request);
    }

    @GetMapping
    public List<Request> getAllRequests() {

        return requestService.getAllRequests();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Request> getRequestById(
            @PathVariable Long id) {

        return requestService.getRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/allocate")
    public ResponseEntity<Request> allocateRequest(
            @PathVariable Long id) {

        Request allocatedRequest =
                allocationService.allocateRequest(id);

        return ResponseEntity.ok(allocatedRequest);
    }
}

