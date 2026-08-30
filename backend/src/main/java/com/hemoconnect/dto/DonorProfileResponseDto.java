package com.hemoconnect.dto;

import com.hemoconnect.entity.BloodGroup;
import com.hemoconnect.entity.DonorProfile;
import com.hemoconnect.entity.Gender;

import java.time.LocalDate;

/**
 * What we send back for a donor profile. Combines a few read-only fields
 * from User (name, bloodGroup, location) with the donor-specific fields
 * from DonorProfile, so the frontend doesn't have to make two separate
 * calls just to show one donor's card.
 */
public class DonorProfileResponseDto {

    private Long userId;
    private String name;
    private BloodGroup bloodGroup;
    private String location;
    private boolean availableForDonation;

    private LocalDate lastDonationDate;
    private LocalDate nextEligibleDate;
    private boolean eligible;
    private int totalDonations;
    private int totalUnitsDonated;
    private Integer age;
    private Double weight;
    private Double height;
    private Gender gender;
    private Integer maxDistanceKm;
    private boolean emergencyOnly;

    public static DonorProfileResponseDto fromEntity(DonorProfile profile) {
        DonorProfileResponseDto dto = new DonorProfileResponseDto();
        dto.userId = profile.getUser().getId();
        dto.name = profile.getUser().getName();
        dto.bloodGroup = profile.getUser().getBloodGroup();
        dto.location = profile.getUser().getLocation();
        dto.availableForDonation = profile.getUser().isAvailableForDonation();

        dto.lastDonationDate = profile.getLastDonationDate();
        dto.nextEligibleDate = profile.getNextEligibleDate();
        dto.eligible = profile.isEligible();
        dto.totalDonations = profile.getTotalDonations();
        dto.totalUnitsDonated = profile.getTotalUnitsDonated();
        dto.age = profile.getAge();
        dto.weight = profile.getWeight();
        dto.height = profile.getHeight();
        dto.gender = profile.getGender();
        dto.maxDistanceKm = profile.getMaxDistanceKm();
        dto.emergencyOnly = profile.isEmergencyOnly();
        return dto;
    }

    // ----- Getters and setters -----

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isAvailableForDonation() {
        return availableForDonation;
    }

    public void setAvailableForDonation(boolean availableForDonation) {
        this.availableForDonation = availableForDonation;
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
}
