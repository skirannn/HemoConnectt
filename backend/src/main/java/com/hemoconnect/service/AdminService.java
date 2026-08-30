package com.hemoconnect.service;

import com.hemoconnect.dto.AdminStatsDto;
import com.hemoconnect.dto.BloodRequestResponseDto;
import com.hemoconnect.dto.DonorProfileResponseDto;
import com.hemoconnect.dto.UserResponseDto;
import com.hemoconnect.entity.ContactStatus;
import com.hemoconnect.entity.RequestStatus;
import com.hemoconnect.entity.Role;
import com.hemoconnect.repository.BloodRequestRepository;
import com.hemoconnect.repository.ContactMessageRepository;
import com.hemoconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The admin dashboard's backing service. Unlike other services in this
 * app, AdminService doesn't own a single entity - it COORDINATES several
 * other services (and, for simple counts, a couple of repositories
 * directly) to answer "give me an overview of the whole system."
 *
 * Every method here is only reachable through AdminController, which is
 * locked down at the class level with @PreAuthorize("hasRole('ADMIN')") -
 * see that class for why a class-level check is safe here specifically.
 */
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final UserService userService;
    private final DonorProfileService donorProfileService;
    private final BloodRequestService bloodRequestService;

    public AdminService(
            UserRepository userRepository,
            BloodRequestRepository bloodRequestRepository,
            ContactMessageRepository contactMessageRepository,
            UserService userService,
            DonorProfileService donorProfileService,
            BloodRequestService bloodRequestService) {
        this.userRepository = userRepository;
        this.bloodRequestRepository = bloodRequestRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.userService = userService;
        this.donorProfileService = donorProfileService;
        this.bloodRequestService = bloodRequestService;
    }

    /**
     * Real counts only - no placeholder/mock numbers. This directly fixes
     * a gap in the original project, whose admin stats endpoint hardcoded
     * a "completedDonations" number instead of actually querying for it.
     */
    public AdminStatsDto getStatistics() {
        AdminStatsDto stats = new AdminStatsDto();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalDonors(userRepository.countByRole(Role.DONOR));
        stats.setTotalRecipients(userRepository.countByRole(Role.RECIPIENT));
        stats.setTotalAdmins(userRepository.countByRole(Role.ADMIN));
        stats.setActiveRequests(
                bloodRequestRepository.countByStatusIn(List.of(RequestStatus.PENDING, RequestStatus.MATCHED)));
        stats.setFulfilledRequests(bloodRequestRepository.countByStatus(RequestStatus.FULFILLED));
        stats.setFlaggedRequests(bloodRequestRepository.countByFlaggedTrue());
        stats.setNewContactMessages(contactMessageRepository.countByStatus(ContactStatus.NEW));
        return stats;
    }

    public List<UserResponseDto> listRecentUsers() {
        return userService.getAllUsers(); // already returns safe DTOs (no password field)
    }

    public List<DonorProfileResponseDto> listAllDonors() {
        return donorProfileService.getAllProfiles();
    }

    public List<BloodRequestResponseDto> listAllRequests() {
        return bloodRequestService.listAllRequests();
    }

    public List<BloodRequestResponseDto> listFlaggedRequests() {
        return bloodRequestService.listFlaggedRequests();
    }

    public BloodRequestResponseDto flagRequest(Long requestId, String reason) {
        return bloodRequestService.flagRequest(requestId, reason);
    }

    public BloodRequestResponseDto approveFlaggedRequest(Long requestId) {
        return bloodRequestService.approveFlaggedRequest(requestId);
    }

    public BloodRequestResponseDto rejectFlaggedRequest(Long requestId, String reason) {
        return bloodRequestService.rejectFlaggedRequest(requestId, reason);
    }

    public void deleteUser(Long userId) {
        userService.deleteUser(userId);
    }
}
