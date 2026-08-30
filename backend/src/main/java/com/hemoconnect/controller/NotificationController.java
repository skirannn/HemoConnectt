package com.hemoconnect.controller;

import com.hemoconnect.dto.NotificationResponseDto;
import com.hemoconnect.security.UserPrincipal;
import com.hemoconnect.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Every endpoint here always operates on the LOGGED-IN user's own
 * notifications - there's no {userId} path variable, unlike
 * DonorController. Notifications are inherently personal, so we just read
 * "who am I" from the JWT (@AuthenticationPrincipal) instead of trusting
 * a value the client could put in the URL.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** GET /api/notifications - my notifications, most recent first. */
    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(notificationService.listForUser(principal.getUser().getId()));
    }

    /** GET /api/notifications/unread-count - for a notification bell badge. */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        long count = notificationService.countUnread(principal.getUser().getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /** PATCH /api/notifications/{id}/read - mark one notification as read. */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDto> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(notificationService.markAsRead(id, principal.getUser().getId()));
    }

    /** PATCH /api/notifications/read-all - mark every notification as read. */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
