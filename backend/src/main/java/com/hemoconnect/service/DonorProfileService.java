package com.hemoconnect.service;

import com.hemoconnect.dto.DonorProfileRequestDto;
import com.hemoconnect.dto.DonorProfileResponseDto;
import com.hemoconnect.dto.RecordDonationRequestDto;
import com.hemoconnect.entity.DonorProfile;
import com.hemoconnect.entity.Role;
import com.hemoconnect.entity.User;
import com.hemoconnect.exception.ResourceNotFoundException;
import com.hemoconnect.repository.DonorProfileRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * All donor-specific business logic, most importantly the 56-day
 * re-donation eligibility rule carried over from the original project's
 * DonorBlood.js pre-save hook.
 */
@Service
public class DonorProfileService {

    private final DonorProfileRepository donorProfileRepository;
    private final UserService userService;

    public DonorProfileService(DonorProfileRepository donorProfileRepository, UserService userService) {
        this.donorProfileRepository = donorProfileRepository;
        this.userService = userService;
    }

    /**
     * Creates the donor profile the first time, or updates it on every
     * later call ("upsert"). Only makes sense for users with role DONOR -
     * enforced here with a clear error message rather than silently
     * letting a recipient/admin end up with one.
     */
    public DonorProfileResponseDto createOrUpdateProfile(Long userId, DonorProfileRequestDto request) {
        User user = userService.getUserEntityById(userId);
        if (user.getRole() != Role.DONOR) {
            throw new IllegalArgumentException("Only users with role DONOR can have a donor profile");
        }

        DonorProfile profile = donorProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    DonorProfile newProfile = new DonorProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setAge(request.getAge());
        profile.setWeight(request.getWeight());
        profile.setHeight(request.getHeight());
        profile.setGender(request.getGender());
        profile.setMaxDistanceKm(request.getMaxDistanceKm());
        profile.setEmergencyOnly(request.isEmergencyOnly());

        DonorProfile saved = donorProfileRepository.save(profile);
        return DonorProfileResponseDto.fromEntity(saved);
    }

    public DonorProfileResponseDto getProfileByUserId(Long userId) {
        DonorProfile profile = findProfileOrThrow(userId);
        return DonorProfileResponseDto.fromEntity(profile);
    }

    /**
     * Records a completed donation: bumps the donation counters, stamps
     * today (or the given date) as the last donation date, and
     * recalculates eligibility from the 56-day cooldown rule.
     *
     * This is the direct equivalent of the original DonorBlood model's
     * Mongoose pre-save hook, just written as an explicit method instead
     * of "magic" that runs on every save.
     */
    public DonorProfileResponseDto recordDonation(Long userId, RecordDonationRequestDto request) {
        DonorProfile profile = findProfileOrThrow(userId);

        LocalDate donationDate = request.getDonationDate() != null
                ? request.getDonationDate()
                : LocalDate.now();

        profile.setLastDonationDate(donationDate);
        profile.setTotalDonations(profile.getTotalDonations() + 1);
        profile.setTotalUnitsDonated(profile.getTotalUnitsDonated() + request.getUnits());

        recalculateEligibility(profile);

        DonorProfile saved = donorProfileRepository.save(profile);
        return DonorProfileResponseDto.fromEntity(saved);
    }

    /**
     * The eligibility rule itself:
     *   nextEligibleDate = lastDonationDate + 56 days
     *   eligible = today is on/after nextEligibleDate
     *
     * A donor who has never donated (lastDonationDate == null) is always
     * eligible - there's nothing to cool down from yet.
     */
    private void recalculateEligibility(DonorProfile profile) {
        if (profile.getLastDonationDate() == null) {
            profile.setEligible(true);
            profile.setNextEligibleDate(null);
            return;
        }

        LocalDate nextEligibleDate = profile.getLastDonationDate()
                .plusDays(DonorProfile.DONATION_COOLDOWN_DAYS);

        profile.setNextEligibleDate(nextEligibleDate);
        profile.setEligible(!LocalDate.now().isBefore(nextEligibleDate)); // today >= nextEligibleDate
    }

    private DonorProfile findProfileOrThrow(Long userId) {
        return donorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No donor profile found for user id: " + userId + ". Create one first."));
    }
}
