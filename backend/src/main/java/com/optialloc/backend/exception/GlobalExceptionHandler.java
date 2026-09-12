package com.optialloc.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleResourceUnavailable(
            ResourceUnavailableException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "RESOURCE_UNAVAILABLE",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<Map<String, String>> handleBookingConflict(
            BookingConflictException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "BOOKING_CONFLICT",
                        "message", ex.getMessage()
                ));
    }
}