package com.hemoconnect.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * What's sent when a donor's donation is recorded
 * (POST /api/donors/{userId}/donations).
 *
 * For now this just updates the donor's aggregate stats (used, for
 * example, when an admin or the donor themself confirms a donation
 * happened). Once Module 4 (Blood Request) exists, a donation will
 * usually be recorded as a side-effect of fulfilling a specific request
 * instead of being entered directly like this - this endpoint stays
 * useful for donations made outside of a tracked request (e.g. at a
 * blood drive).
 */
public class RecordDonationRequestDto {

    @NotNull(message = "Units donated is required")
    @Min(value = 1, message = "Must donate at least 1 unit")
    private Integer units;

    /** Defaults to today if not provided. */
    private LocalDate donationDate;

    public RecordDonationRequestDto() {
    }

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(LocalDate donationDate) {
        this.donationDate = donationDate;
    }
}
