package com.hemoconnect.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * The consistent JSON shape every error response from this API returns,
 * e.g.:
 * {
 *   "timestamp": "2026-08-29T10:15:30",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "User not found with id: 42"
 * }
 *
 * `fieldErrors` is only populated for validation failures (@Valid), where
 * we want to tell the frontend exactly which field was wrong and why.
 */
public class ErrorResponse {

    private final LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String error;
    private String message;
    private Map<String, String> fieldErrors;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public ErrorResponse(int status, String error, String message, Map<String, String> fieldErrors) {
        this(status, error, message);
        this.fieldErrors = fieldErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
