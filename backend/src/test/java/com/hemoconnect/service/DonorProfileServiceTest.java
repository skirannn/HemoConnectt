package com.hemoconnect.service;

import com.hemoconnect.dto.RecordDonationRequestDto;
import com.hemoconnect.entity.DonorProfile;
import com.hemoconnect.entity.Role;
import com.hemoconnect.entity.User;
import com.hemoconnect.exception.ResourceNotFoundException;
import com.hemoconnect.repository.DonorProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests focused on the most important rule in this module: a donor
 * becomes eligible again exactly 56 days after their last donation.
 */
@ExtendWith(MockitoExtension.class)
class DonorProfileServiceTest {

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DonorProfileService donorProfileService;

    private DonorProfile existingProfile;

    @BeforeEach
    void setUp() {
        User donor = new User();
        donor.setId(1L);
        donor.setRole(Role.DONOR);

        existingProfile = new DonorProfile();
        existingProfile.setUser(donor);

        when(donorProfileRepository.save(any(DonorProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void recordDonation_setsEligibleFalse_immediatelyAfterDonating() {
        when(donorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));

        RecordDonationRequestDto request = new RecordDonationRequestDto();
        request.setUnits(1);
        request.setDonationDate(LocalDate.now());

        var result = donorProfileService.recordDonation(1L, request);

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getNextEligibleDate()).isEqualTo(LocalDate.now().plusDays(56));
        assertThat(result.getTotalDonations()).isEqualTo(1);
    }

    @Test
    void recordDonation_setsEligibleTrue_whenLastDonationWasOver56DaysAgo() {
        when(donorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));

        RecordDonationRequestDto request = new RecordDonationRequestDto();
        request.setUnits(1);
        request.setDonationDate(LocalDate.now().minusDays(60)); // well past the cooldown

        var result = donorProfileService.recordDonation(1L, request);

        assertThat(result.isEligible()).isTrue();
    }

    @Test
    void recordDonation_setsEligibleFalse_onDay55() {
        when(donorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));

        RecordDonationRequestDto request = new RecordDonationRequestDto();
        request.setUnits(1);
        request.setDonationDate(LocalDate.now().minusDays(55)); // one day short of eligible

        var result = donorProfileService.recordDonation(1L, request);

        assertThat(result.isEligible()).isFalse();
    }

    @Test
    void recordDonation_setsEligibleTrue_exactlyOnDay56() {
        when(donorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));

        RecordDonationRequestDto request = new RecordDonationRequestDto();
        request.setUnits(1);
        request.setDonationDate(LocalDate.now().minusDays(56)); // exactly the cooldown boundary

        var result = donorProfileService.recordDonation(1L, request);

        assertThat(result.isEligible()).isTrue();
    }

    @Test
    void recordDonation_throwsResourceNotFoundException_whenNoProfileExists() {
        when(donorProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        RecordDonationRequestDto request = new RecordDonationRequestDto();
        request.setUnits(1);

        assertThatThrownBy(() -> donorProfileService.recordDonation(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
