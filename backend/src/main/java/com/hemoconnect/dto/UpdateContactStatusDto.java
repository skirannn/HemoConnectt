package com.hemoconnect.dto;

import com.hemoconnect.entity.ContactStatus;
import jakarta.validation.constraints.NotNull;

/** What an admin sends to PATCH /api/contact/{id}/status. */
public class UpdateContactStatusDto {

    @NotNull(message = "Status is required")
    private ContactStatus status;

    public UpdateContactStatusDto() {
    }

    public ContactStatus getStatus() {
        return status;
    }

    public void setStatus(ContactStatus status) {
        this.status = status;
    }
}
