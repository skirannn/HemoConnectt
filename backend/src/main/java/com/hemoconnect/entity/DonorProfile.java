package com.hemoconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Extra, donor-only data that doesn't belong on every User (a recipient or
 * admin will never have a weight, a last-donation-date, etc). This is the
 * Java/JPA equivalent of the original server/models/DonorBlood.js
 * collection — one row per donor, linked 1:1 to a User.
 *
 * Note: the original DonorBlood also stored a separate `rhFactor` field.
 * We don't repeat that here — User.bloodGroup (e.g. O_POSITIVE) already
 * encodes the Rh factor, so a second field would just be duplicate data
 * that could drift out of sync. One less thing to keep consistent.
 */
@Entity
@Table(name = "donor_profiles")
public class DonorProfile {

    /**
     * The most important business rule carried over from the original
     * project: after donating, a donor is not eligible to donate again
     * for 56 days (a real, medically-motivated blood donation rule).
     */
    public static final int DONATION_COOLDOWN_DAYS = 56;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @OneToOne + @JoinColumn(unique = true) - each User has AT MOST one
     * DonorProfile, and each DonorProfile belongs to exactly one User.
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;

    /** Recalculated automatically - see DonorProfileService.recalculateEligibility(). */
    @Column(name = "next_eligible_date")
    private LocalDate nextEligibleDate;

    @Column(name = "is_eligible")
    private boolean eligible = true; // true until a first donation is recorded

    @Column(name = "total_donations")
    private int totalDonations = 0;

    @Column(name = "total_units_donated")
    private int totalUnitsDonated = 0;

    private Integer age;
    private Double weight; // kilograms
    private Double height; // centimeters

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "max_distance_km")
    private Integer maxDistanceKm;

    /** If true, this donor only wants to be matched for CRITICAL urgency requests. */
    @Column(name = "emergency_only")
    private boolean emergencyOnly = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DonorProfile() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ----- Getters and setters -----

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(LocalDate lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public LocalDate getNextEligibleDate() {
        return nextEligibleDate;
    }

    public void setNextEligibleDate(LocalDate nextEligibleDate) {
        this.nextEligibleDate = nextEligibleDate;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public int getTotalDonations() {
        return totalDonations;
    }

    public void setTotalDonations(int totalDonations) {
        this.totalDonations = totalDonations;
    }

    public int getTotalUnitsDonated() {
        return totalUnitsDonated;
    }

    public void setTotalUnitsDonated(int totalUnitsDonated) {
        this.totalUnitsDonated = totalUnitsDonated;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Integer getMaxDistanceKm() {
        return maxDistanceKm;
    }

    public void setMaxDistanceKm(Integer maxDistanceKm) {
        this.maxDistanceKm = maxDistanceKm;
    }

    public boolean isEmergencyOnly() {
        return emergencyOnly;
    }

    public void setEmergencyOnly(boolean emergencyOnly) {
        this.emergencyOnly = emergencyOnly;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
