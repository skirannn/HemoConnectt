package com.hemoconnect.repository;

import com.hemoconnect.entity.BloodGroup;
import com.hemoconnect.entity.DonorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DonorProfileRepository extends JpaRepository<DonorProfile, Long> {

    // Generates: SELECT * FROM donor_profiles WHERE user_id = ?
    Optional<DonorProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    /**
     * The core query for Module 5 (Donor Matching): every currently
     * eligible, available donor whose blood group is in the given
     * compatible set, and whose location contains the given text
     * (a simple case-insensitive substring match - the original project
     * used the same kind of plain text matching rather than real geo
     * distance calculations, which is a reasonable amount of complexity
     * for this app).
     */
    @Query("""
            SELECT dp FROM DonorProfile dp
            WHERE dp.eligible = true
              AND dp.user.availableForDonation = true
              AND dp.user.bloodGroup IN :bloodGroups
              AND LOWER(dp.user.location) LIKE LOWER(CONCAT('%', :location, '%'))
            """)
    List<DonorProfile> findEligibleMatches(
            @Param("bloodGroups") Set<BloodGroup> bloodGroups,
            @Param("location") String location);
}
