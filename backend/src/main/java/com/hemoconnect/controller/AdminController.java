package com.hemoconnect.controller;

import com.hemoconnect.dto.*;
import com.hemoconnect.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The admin dashboard's REST API.
 *
 * @PreAuthorize("hasRole('ADMIN')") is placed on the CLASS here, applying
 * to every method below - safe to do because literally every endpoint in
 * this controller is admin-only. (Compare with DonorController or
 * BloodRequestController, where different endpoints need different rules,
 * so each one is annotated individually instead.)
 *
 * NOTE: "view contact messages" (part of the original admin dashboard) is
 * intentionally not here yet - it depends on Module 8 (Contact), which
 * doesn't exist yet. It'll be added to this controller once that module
 * is built, rather than faked now.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /** GET /api/admin/stats - overview counts for the dashboard. */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getStatistics() {
        return ResponseEntity.ok(adminService.getStatistics());
    }

    /** GET /api/admin/users - every user, most recently created first. */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDto>> getUsers() {
        return ResponseEntity.ok(adminService.listRecentUsers());
    }

    /** DELETE /api/admin/users/{id} - remove a user account. */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/admin/donors - every donor profile in the system. */
    @GetMapping("/donors")
    public ResponseEntity<List<DonorProfileResponseDto>> getDonors() {
        return ResponseEntity.ok(adminService.listAllDonors());
    }

    /** GET /api/admin/blood-requests - every blood request, any status. */
    @GetMapping("/blood-requests")
    public ResponseEntity<List<BloodRequestResponseDto>> getAllRequests() {
        return ResponseEntity.ok(adminService.listAllRequests());
    }

    /** GET /api/admin/blood-requests/flagged - requests awaiting review. */
    @GetMapping("/blood-requests/flagged")
    public ResponseEntity<List<BloodRequestResponseDto>> getFlaggedRequests() {
        return ResponseEntity.ok(adminService.listFlaggedRequests());
    }

    /** POST /api/admin/blood-requests/{id}/flag - flag a request for review. */
    @PostMapping("/blood-requests/{id}/flag")
    public ResponseEntity<BloodRequestResponseDto> flagRequest(
            @PathVariable Long id, @Valid @RequestBody ModerationReasonDto dto) {
        return ResponseEntity.ok(adminService.flagRequest(id, dto.getReason()));
    }

    /** POST /api/admin/blood-requests/{id}/approve - clear a flag, request continues normally. */
    @PostMapping("/blood-requests/{id}/approve")
    public ResponseEntity<BloodRequestResponseDto> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveFlaggedRequest(id));
    }

    /** POST /api/admin/blood-requests/{id}/reject - cancel a flagged request and notify the requester. */
    @PostMapping("/blood-requests/{id}/reject")
    public ResponseEntity<BloodRequestResponseDto> rejectRequest(
            @PathVariable Long id, @Valid @RequestBody ModerationReasonDto dto) {
        return ResponseEntity.ok(adminService.rejectFlaggedRequest(id, dto.getReason()));
    }
}
