package com.hemoconnect.service;

import com.hemoconnect.entity.*;
import com.hemoconnect.repository.BloodRequestRepository;
import com.hemoconnect.repository.DonorProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonorMatchingServiceTest {

    @Mock
    private BloodRequestRepository bloodRequestRepository;
    @Mock
    private DonorProfileRepository donorProfileRepository;

    @InjectMocks
    private DonorMatchingService donorMatchingService;

    private User requester;
    private BloodRequest request;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);
        requester.setRole(Role.RECIPIENT);

        request = new BloodRequest();
        request.setRequester(requester);
        request.setBloodGroup(BloodGroup.A_POSITIVE);
        request.setLocation("Hyderabad");
        request.setUrgency(UrgencyLevel.MEDIUM);
        request.setStatus(RequestStatus.PENDING);
        request.setExpiresAt(LocalDateTime.now().plusDays(30));
    }

    @Test
    void findMatches_throwsAccessDenied_whenCallerIsNotRequesterOrAdmin() {
        when(bloodRequestRepository.findById(5L)).thenReturn(Optional.of(request));

        User stranger = new User();
        stranger.setId(99L);
        stranger.setRole(Role.DONOR);

        assertThatThrownBy(() -> donorMatchingService.findMatches(5L, stranger))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void findMatches_excludesEmergencyOnlyDonors_forNonCriticalRequests() {
        when(bloodRequestRepository.findById(5L)).thenReturn(Optional.of(request));

        User donorUser = new User();
        donorUser.setId(2L);
        donorUser.setName("Emergency Donor");
        donorUser.setBloodGroup(BloodGroup.O_NEGATIVE);
        donorUser.setLocation("Hyderabad");

        DonorProfile emergencyOnlyProfile = new DonorProfile();
        emergencyOnlyProfile.setUser(donorUser);
        emergencyOnlyProfile.setEmergencyOnly(true);

        when(donorProfileRepository.findEligibleMatches(any(Set.class), anyString()))
                .thenReturn(List.of(emergencyOnlyProfile));

        var results = donorMatchingService.findMatches(5L, requester);

        assertThat(results).isEmpty(); // filtered out: request is MEDIUM, not CRITICAL
    }

    @Test
    void findMatches_includesEmergencyOnlyDonors_forCriticalRequests() {
        request.setUrgency(UrgencyLevel.CRITICAL);
        when(bloodRequestRepository.findById(5L)).thenReturn(Optional.of(request));

        User donorUser = new User();
        donorUser.setId(2L);
        donorUser.setName("Emergency Donor");
        donorUser.setBloodGroup(BloodGroup.O_NEGATIVE);
        donorUser.setLocation("Hyderabad");

        DonorProfile emergencyOnlyProfile = new DonorProfile();
        emergencyOnlyProfile.setUser(donorUser);
        emergencyOnlyProfile.setEmergencyOnly(true);

        when(donorProfileRepository.findEligibleMatches(any(Set.class), anyString()))
                .thenReturn(List.of(emergencyOnlyProfile));

        var results = donorMatchingService.findMatches(5L, requester);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDonorName()).isEqualTo("Emergency Donor");
    }

    @Test
    void findMatches_returnsEmptyList_whenRequestIsAlreadyFulfilled() {
        request.setStatus(RequestStatus.FULFILLED);
        when(bloodRequestRepository.findById(5L)).thenReturn(Optional.of(request));

        var results = donorMatchingService.findMatches(5L, requester);

        assertThat(results).isEmpty();
    }
}
