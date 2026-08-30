package com.hemoconnect.service;

import com.hemoconnect.dto.ConfirmDonorDto;
import com.hemoconnect.dto.RespondToRequestDto;
import com.hemoconnect.entity.*;
import com.hemoconnect.repository.BloodRequestRepository;
import com.hemoconnect.repository.DonorResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloodRequestServiceTest {

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

    private User requester;
    private User donor;
    private BloodRequest pendingRequest;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);
        requester.setRole(Role.RECIPIENT);

        donor = new User();
        donor.setId(2L);
        donor.setRole(Role.DONOR);

        pendingRequest = new BloodRequest();
        pendingRequest.setRequester(requester);
        pendingRequest.setStatus(RequestStatus.PENDING);
        pendingRequest.setExpiresAt(LocalDateTime.now().plusDays(30)); // not expired

        when(bloodRequestRepository.save(any(BloodRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void respondToRequest_withAccept_flipsStatusFromPendingToMatched() {
        when(bloodRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(donorResponseRepository.existsByBloodRequestIdAndDonorId(10L, 2L)).thenReturn(false);
        when(userService.getUserEntityById(2L)).thenReturn(donor);

        RespondToRequestDto dto = new RespondToRequestDto();
        dto.setResponseType(ResponseType.ACCEPT);

        var result = bloodRequestService.respondToRequest(10L, 2L, dto);

        assertThat(result.getStatus()).isEqualTo(RequestStatus.MATCHED);
        assertThat(result.getResponses()).hasSize(1);
    }

    @Test
    void respondToRequest_withDecline_leavesStatusPending() {
        when(bloodRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(donorResponseRepository.existsByBloodRequestIdAndDonorId(10L, 2L)).thenReturn(false);
        when(userService.getUserEntityById(2L)).thenReturn(donor);

        RespondToRequestDto dto = new RespondToRequestDto();
        dto.setResponseType(ResponseType.DECLINE);

        var result = bloodRequestService.respondToRequest(10L, 2L, dto);

        assertThat(result.getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    void respondToRequest_throwsIllegalArgumentException_whenDonorAlreadyResponded() {
        when(bloodRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(donorResponseRepository.existsByBloodRequestIdAndDonorId(10L, 2L)).thenReturn(true);

        RespondToRequestDto dto = new RespondToRequestDto();
        dto.setResponseType(ResponseType.ACCEPT);

        assertThatThrownBy(() -> bloodRequestService.respondToRequest(10L, 2L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already responded");
    }

    @Test
    void cancelRequest_throwsAccessDeniedException_whenCallerIsNotRequesterOrAdmin() {
        when(bloodRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));

        User stranger = new User();
        stranger.setId(99L);
        stranger.setRole(Role.DONOR);
        when(userService.getUserEntityById(99L)).thenReturn(stranger);

        assertThatThrownBy(() -> bloodRequestService.cancelRequest(10L, 99L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cancelRequest_succeeds_whenCallerIsTheRequester() {
        when(bloodRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(userService.getUserEntityById(1L)).thenReturn(requester);

        var result = bloodRequestService.cancelRequest(10L, 1L);

        assertThat(result.getStatus()).isEqualTo(RequestStatus.CANCELLED);
    }

    @Test
    void getRequestById_flipsToExpired_whenPastThe30DayWindow() {
        pendingRequest.setExpiresAt(LocalDateTime.now().minusDays(1)); // already past expiry
        when(bloodRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));

        var result = bloodRequestService.getRequestById(10L);

        assertThat(result.getStatus()).isEqualTo(RequestStatus.EXPIRED);
    }
}
