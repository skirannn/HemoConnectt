package com.hemoconnect.service;

import com.hemoconnect.dto.MatchedDonorDto;
import com.hemoconnect.entity.BloodGroup;
import com.hemoconnect.entity.BloodRequest;
import com.hemoconnect.entity.DonorProfile;
import com.hemoconnect.entity.RequestStatus;
import com.hemoconnect.entity.Role;
import com.hemoconnect.entity.UrgencyLevel;
import com.hemoconnect.entity.User;
import com.hemoconnect.exception.ResourceNotFoundException;
import com.hemoconnect.repository.BloodRequestRepository;
import com.hemoconnect.repository.DonorProfileRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Finds candidate donors for a blood request.
 *
 * All the "hard" pieces this module needs already exist from earlier
 * modules — this service is mostly a focused query on top of them:
 *   - eligibility & availability      -> Module 3 (DonorProfile)
 *   - the request's own blood group   -> Module 4 (BloodRequest)
 *   - blood group compatibility rules -> BloodCompatibility (this module)
 *
 * Matching logic stays in this service layer, per the project brief -
 * never in the controller or the repository.
 */
@Service
public class DonorMatchingService {

    private final BloodRequestRepository bloodRequestRepository;
    private final DonorProfileRepository donorProfileRepository;

    public DonorMatchingService(
            BloodRequestRepository bloodRequestRepository,
            DonorProfileRepository donorProfileRepository) {
        this.bloodRequestRepository = bloodRequestRepository;
        this.donorProfileRepository = donorProfileRepository;
    }

    /**
     * Finds every eligible, available donor who is a medically compatible
     * match for the given request's blood group and location.
     *
     * Three filters, applied in this order:
     *   1. Blood group compatibility (via BloodCompatibility)
     *   2. Eligible + available + location match (the repository query)
     *   3. "Emergency only" donors are excluded UNLESS the request is CRITICAL
     */
    public List<MatchedDonorDto> findMatches(Long bloodRequestId, User caller) {
        BloodRequest request = bloodRequestRepository.findById(bloodRequestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Blood request not found with id: " + bloodRequestId));

        boolean isOwner = request.getRequester().getId().equals(caller.getId());
        boolean isAdmin = caller.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException(
                    "Only the requester or an admin can view matches for this request");
        }

        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.MATCHED) {
            // No point matching donors against a request that's already
            // confirmed, fulfilled, cancelled, or expired.
            return List.of();
        }

        Set<BloodGroup> compatibleGroups = BloodCompatibility.compatibleDonorGroups(request.getBloodGroup());

        List<DonorProfile> candidates =
                donorProfileRepository.findEligibleMatches(compatibleGroups, request.getLocation());

        boolean isCritical = request.getUrgency() == UrgencyLevel.CRITICAL;

        return candidates.stream()
                .filter(profile -> isCritical || !profile.isEmergencyOnly())
                .map(MatchedDonorDto::fromDonorProfile)
                .toList();
    }
}
