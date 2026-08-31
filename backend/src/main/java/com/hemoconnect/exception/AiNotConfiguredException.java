package com.hemoconnect.exception;

/**
 * Thrown when someone calls the AI assistant but AI_API_KEY was never
 * set. Unlike JwtService (Module 2), which fails the whole app at
 * startup if its secret is missing, the AI assistant is an optional,
 * supplementary feature (Module 10) - the rest of the app should keep
 * working fine without it. So this only fails the ONE request that
 * needed it, with a clear message, instead of blocking the app from
 * starting at all.
 */
public class AiNotConfiguredException extends RuntimeException {
    public AiNotConfiguredException(String message) {
        super(message);
    }
}
