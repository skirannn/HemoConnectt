package com.hemoconnect.dto;

import com.hemoconnect.entity.DonorResponse;
import com.hemoconnect.entity.ResponseType;

import java.time.LocalDateTime;

/** One donor's response, as returned inside a BloodRequestResponseDto. */
public class DonorResponseDto {

    private Long id;
    private Long donorId;
    private String donorName;
    private ResponseType responseType;
    private String responseMessage;
    private LocalDateTime createdAt;

    public static DonorResponseDto fromEntity(DonorResponse response) {
        DonorResponseDto dto = new DonorResponseDto();
        dto.id = response.getId();
        dto.donorId = response.getDonor().getId();
        dto.donorName = response.getDonor().getName();
        dto.responseType = response.getResponseType();
        dto.responseMessage = response.getResponseMessage();
        dto.createdAt = response.getCreatedAt();
        return dto;
    }

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
