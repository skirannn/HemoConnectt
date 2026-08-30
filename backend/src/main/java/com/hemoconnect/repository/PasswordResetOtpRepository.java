package com.hemoconnect.repository;

import com.hemoconnect.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    /**
     * Finds the most recently created, still-unused OTP for an email.
     * Generates: SELECT * FROM password_reset_otps
     *            WHERE email = ? AND used = false
     *            ORDER BY created_at DESC LIMIT 1
     */
    Optional<PasswordResetOtp> findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(String email);
}
