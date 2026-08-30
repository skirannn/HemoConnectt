package com.hemoconnect.repository;

import com.hemoconnect.entity.BloodRequest;
import com.hemoconnect.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    // Generates: SELECT * FROM blood_requests WHERE status IN (?, ?) ORDER BY created_at DESC
    List<BloodRequest> findByStatusInOrderByCreatedAtDesc(List<RequestStatus> statuses);

    // Generates: SELECT * FROM blood_requests WHERE requester_id = ? ORDER BY created_at DESC
    List<BloodRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    // Generates: SELECT * FROM blood_requests ORDER BY created_at DESC (Module 7: admin overview)
    List<BloodRequest> findAllByOrderByCreatedAtDesc();

    // Generates: SELECT * FROM blood_requests WHERE is_flagged = true ORDER BY created_at DESC
    List<BloodRequest> findByFlaggedTrueOrderByCreatedAtDesc();

    long countByStatusIn(List<RequestStatus> statuses);

    long countByStatus(RequestStatus status);

    long countByFlaggedTrue();
}
