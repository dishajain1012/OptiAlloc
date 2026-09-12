package com.optialloc.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Integer capacityRequired;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false)
    private String resourceType;

    @Column(nullable = false)
    private String status;

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
}