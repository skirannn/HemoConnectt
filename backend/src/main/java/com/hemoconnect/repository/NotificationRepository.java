package com.hemoconnect.repository;

import com.hemoconnect.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Generates: SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Generates: SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = false
    long countByUserIdAndReadFalse(Long userId);

    // Used by markAllAsRead - fetches only the unread ones instead of every notification.
    List<Notification> findByUserIdAndReadFalse(Long userId);
}
