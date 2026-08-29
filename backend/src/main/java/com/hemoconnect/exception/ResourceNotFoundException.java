package com.hemoconnect.exception;

/**
 * Thrown whenever we look something up by id (or another key) and it
 * doesn't exist — e.g. UserService.getUserById(999) when there's no user
 * with id 999. Caught by GlobalExceptionHandler and turned into a clean
 * 404 JSON response instead of a raw stack trace.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
