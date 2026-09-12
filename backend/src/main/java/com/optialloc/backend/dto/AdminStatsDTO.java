package com.optialloc.backend.dto;

public class AdminStatsDTO {

    private long totalResources;
    private long availableResources;
    private long maintenanceResources;
    private long inactiveResources;

    private long totalRequests;
    private long allocatedRequests;
    private long pendingRequests;
    private long conflictRequests;

    public AdminStatsDTO() {
    }

    public AdminStatsDTO(long totalResources,
                         long availableResources,
                         long maintenanceResources,
                         long inactiveResources,
                         long totalRequests,
                         long allocatedRequests,
                         long pendingRequests,
                         long conflictRequests) {
        this.totalResources = totalResources;
        this.availableResources = availableResources;
        this.maintenanceResources = maintenanceResources;
        this.inactiveResources = inactiveResources;
        this.totalRequests = totalRequests;
        this.allocatedRequests = allocatedRequests;
        this.pendingRequests = pendingRequests;
        this.conflictRequests = conflictRequests;
    }

    public long getTotalResources() {
        return totalResources;
    }

    public void setTotalResources(long totalResources) {
        this.totalResources = totalResources;
    }

    public long getAvailableResources() {
        return availableResources;
    }

    public void setAvailableResources(long availableResources) {
        this.availableResources = availableResources;
    }

    public long getMaintenanceResources() {
        return maintenanceResources;
    }

    public void setMaintenanceResources(long maintenanceResources) {
        this.maintenanceResources = maintenanceResources;
    }

    public long getInactiveResources() {
        return inactiveResources;
    }

    public void setInactiveResources(long inactiveResources) {
        this.inactiveResources = inactiveResources;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getAllocatedRequests() {
        return allocatedRequests;
    }

    public void setAllocatedRequests(long allocatedRequests) {
        this.allocatedRequests = allocatedRequests;
    }

    public long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public long getConflictRequests() {
        return conflictRequests;
    }

    public void setConflictRequests(long conflictRequests) {
        this.conflictRequests = conflictRequests;
    }
}
