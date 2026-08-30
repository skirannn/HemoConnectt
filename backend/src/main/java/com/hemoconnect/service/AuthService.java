package com.hemoconnect.service;

import com.hemoconnect.dto.*;
import com.hemoconnect.entity.PasswordResetOtp;
import com.hemoconnect.entity.User;
import com.hemoconnect.exception.ResourceNotFoundException;
import com.hemoconnect.repository.PasswordResetOtpRepository;
import com.hemoconnect.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * All the business logic for signing up, logging in, and resetting a
 * forgotten password lives here — the direct equivalent of the original
 * server/routes/auth.js, but split cleanly into Controller (HTTP) vs
 * Service (logic) instead of one big Express route file.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final long OTP_VALIDITY_MINUTES = 5;

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetOtpRepository otpRepository;

    public AuthService(
            UserService userService,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            PasswordResetOtpRepository otpRepository) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.otpRepository = otpRepository;
    }

    /**
     * Registration. UserService.createUser() already handles the
     * "email must be unique" rule and password hashing - AuthService's
     * only job on top of that is to build the User entity from the
     * request and hand back a signed token.
     */
    public AuthResponseDto register(RegisterRequestDto request) {
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(request.getPassword()); // hashed inside UserService.createUser
        newUser.setPhone(request.getPhone());
        newUser.setBloodGroup(request.getBloodGroup());
        newUser.setLocation(request.getLocation());
        newUser.setRole(request.getRole());

        User savedUser = userService.createUser(newUser);

        String token = jwtService.generateToken(savedUser.getEmail());
        return new AuthResponseDto(token, UserResponseDto.fromEntity(savedUser));
    }

    /**
     * Login. We delegate the actual "does this password match?" check to
     * Spring Security's AuthenticationManager (which uses the
     * DaoAuthenticationProvider we configured in SecurityConfig) instead
     * of comparing hashes ourselves - that's the Spring-idiomatic way and
     * automatically throws BadCredentialsException on a wrong password,
     * which GlobalExceptionHandler turns into a clean 401.
     */
    public AuthResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userService.getUserEntityByEmail(request.getEmail());

        // "Remember me" -> 7-day token, otherwise the default 24h token,
        // matching the original expiresIn = rememberMe ? '7d' : '24h' rule.
        long sevenDaysMs = 7L * 24 * 60 * 60 * 1000;
        String token = request.isRememberMe()
                ? jwtService.generateToken(user.getEmail(), sevenDaysMs)
                : jwtService.generateToken(user.getEmail());

        return new AuthResponseDto(token, UserResponseDto.fromEntity(user));
    }

    /**
     * Generates a 6-digit OTP, stores it in the database with a 5-minute
     * expiry, and (in place of a real email/SMS provider) logs it to the
     * server console.
     *
     * IMPROVEMENT over the original: the original Express route returned
     * the OTP directly in the HTTP response "for demo purposes" - but
     * that defeats the entire point of an OTP (anyone watching network
     * traffic could read it). We only ever log it server-side.
     */
    public void sendOtp(SendOtpRequestDto request) {
        // Confirms the account exists before generating an OTP for it -
        // same check the original route performed.
        userService.getUserEntityByEmail(request.getEmail());

        String otpCode = generateSixDigitOtp();

        PasswordResetOtp otp = new PasswordResetOtp();
        otp.setEmail(request.getEmail());
        otp.setOtpCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        otpRepository.save(otp);

        // In a real app this line is replaced with a call to an email/SMS
        // provider. For local development, just check your server console.
        log.info("Password reset OTP for {}: {} (valid {} minutes)",
                request.getEmail(), otpCode, OTP_VALIDITY_MINUTES);
    }

    /** Verifies the OTP and, if valid, overwrites the user's password. */
    public void resetPassword(ResetPasswordRequestDto request) {
        PasswordResetOtp otp = otpRepository
                .findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No pending OTP request found for this email"));

        if (otp.isExpired()) {
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }
        if (!otp.getOtpCode().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        userService.updatePassword(request.getEmail(), request.getNewPassword());

        otp.setUsed(true);
        otpRepository.save(otp);
    }

    private String generateSixDigitOtp() {
        SecureRandom random = new SecureRandom();
        int number = 100000 + random.nextInt(900000); // always exactly 6 digits
        return String.valueOf(number);
    }
}
