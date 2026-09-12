package com.optialloc.backend.dto;

import com.optialloc.backend.entity.Booking;

import java.time.LocalDateTime;

public class BookingDTO {

    private Long id;
    private Long requestId;
    private Long resourceId;
    private String resourceName;
    private String resourceType;
    private Integer resourceCapacity;
    private String resourceLocation;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String requestStatus;

    public BookingDTO() {
    }

    public BookingDTO(Booking booking) {
        if (booking != null) {
            this.id = booking.getId();
            this.startTime = booking.getStartTime();
            this.endTime = booking.getEndTime();
            this.status = booking.getStatus();

            if (booking.getRequest() != null) {
                this.requestId = booking.getRequest().getId();
                this.requestStatus = booking.getRequest().getStatus();
            }

            if (booking.getResource() != null) {
                this.resourceId = booking.getResource().getId();
                this.resourceName = booking.getResource().getName();
                this.resourceType = booking.getResource().getType();
                this.resourceCapacity = booking.getResource().getCapacity();
                this.resourceLocation = booking.getResource().getLocation();
            }
        }
    }

    public BookingDTO(Long id, Long requestId, Long resourceId, String resourceName, String resourceType, Integer resourceCapacity, String resourceLocation, LocalDateTime startTime, LocalDateTime endTime, String status, String requestStatus) {
        this.id = id;
        this.requestId = requestId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.resourceCapacity = resourceCapacity;
        this.resourceLocation = resourceLocation;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.requestStatus = requestStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Integer getResourceCapacity() {
        return resourceCapacity;
    }

    public void setResourceCapacity(Integer resourceCapacity) {
        this.resourceCapacity = resourceCapacity;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    public void setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }
}
