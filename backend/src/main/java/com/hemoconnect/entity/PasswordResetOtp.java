package com.hemoconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Stores a one-time password (OTP) issued for a "forgot password" flow.
 *
 * The original Express app kept OTPs in a plain in-memory JavaScript
 * object (`const otpStore = {}`) — it worked for a demo, but every OTP was
 * wiped out if the server ever restarted, and it wouldn't work at all if
 * you ever ran more than one server instance. Persisting OTPs in the
 * database fixes both problems while keeping the exact same idea: a
 * short-lived code tied to an email, checked once, then thrown away.
 */
@Entity
@Table(name = "password_reset_otps")
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The email the OTP was issued for. */
    @Column(nullable = false)
    private String email;

    @Column(name = "otp_code", nullable = false)
    private String otpCode;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Once an OTP has been used to reset a password, it can't be reused. */
    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public PasswordResetOtp() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // ----- Getters and setters -----

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
