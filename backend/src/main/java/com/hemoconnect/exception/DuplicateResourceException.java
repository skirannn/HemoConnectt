package com.hemoconnect.exception;

/**
 * Thrown when trying to create something that violates a uniqueness rule —
 * for example registering with an email that's already in use (the same
 * check the original Express route did with `User.findOne({ email })`
 * before creating a new user).
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
