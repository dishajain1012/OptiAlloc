package com.optialloc.backend.controller;

import com.optialloc.backend.entity.Request;
import com.optialloc.backend.service.AllocationService;
import com.optialloc.backend.service.RequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
@Tag(name = "Requests", description = "Resource allocation request endpoints")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Create resource request", description = "Submits a new resource allocation request and triggers automatic scheduling.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request submitted and processed (ALLOCATED or CONFLICT)"),
            @ApiResponse(responseCode = "400", description = "Invalid time range or capacity values"),
            @ApiResponse(responseCode = "401", description = "Unauthorized JWT token")
    })
    public Request createRequest(
            @Valid @RequestBody Request request,
            Principal principal) {

        return requestService.createRequest(request, principal);
    }

    @GetMapping
    @Operation(summary = "Get user requests", description = "Retrieves all requests submitted by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of user requests returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized JWT token")
    })
    public List<Request> getAllRequests(Principal principal) {

        return requestService.getAllRequestsForCurrentUser(principal);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get request by ID", description = "Retrieves a specific request by ID for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request details returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Request belongs to another user")
    })
    public ResponseEntity<Request> getRequestById(
            @PathVariable Long id,
            Principal principal) {

        return requestService.getRequestByIdForCurrentUser(id, principal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @PostMapping("/{id}/allocate")
    @Operation(summary = "Re-trigger allocation", description = "Triggers the allocation engine for a specific request ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Allocation processed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized JWT token")
    })
    public ResponseEntity<Request> allocateRequest(
            @PathVariable Long id) {

        Request allocatedRequest =
                allocationService.allocateRequest(id);

        return ResponseEntity.ok(allocatedRequest);
    }
}
