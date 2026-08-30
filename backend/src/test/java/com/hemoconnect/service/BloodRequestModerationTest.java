package com.hemoconnect.service;

import com.hemoconnect.entity.BloodRequest;
import com.hemoconnect.entity.RequestStatus;
import com.hemoconnect.entity.Role;
import com.hemoconnect.entity.User;
import com.hemoconnect.repository.BloodRequestRepository;
import com.hemoconnect.repository.DonorResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloodRequestModerationTest {

    @Mock
    private BloodRequestRepository bloodRequestRepository;
    @Mock
    private DonorResponseRepository donorResponseRepository;
    @Mock
    private UserService userService;
    @Mock
    private DonorProfileService donorProfileService;
    @Mock
    private DonorMatchingService donorMatchingService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BloodRequestService bloodRequestService;

    private BloodRequest request;

    @BeforeEach
    void setUp() {
        User requester = new User();
        requester.setId(1L);
        requester.setRole(Role.RECIPIENT);

        request = new BloodRequest();
        request.setRequester(requester);
        request.setStatus(RequestStatus.PENDING);
        request.setExpiresAt(LocalDateTime.now().plusDays(30));

        when(bloodRequestRepository.save(any(BloodRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void flagRequest_setsFlaggedTrueWithReason() {
        when(bloodRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        var result = bloodRequestService.flagRequest(1L, "Suspicious details");

        assertThat(result.isFlagged()).isTrue();
        assertThat(result.getFlagReason()).isEqualTo("Suspicious details");
    }

    @Test
    void approveFlaggedRequest_clearsFlagButKeepsStatus() {
        request.setFlagged(true);
        request.setFlagReason("Suspicious details");
        when(bloodRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        var result = bloodRequestService.approveFlaggedRequest(1L);

        assertThat(result.isFlagged()).isFalse();
        assertThat(result.getFlagReason()).isNull();
        assertThat(result.getStatus()).isEqualTo(RequestStatus.PENDING); // untouched
    }

    @Test
    void rejectFlaggedRequest_cancelsAndClearsFlag() {
        request.setFlagged(true);
        request.setFlagReason("Suspicious details");
        when(bloodRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        var result = bloodRequestService.rejectFlaggedRequest(1L, "Confirmed fraudulent");

        assertThat(result.getStatus()).isEqualTo(RequestStatus.CANCELLED);
        assertThat(result.isFlagged()).isFalse();
    }

    @Test
    void rejectFlaggedRequest_throwsIllegalArgumentException_whenNotFlagged() {
        when(bloodRequestRepository.findById(1L)).thenReturn(Optional.of(request)); // flagged = false

        assertThatThrownBy(() -> bloodRequestService.rejectFlaggedRequest(1L, "reason"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("flagged");
    }
}
