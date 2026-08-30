package com.hemoconnect.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A focused unit test for JwtService: can we generate a token and then
 * successfully validate it, and does validation correctly reject a token
 * meant for a different user?
 *
 * We use ReflectionTestUtils to inject the @Value fields directly, since
 * this is a plain unit test (no Spring context is started).
 */
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // A throwaway 256-bit test secret - never use a literal like this
        // in a real environment; real secrets always come from JWT_SECRET.
        ReflectionTestUtils.setField(jwtService, "secretProperty",
                "test-secret-key-must-be-long-enough-for-hs256-signing-1234567890");
        ReflectionTestUtils.setField(jwtService, "defaultExpirationMs", 3600000L); // 1 hour
        jwtService.init();
    }

    @Test
    void generateToken_thenIsTokenValid_returnsTrue_forCorrectEmail() {
        String token = jwtService.generateToken("donor@example.com");

        assertThat(jwtService.isTokenValid(token, "donor@example.com")).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_whenEmailDoesNotMatch() {
        String token = jwtService.generateToken("donor@example.com");

        assertThat(jwtService.isTokenValid(token, "someone-else@example.com")).isFalse();
    }

    @Test
    void extractEmail_returnsTheEmailTheTokenWasIssuedFor() {
        String token = jwtService.generateToken("recipient@example.com");

        assertThat(jwtService.extractEmail(token)).isEqualTo("recipient@example.com");
    }

    @Test
    void isTokenValid_returnsFalse_forAGarbageToken() {
        assertThat(jwtService.isTokenValid("not-a-real-jwt", "donor@example.com")).isFalse();
    }
}
