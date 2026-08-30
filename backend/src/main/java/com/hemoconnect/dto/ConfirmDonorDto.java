package com.hemoconnect.dto;

import jakarta.validation.constraints.NotNull;

/** What a requester sends to POST /api/blood-requests/{id}/confirm. */
public class ConfirmDonorDto {

    @NotNull(message = "donorId is required")
    private Long donorId;

    public ConfirmDonorDto() {
    }

    public Long getDonorId() {
        return donorId;
    }

    public void setDonorId(Long donorId) {
        this.donorId = donorId;
    }
}
