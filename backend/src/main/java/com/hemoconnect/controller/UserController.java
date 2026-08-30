package com.hemoconnect.controller;

import com.hemoconnect.dto.UserResponseDto;
import com.hemoconnect.dto.UserUpdateRequestDto;
import com.hemoconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller = the layer that exposes REST endpoints over HTTP.
 *
 * Notice how thin this class is: every method just calls the service and
 * wraps the result in a ResponseEntity with the right HTTP status code.
 * No business logic lives here (Section 3 of the brief: "Do not put
 * business logic directly inside controllers").
 *
 * SECURITY NOTE: since Module 2, SecurityConfig requires every request
 * under /api/** (other than /api/auth/**) to carry a valid JWT - so every
 * endpoint below now requires being logged in. It does NOT yet check
 * roles (e.g. "only admins can list all users") - that fine-grained rule
 * arrives in Module 7. We're building bottom-up, one rule at a time.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /api/users/{id} - fetch a single user's public profile. */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * GET /api/users - list every user.
     * Equivalent of the original admin.js "/recent-users" idea, but
     * generalised. Will be restricted to ADMIN only once Module 2/7 add
     * role-based security.
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * PUT /api/users/{id}/profile - update profile fields.
     * Equivalent of PUT /api/users/profile and POST /api/users/complete-profile
     * from the original Express app.
     */
    @PutMapping("/{id}/profile")
    public ResponseEntity<UserResponseDto> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDto request) {
        return ResponseEntity.ok(userService.updateProfile(id, request));
    }

    /** DELETE /api/users/{id} - remove a user account. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
