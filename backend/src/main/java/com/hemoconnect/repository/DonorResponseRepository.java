package com.hemoconnect.repository;

import com.hemoconnect.entity.DonorResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonorResponseRepository extends JpaRepository<DonorResponse, Long> {

    // Used to stop a donor from responding to the same request twice.
    boolean existsByBloodRequestIdAndDonorId(Long bloodRequestId, Long donorId);
}
