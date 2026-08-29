package com.hemoconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Registers a single, reusable BCryptPasswordEncoder bean.
 *
 * We need this now (Module 1) because UserService hashes passwords before
 * saving a User — the original project called `bcrypt.hash(password, 12)`
 * directly in the Express route; Spring's BCryptPasswordEncoder does the
 * same job (salted BCrypt hash) the Spring-idiomatic way.
 *
 * NOTE: this class only provides the password-hashing tool. The full
 * Spring Security filter chain (which endpoints require a login, JWT
 * validation, etc.) is configured separately in Module 2, in
 * SecurityConfig.java, so it doesn't block us from testing Module 1's
 * plain REST endpoints first.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
