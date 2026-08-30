package com.hemoconnect.dto;

import com.hemoconnect.entity.BloodGroup;
import com.hemoconnect.entity.DonorProfile;

import java.time.LocalDate;

/** A single donor returned as a match for a specific blood request. */
public class MatchedDonorDto {

    private Long donorId;
    private String donorName;
    private String phone;
    private BloodGroup bloodGroup;
    private String location;
    private LocalDate nextEligibleDate;
    private int totalDonations;

    public static MatchedDonorDto fromDonorProfile(DonorProfile profile) {
        MatchedDonorDto dto = new MatchedDonorDto();
        dto.donorId = profile.getUser().getId();
        dto.donorName = profile.getUser().getName();
        dto.phone = profile.getUser().getPhone();
        dto.bloodGroup = profile.getUser().getBloodGroup();
        dto.location = profile.getUser().getLocation();
        dto.nextEligibleDate = profile.getNextEligibleDate();
        dto.totalDonations = profile.getTotalDonations();
        return dto;
    }

    // ----- Getters and setters -----

    public Long getDonorId() {
        return donorId;
    }

    public void setDonorId(Long donorId) {
        this.donorId = donorId;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public LocalDate getNextEligibleDate() {
        return nextEligibleDate;
    }

    public void setNextEligibleDate(LocalDate nextEligibleDate) {
        this.nextEligibleDate = nextEligibleDate;
    }

    public int getTotalDonations() {
        return totalDonations;
    }

    public void setTotalDonations(int totalDonations) {
        this.totalDonations = totalDonations;
    }
}
