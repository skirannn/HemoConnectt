package com.hemoconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body for POST /api/admin/blood-requests/{id}/flag and .../reject. */
public class ModerationReasonDto {

    @NotBlank(message = "A reason is required")
    @Size(max = 500)
    private String reason;

    public ModerationReasonDto() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
