package com.optialloc.backend.controller;

import com.optialloc.backend.entity.Resource;
import com.optialloc.backend.service.ResourceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@Tag(name = "Resources", description = "Facility resource management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    @Operation(summary = "Get all resources", description = "Retrieves all facility resources available in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of resources returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized JWT token")
    })
    public List<Resource> getAllResources() {
        return resourceService.getAllResources();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resource by ID", description = "Retrieves a specific resource by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource details returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized JWT token"),
            @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    public ResponseEntity<Resource> getResourceById(
            @PathVariable Long id) {

        return resourceService.getResourceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create resource (Admin Only)", description = "Creates a new resource. Requires ADMIN role authorization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid capacity or required fields missing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin authorization required")
    })
    public Resource createResource(
            @Valid @RequestBody Resource resource) {

        return resourceService.createResource(resource);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update resource (Admin Only)", description = "Updates an existing resource. Requires ADMIN role authorization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid resource parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin authorization required"),
            @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    public Resource updateResource(
            @PathVariable Long id,
            @Valid @RequestBody Resource resource) {

        return resourceService.updateResource(id, resource);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete resource (Admin Only)", description = "Deletes a resource by ID. Requires ADMIN role authorization.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Resource deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin authorization required")
    })
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long id) {

        resourceService.deleteResource(id);

        return ResponseEntity.noContent().build();
    }
}