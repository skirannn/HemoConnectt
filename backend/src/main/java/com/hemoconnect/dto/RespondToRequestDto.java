package com.hemoconnect.dto;

import com.hemoconnect.entity.ResponseType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** What a donor sends to POST /api/blood-requests/{id}/respond. */
public class RespondToRequestDto {

    @NotNull(message = "Response type is required")
    private ResponseType responseType;

    @Size(max = 500)
    private String responseMessage;

    public RespondToRequestDto() {
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
}
