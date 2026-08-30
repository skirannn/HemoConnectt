package com.hemoconnect.repository;

import com.hemoconnect.entity.DonorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DonorProfileRepository extends JpaRepository<DonorProfile, Long> {

    // Generates: SELECT * FROM donor_profiles WHERE user_id = ?
    Optional<DonorProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    // Note: queries that filter by eligibility + blood group + availability
    // together (for donor matching) belong to Module 5, not here - this
    // repository only knows about a single donor's own profile.
}
