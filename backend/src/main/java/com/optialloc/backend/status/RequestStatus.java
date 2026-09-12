package com.optialloc.backend.status;

public final class RequestStatus {
    public static final String PENDING = "PENDING";
    public static final String ALLOCATED = "ALLOCATED";
    public static final String CONFLICT = "CONFLICT";

    private RequestStatus() {
    }

    public static boolean isValid(String status) {
        return PENDING.equals(status)
                || ALLOCATED.equals(status)
                || CONFLICT.equals(status);
    }
}
