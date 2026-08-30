package com.hemoconnect.service;

import com.hemoconnect.dto.NotificationResponseDto;
import com.hemoconnect.entity.Notification;
import com.hemoconnect.entity.NotificationType;
import com.hemoconnect.entity.User;
import com.hemoconnect.exception.ResourceNotFoundException;
import com.hemoconnect.repository.NotificationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Creates a notification for a user. This is called from OTHER
     * services (BloodRequestService, in Module 6's integration) rather
     * than directly from a controller - there's no "create a notification"
     * button anywhere in the app; notifications are always a side-effect
     * of something else happening.
     */
    public void notify(User recipient, NotificationType type, String title, String message) {
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    /** The logged-in user's own notifications, most recent first. */
    public List<NotificationResponseDto> listForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponseDto::fromEntity)
                .toList();
    }

    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    /** Marks one notification as read - only if it actually belongs to the caller. */
    public NotificationResponseDto markAsRead(Long notificationId, Long callerId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId));

        if (!notification.getUser().getId().equals(callerId)) {
            throw new AccessDeniedException("You can only mark your own notifications as read");
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return NotificationResponseDto.fromEntity(saved);
    }

    /** Marks every unread notification for this user as read in one go. */
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
