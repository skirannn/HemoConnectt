package com.hemoconnect.dto;

import com.hemoconnect.entity.BloodGroup;
import com.hemoconnect.entity.UrgencyLevel;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/** What a recipient sends to POST /api/blood-requests. */
public class BloodRequestCreateDto {

    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @NotNull(message = "Units required is required")
    @Min(value = 1, message = "Must request at least 1 unit")
    private Integer unitsRequired;

    private String hospital;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Urgency is required")
    private UrgencyLevel urgency;

    @NotNull(message = "Required date is required")
    @FutureOrPresent(message = "Required date can't be in the past")
    private LocalDate requiredDate;

    @Size(max = 1000)
    private String description;

    public BloodRequestCreateDto() {
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public Integer getUnitsRequired() {
        return unitsRequired;
    }

    public void setUnitsRequired(Integer unitsRequired) {
        this.unitsRequired = unitsRequired;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public UrgencyLevel getUrgency() {
        return urgency;
    }

    public void setUrgency(UrgencyLevel urgency) {
        this.urgency = urgency;
    }

    public LocalDate getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(LocalDate requiredDate) {
        this.requiredDate = requiredDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
