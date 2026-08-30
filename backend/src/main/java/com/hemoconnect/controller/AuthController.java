package com.hemoconnect.controller;

import com.hemoconnect.dto.*;
import com.hemoconnect.service.AuthService;
import com.hemoconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.hemoconnect.security.UserPrincipal;

import java.util.Map;

/**
 * All authentication endpoints. Everything under /api/auth/** is public
 * (see SecurityConfig) - you don't need to already be logged in to reach
 * any of these, which makes sense: you can't log in if logging in required
 * you to already be logged in!
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /** POST /api/auth/signup - create an account and return a JWT immediately. */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDto> signup(@Valid @RequestBody RegisterRequestDto request) {
        AuthResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** POST /api/auth/login - verify credentials and return a JWT. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * GET /api/auth/verify - called by the frontend on page load to check
     * "is my saved token still valid, and who am I?". Requires a valid
     * Bearer token (JwtAuthenticationFilter must have authenticated the
     * request already for this endpoint to be reachable at all).
     *
     * @AuthenticationPrincipal injects the currently authenticated user
     * directly - Spring Security reads it out of the SecurityContext that
     * JwtAuthenticationFilter populated earlier in the request.
     */
    @GetMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> verify(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(UserResponseDto.fromEntity(principal.getUser()));
    }

    /**
     * POST /api/auth/forgot-password - confirms an account exists for this
     * email. (The real reset flow is send-otp -> reset-password below;
     * this endpoint mirrors the original app's separate "forgot password"
     * entry point.)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody SendOtpRequestDto request) {
        userService.getUserEntityByEmail(request.getEmail()); // throws 404 if not found
        return ResponseEntity.ok(Map.of("message", "If that account exists, reset instructions have been sent."));
    }

    /** POST /api/auth/send-otp - generates and (server-side) logs a 6-digit OTP. */
    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody SendOtpRequestDto request) {
        authService.sendOtp(request);
        return ResponseEntity.ok(Map.of("message", "OTP sent. Check the server console for local development."));
    }

    /** POST /api/auth/reset-password - verifies the OTP and sets a new password. */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }
}
