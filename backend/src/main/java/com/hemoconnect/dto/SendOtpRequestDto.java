package com.hemoconnect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * What the frontend sends to POST /api/auth/send-otp.
 *
 * Simplification vs. the original: the original supported resetting via
 * either email OR phone (a `method`/`value` pair), but there was no real
 * SMS integration behind the phone path — it was the same in-memory OTP
 * logic either way. We keep the one path that's actually meaningful
 * (email) rather than carrying two code paths for a feature that isn't
 * really implemented. Phone-based OTP can be added later as its own
 * enhancement if you add a real SMS provider.
 */
public class SendOtpRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    public SendOtpRequestDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
