package com.hemoconnect.dto;

import com.hemoconnect.entity.BloodGroup;
import com.hemoconnect.entity.DonorResponse;
import com.hemoconnect.entity.ResponseType;

import java.time.LocalDateTime;

/**
 * One donor's response, as returned inside a BloodRequestResponseDto.
 *
 * The recipient needs enough donor information to contact a donor
 * after the donor accepts a blood request.
 */
public class DonorResponseDto {

    private Long id;
    private Long donorId;
    private String donorName;
    private String donorPhone;
    private BloodGroup donorBloodGroup;
    private String donorLocation;
    private ResponseType responseType;
    private String responseMessage;
    private LocalDateTime createdAt;

    public DonorResponseDto() {
    }

    public static DonorResponseDto fromEntity(DonorResponse response) {

        DonorResponseDto dto = new DonorResponseDto();

        dto.id = response.getId();

        if (response.getDonor() != null) {
            dto.donorId = response.getDonor().getId();
            dto.donorName = response.getDonor().getName();
            dto.donorPhone = response.getDonor().getPhone();
            dto.donorBloodGroup = response.getDonor().getBloodGroup();
            dto.donorLocation = response.getDonor().getLocation();
        }

        dto.responseType = response.getResponseType();
        dto.responseMessage = response.getResponseMessage();
        dto.createdAt = response.getCreatedAt();

        return dto;
    }

    // ----- Getters and setters -----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getDonorPhone() {
        return donorPhone;
    }

    public void setDonorPhone(String donorPhone) {
        this.donorPhone = donorPhone;
    }

    public BloodGroup getDonorBloodGroup() {
        return donorBloodGroup;
    }

    public void setDonorBloodGroup(BloodGroup donorBloodGroup) {
        this.donorBloodGroup = donorBloodGroup;
    }

    public String getDonorLocation() {
        return donorLocation;
    }

    public void setDonorLocation(String donorLocation) {
        this.donorLocation = donorLocation;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}