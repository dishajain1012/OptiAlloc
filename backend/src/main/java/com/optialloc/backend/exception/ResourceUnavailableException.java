package com.optialloc.backend.exception;

public class ResourceUnavailableException extends RuntimeException {

    public ResourceUnavailableException(String message) {
        super(message);
    }
}