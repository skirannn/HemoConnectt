package com.hemoconnect.controller;

import com.hemoconnect.dto.ContactMessageCreateDto;
import com.hemoconnect.dto.ContactMessageResponseDto;
import com.hemoconnect.dto.UpdateContactStatusDto;
import com.hemoconnect.entity.ContactStatus;
import com.hemoconnect.service.ContactMessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * POST here is the one endpoint in the WHOLE app (besides /api/auth/**)
 * that doesn't require being logged in - see SecurityConfig for the exact
 * rule. Reading and managing messages is admin-only, same as everything
 * under /api/admin/**.
 */
@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactMessageService contactMessageService;

    public ContactController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    /** POST /api/contact - public, no login required. */
    @PostMapping
    public ResponseEntity<ContactMessageResponseDto> submit(
            @Valid @RequestBody ContactMessageCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactMessageService.submit(dto));
    }

    /** GET /api/contact?status=NEW - admin only; status filter is optional. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContactMessageResponseDto>> listAll(
            @RequestParam(required = false) ContactStatus status) {
        return ResponseEntity.ok(contactMessageService.listAll(status));
    }

    /** GET /api/contact/{id} - admin only. */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactMessageResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contactMessageService.getById(id));
    }

    /** PATCH /api/contact/{id}/status - admin only. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactMessageResponseDto> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateContactStatusDto dto) {
        return ResponseEntity.ok(contactMessageService.updateStatus(id, dto.getStatus()));
    }
}
