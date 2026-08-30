package com.hemoconnect.controller;

import com.hemoconnect.dto.*;
import com.hemoconnect.security.UserPrincipal;
import com.hemoconnect.service.BloodRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the blood request life cycle.
 *
 * AUTHORIZATION NOTE: notice that @PreAuthorize here only checks ROLE
 * (e.g. "hasRole('RECIPIENT')"), never "does this request belong to this
 * caller" - that check needs the request loaded from the database first,
 * which @PreAuthorize (evaluated BEFORE the method body runs) can't do.
 * That "is this actually yours?" check lives inside BloodRequestService
 * (see assertRequesterOrAdmin) once the entity is loaded. Two different
 * layers, two different kinds of authorization - a useful distinction to
 * be able to explain in an interview.
 */
@RestController
@RequestMapping("/api/blood-requests")
public class BloodRequestController {

    private final BloodRequestService bloodRequestService;

    public BloodRequestController(BloodRequestService bloodRequestService) {
        this.bloodRequestService = bloodRequestService;
    }

    /** POST /api/blood-requests - a recipient creates a new request. */
    @PostMapping
    @PreAuthorize("hasRole('RECIPIENT')")
    public ResponseEntity<BloodRequestResponseDto> createRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BloodRequestCreateDto dto) {
        var response = bloodRequestService.createRequest(principal.getUser().getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** GET /api/blood-requests/{id} - view one request (and its donor responses). */
    @GetMapping("/{id}")
    public ResponseEntity<BloodRequestResponseDto> getRequest(@PathVariable Long id) {
        return ResponseEntity.ok(bloodRequestService.getRequestById(id));
    }

    /** GET /api/blood-requests/active - every request still open for donor responses. */
    @GetMapping("/active")
    public ResponseEntity<List<BloodRequestResponseDto>> listActive() {
        return ResponseEntity.ok(bloodRequestService.listActiveRequests());
    }

    /** GET /api/blood-requests/mine - the logged-in recipient's own request history. */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('RECIPIENT')")
    public ResponseEntity<List<BloodRequestResponseDto>> listMine(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bloodRequestService.listMyRequests(principal.getUser().getId()));
    }

    /** POST /api/blood-requests/{id}/respond - a donor accepts/declines/maybes a request. */
    @PostMapping("/{id}/respond")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<BloodRequestResponseDto> respond(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RespondToRequestDto dto) {
        return ResponseEntity.ok(
                bloodRequestService.respondToRequest(id, principal.getUser().getId(), dto));
    }

    /** POST /api/blood-requests/{id}/confirm - the requester picks one accepted donor. */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<BloodRequestResponseDto> confirm(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ConfirmDonorDto dto) {
        return ResponseEntity.ok(
                bloodRequestService.confirmDonor(id, principal.getUser().getId(), dto));
    }

    /** POST /api/blood-requests/{id}/fulfill - mark the donation as completed. */
    @PostMapping("/{id}/fulfill")
    public ResponseEntity<BloodRequestResponseDto> fulfill(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                bloodRequestService.fulfillRequest(id, principal.getUser().getId()));
    }

    /** POST /api/blood-requests/{id}/cancel - the requester cancels their own request. */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<BloodRequestResponseDto> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                bloodRequestService.cancelRequest(id, principal.getUser().getId()));
    }
}
