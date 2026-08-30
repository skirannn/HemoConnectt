package com.hemoconnect.controller;

import com.hemoconnect.dto.DonorProfileRequestDto;
import com.hemoconnect.dto.DonorProfileResponseDto;
import com.hemoconnect.dto.RecordDonationRequestDto;
import com.hemoconnect.service.DonorProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for a donor's own profile: create/update it, view it,
 * and record a completed donation.
 *
 * AUTHORIZATION: every endpoint here uses @PreAuthorize to check that the
 * logged-in user IS the donor in the URL (#userId == ... principal's own
 * id) OR is an ADMIN. This is the Spring Security equivalent of the
 * original project's `requireRole`/`requireRoles` Express middleware,
 * just applied per-endpoint instead of per-router.
 */
@RestController
@RequestMapping("/api/donors")
public class DonorController {

    private final DonorProfileService donorProfileService;

    public DonorController(DonorProfileService donorProfileService) {
        this.donorProfileService = donorProfileService;
    }

    /** PUT /api/donors/{userId}/profile - create or update a donor's profile. */
    @PutMapping("/{userId}/profile")
    @PreAuthorize("#userId == authentication.principal.user.id or hasRole('ADMIN')")
    public ResponseEntity<DonorProfileResponseDto> upsertProfile(
            @PathVariable Long userId,
            @Valid @RequestBody DonorProfileRequestDto request) {
        return ResponseEntity.ok(donorProfileService.createOrUpdateProfile(userId, request));
    }

    /** GET /api/donors/{userId}/profile - view a donor's profile. */
    @GetMapping("/{userId}/profile")
    @PreAuthorize("#userId == authentication.principal.user.id or hasRole('ADMIN')")
    public ResponseEntity<DonorProfileResponseDto> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(donorProfileService.getProfileByUserId(userId));
    }

    /** POST /api/donors/{userId}/donations - record a completed donation. */
    @PostMapping("/{userId}/donations")
    @PreAuthorize("#userId == authentication.principal.user.id or hasRole('ADMIN')")
    public ResponseEntity<DonorProfileResponseDto> recordDonation(
            @PathVariable Long userId,
            @Valid @RequestBody RecordDonationRequestDto request) {
        return ResponseEntity.ok(donorProfileService.recordDonation(userId, request));
    }
}
