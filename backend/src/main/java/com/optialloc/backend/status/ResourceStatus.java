package com.optialloc.backend.status;

public final class ResourceStatus {
    public static final String AVAILABLE = "AVAILABLE";
    public static final String MAINTENANCE = "MAINTENANCE";
    public static final String INACTIVE = "INACTIVE";

    private ResourceStatus() {
    }

    public static boolean isValid(String status) {
        return AVAILABLE.equals(status)
                || MAINTENANCE.equals(status)
                || INACTIVE.equals(status);
    }
}
