package com.optialloc.backend.controller;

import com.optialloc.backend.dto.AdminStatsDTO;
import com.optialloc.backend.entity.Request;
import com.optialloc.backend.service.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin dashboard oversight and statistics endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get admin statistics (Admin Only)", description = "Returns system-wide aggregate resource and request statistics. Requires ADMIN role authorization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AdminStatsDTO returned with aggregate numbers"),
            @ApiResponse(responseCode = "401", description = "Unauthorized JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin authorization required")
    })
    public AdminStatsDTO getAdminStatistics() {
        return adminService.getAdminStatistics();
    }

    @GetMapping("/requests")
    @Operation(summary = "Get all system requests (Admin Only)", description = "Returns all resource allocation requests across all users. Requires ADMIN role authorization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all system requests returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin authorization required")
    })
    public List<Request> getAllRequests() {
        return adminService.getAllRequests();
    }
}
