package com.optialloc.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Start time is required")
    @Column(nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Column(nullable = false)
    private LocalDateTime endTime;

    @NotNull(message = "Required capacity is required")
    @Positive(message = "Capacity must be greater than zero")
    @Column(nullable = false)
    private Integer capacityRequired;

    @NotNull(message = "Priority is required")
    @Min(value = 1, message = "Priority must be at least 1")
    @Column(nullable = false)
    private Integer priority;

    @NotBlank(message = "Resource type is required")
    @Column(nullable = false)
    private String resourceType;

    @Column(nullable = false)
    private String status;

    // Resource automatically allocated to this request
    @ManyToOne
    @JoinColumn(name = "allocated_resource_id")
    private Resource allocatedResource;

    public Request() {
    }

    public Request(LocalDateTime startTime,
                   LocalDateTime endTime,
                   Integer capacityRequired,
                   Integer priority,
                   String resourceType,
                   String status) {

        this.startTime = startTime;
        this.endTime = endTime;
        this.capacityRequired = capacityRequired;
        this.priority = priority;
        this.resourceType = resourceType;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getCapacityRequired() {
        return capacityRequired;
    }

    public void setCapacityRequired(Integer capacityRequired) {
        this.capacityRequired = capacityRequired;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Resource getAllocatedResource() {
        return allocatedResource;
    }

    public void setAllocatedResource(Resource allocatedResource) {
        this.allocatedResource = allocatedResource;
    }
}

