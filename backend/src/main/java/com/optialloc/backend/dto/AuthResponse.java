package com.optialloc.backend.dto;

public record AuthResponse(
        String message,
        String token,
        String role
) {
}