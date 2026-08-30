package com.hemoconnect.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Everything to do with creating and reading JWT (JSON Web Token) strings
 * lives in this one class.
 *
 * A JWT is just a signed, self-contained string. Once we hand one to the
 * browser, we never have to look anything up in a "sessions" table to
 * check if the user is logged in — we just re-verify the signature on
 * every request. That's what "stateless authentication" means, and it's
 * the direct equivalent of what `jsonwebtoken`'s `jwt.sign()` /
 * `jwt.verify()` did in the original Express middleware.
 */
@Service
public class JwtService {

    @Value("${hemoconnect.jwt.secret}")
    private String secretProperty;

    @Value("${hemoconnect.jwt.expiration-ms}")
    private long defaultExpirationMs;

    private SecretKey signingKey;

    /**
     * Runs once, right after Spring creates this bean. We fail loudly and
     * immediately at startup if JWT_SECRET was never set, instead of the
     * original app's approach of silently falling back to the hardcoded
     * string 'your-secret-key' — a real security hole we're fixing here.
     */
    @PostConstruct
    public void init() {
        if (secretProperty == null || secretProperty.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET environment variable is not set. " +
                    "Generate one (e.g. `openssl rand -base64 64`) and set it " +
                    "before starting the app - see .env.example.");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretProperty.getBytes(StandardCharsets.UTF_8));
    }

    /** Generates a token using the default expiration (24h, from application.yml). */
    public String generateToken(String email) {
        return generateToken(email, defaultExpirationMs);
    }

    /**
     * Generates a token with a custom lifetime — used for the "remember me"
     * checkbox on login (7 days instead of 24 hours), exactly like the
     * original `expiresIn = rememberMe ? '7d' : '24h'` logic.
     */
    public String generateToken(String email, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, String expectedEmail) {
        try {
            String email = extractEmail(token);
            return email.equals(expectedEmail) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            // Malformed, expired (signature-wise), or tampered token.
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
