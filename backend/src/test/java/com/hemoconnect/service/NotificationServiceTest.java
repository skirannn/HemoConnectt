package com.hemoconnect.service;

import com.hemoconnect.entity.Notification;
import com.hemoconnect.entity.NotificationType;
import com.hemoconnect.entity.User;
import com.hemoconnect.exception.ResourceNotFoundException;
import com.hemoconnect.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setId(1L);

        notification = new Notification();
        notification.setUser(owner);
        notification.setType(NotificationType.DONOR_RESPONSE);
        notification.setTitle("Test");
        notification.setMessage("Test message");

        
    }

    @Test
    void markAsRead_setsReadTrue_whenCallerOwnsTheNotification() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

       when(notificationRepository.save(any(Notification.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

        var result = notificationService.markAsRead(10L, 1L);

        assertThat(result.isRead()).isTrue();
    }

    @Test
    void markAsRead_throwsAccessDeniedException_whenCallerDoesNotOwnTheNotification() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(10L, 999L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void markAsRead_throwsResourceNotFoundException_whenNotificationDoesNotExist() {
        when(notificationRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(404L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
