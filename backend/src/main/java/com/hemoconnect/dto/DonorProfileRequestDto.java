package com.hemoconnect.dto;

import com.hemoconnect.entity.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * What a donor sends to create/update their donor profile
 * (PUT /api/donors/{userId}/profile).
 */
public class DonorProfileRequestDto {

    @Min(value = 18, message = "Donors must be at least 18 years old")
    @Max(value = 65, message = "Please double-check this age")
    private Integer age;

    @Min(value = 30, message = "Please double-check this weight (kg)")
    private Double weight;

    @Min(value = 100, message = "Please double-check this height (cm)")
    private Double height;

    private Gender gender;

    @Min(value = 1, message = "Distance must be at least 1 km")
    private Integer maxDistanceKm;

    private boolean emergencyOnly;

    public DonorProfileRequestDto() {
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
