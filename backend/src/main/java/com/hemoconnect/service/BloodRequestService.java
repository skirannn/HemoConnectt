package com.hemoconnect.service;

import com.hemoconnect.dto.*;
import com.hemoconnect.entity.*;
import com.hemoconnect.exception.ResourceNotFoundException;
import com.hemoconnect.repository.BloodRequestRepository;
import com.hemoconnect.repository.DonorResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * All the business logic for a blood request's life cycle - the direct
 * equivalent of the original server/routes/blood-requests.js, split into
 * Controller (HTTP) vs Service (rules) like every other module.
 */
@Service
public class BloodRequestService {

    private static final Logger log = LoggerFactory.getLogger(BloodRequestService.class);

    private final BloodRequestRepository bloodRequestRepository;
    private final DonorResponseRepository donorResponseRepository;
    private final UserService userService;
    private final DonorProfileService donorProfileService;

    public BloodRequestService(
            BloodRequestRepository bloodRequestRepository,
            DonorResponseRepository donorResponseRepository,
            UserService userService,
            DonorProfileService donorProfileService) {
        this.bloodRequestRepository = bloodRequestRepository;
        this.donorResponseRepository = donorResponseRepository;
        this.userService = userService;
        this.donorProfileService = donorProfileService;
    }

    /** Only a RECIPIENT can create a blood request. */
    public BloodRequestResponseDto createRequest(Long requesterId, BloodRequestCreateDto dto) {
        User requester = userService.getUserEntityById(requesterId);
        if (requester.getRole() != Role.RECIPIENT) {
            throw new IllegalArgumentException("Only recipients can create a blood request");
        }

        BloodRequest request = new BloodRequest();
        request.setRequester(requester);
        request.setBloodGroup(dto.getBloodGroup());
        request.setUnitsRequired(dto.getUnitsRequired());
        request.setHospital(dto.getHospital());
        request.setLocation(dto.getLocation());
        request.setUrgency(dto.getUrgency());
        request.setRequiredDate(dto.getRequiredDate());
        request.setDescription(dto.getDescription());
        // status defaults to PENDING and expiresAt is set automatically (see @PrePersist)

        BloodRequest saved = bloodRequestRepository.save(request);
        return BloodRequestResponseDto.fromEntity(saved);
    }

    public BloodRequestResponseDto getRequestById(Long id) {
        BloodRequest request = findRequestOrThrow(id);
        markExpiredIfNeeded(request);
        return BloodRequestResponseDto.fromEntity(request);
    }

    /** Every request that's still open for donor responses (PENDING or MATCHED, not expired). */
    public List<BloodRequestResponseDto> listActiveRequests() {
        List<BloodRequest> candidates = bloodRequestRepository.findByStatusInOrderByCreatedAtDesc(
                List.of(RequestStatus.PENDING, RequestStatus.MATCHED));

        return candidates.stream()
                .map(this::markExpiredIfNeeded)
                .filter(r -> r.getStatus() != RequestStatus.EXPIRED) // freshly-expired ones drop out of "active"
                .map(BloodRequestResponseDto::fromEntity)
                .toList();
    }

    /** A recipient's own request history. */
    public List<BloodRequestResponseDto> listMyRequests(Long requesterId) {
        return bloodRequestRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId)
                .stream()
                .map(this::markExpiredIfNeeded)
                .map(BloodRequestResponseDto::fromEntity)
                .toList();
    }

    /**
     * A donor accepts, declines, or says "maybe" to a request.
     * The original project's addResponse() business rule is preserved
     * exactly: an ACCEPT on a still-PENDING request flips it to MATCHED.
     */
    public BloodRequestResponseDto respondToRequest(Long requestId, Long donorId, RespondToRequestDto dto) {
        BloodRequest request = findRequestOrThrow(requestId);
        markExpiredIfNeeded(request);

        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.MATCHED) {
            throw new IllegalArgumentException("This request is no longer open for responses");
        }
        if (donorResponseRepository.existsByBloodRequestIdAndDonorId(requestId, donorId)) {
            throw new IllegalArgumentException("You have already responded to this request");
        }

        User donor = userService.getUserEntityById(donorId);

        DonorResponse response = new DonorResponse();
        response.setBloodRequest(request);
        response.setDonor(donor);
        response.setResponseType(dto.getResponseType());
        response.setResponseMessage(dto.getResponseMessage());
        request.getResponses().add(response); // cascade = ALL saves it when we save the request

        if (dto.getResponseType() == ResponseType.ACCEPT && request.getStatus() == RequestStatus.PENDING) {
            request.setStatus(RequestStatus.MATCHED);
        }

        BloodRequest saved = bloodRequestRepository.save(request);
        return BloodRequestResponseDto.fromEntity(saved);
    }

    /** The requester picks exactly one donor (from those who accepted) to confirm. */
    public BloodRequestResponseDto confirmDonor(Long requestId, Long callerId, ConfirmDonorDto dto) {
        BloodRequest request = findRequestOrThrow(requestId);
        assertRequesterOrAdmin(request, callerId);

        if (request.getStatus() != RequestStatus.MATCHED) {
            throw new IllegalArgumentException("A donor can only be confirmed once the request is MATCHED");
        }

        boolean donorDidAccept = request.getResponses().stream()
                .anyMatch(r -> r.getDonor().getId().equals(dto.getDonorId())
                        && r.getResponseType() == ResponseType.ACCEPT);
        if (!donorDidAccept) {
            throw new IllegalArgumentException("That donor has not accepted this request");
        }

        User donor = userService.getUserEntityById(dto.getDonorId());
        request.setConfirmedDonor(donor);
        request.setStatus(RequestStatus.CONFIRMED);

        BloodRequest saved = bloodRequestRepository.save(request);
        return BloodRequestResponseDto.fromEntity(saved);
    }

    /**
     * Marks a CONFIRMED request as FULFILLED, and records the donation
     * against the confirmed donor's profile (Module 3). This is a good
     * example of one service calling another: BloodRequestService doesn't
     * know HOW eligibility/donation counts work, it just asks
     * DonorProfileService to record the donation.
     */
    public BloodRequestResponseDto fulfillRequest(Long requestId, Long callerId) {
        BloodRequest request = findRequestOrThrow(requestId);
        assertRequesterOrAdmin(request, callerId);

        if (request.getStatus() != RequestStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only a CONFIRMED request can be marked fulfilled");
        }

        request.setStatus(RequestStatus.FULFILLED);
        BloodRequest saved = bloodRequestRepository.save(request);

        try {
            RecordDonationRequestDto donationDto = new RecordDonationRequestDto();
            donationDto.setUnits(request.getUnitsRequired());
            donationDto.setDonationDate(LocalDate.now());
            donorProfileService.recordDonation(request.getConfirmedDonor().getId(), donationDto);
        } catch (ResourceNotFoundException ex) {
            // The confirmed donor never finished setting up their donor
            // profile (Module 3). We still fulfill the request - we just
            // can't update stats/eligibility we have no profile to update.
            log.warn("Could not record donation stats for donor {}: {}",
                    request.getConfirmedDonor().getId(), ex.getMessage());
        }

        return BloodRequestResponseDto.fromEntity(saved);
    }

    public BloodRequestResponseDto cancelRequest(Long requestId, Long callerId) {
        BloodRequest request = findRequestOrThrow(requestId);
        assertRequesterOrAdmin(request, callerId);

        if (request.getStatus() == RequestStatus.FULFILLED) {
            throw new IllegalArgumentException("A fulfilled request can't be cancelled");
        }

        request.setStatus(RequestStatus.CANCELLED);
        BloodRequest saved = bloodRequestRepository.save(request);
        return BloodRequestResponseDto.fromEntity(saved);
    }

    // ----- Private helpers -----

    private BloodRequest findRequestOrThrow(Long id) {
        return bloodRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with id: " + id));
    }

    /**
     * "Lazy" expiry: rather than running a background job constantly, we
     * just check "has this request's 30-day window passed?" every time we
     * touch it, and flip it to EXPIRED right then if so. See
     * ExpiredRequestScheduler for the (optional) background version of
     * this same idea.
     */
    private BloodRequest markExpiredIfNeeded(BloodRequest request) {
        boolean stillOpen = request.getStatus() == RequestStatus.PENDING
                || request.getStatus() == RequestStatus.MATCHED;
        if (stillOpen && request.isExpired()) {
            request.setStatus(RequestStatus.EXPIRED);
            return bloodRequestRepository.save(request);
        }
        return request;
    }

    private void assertRequesterOrAdmin(BloodRequest request, Long callerId) {
        User caller = userService.getUserEntityById(callerId);
        boolean isOwner = request.getRequester().getId().equals(callerId);
        boolean isAdmin = caller.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Only the requester or an admin can do this");
        }
    }
}
