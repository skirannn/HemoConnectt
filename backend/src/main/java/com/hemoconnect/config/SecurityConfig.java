package com.hemoconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * TEMPORARY security configuration — Module 1 only.
 *
 * Adding spring-boot-starter-security to the project (needed for
 * BCryptPasswordEncoder) makes Spring Security lock down every endpoint
 * with a login form by default. Since JWT authentication doesn't exist
 * yet at this stage of the build, we explicitly permit all requests so
 * the User module's REST API can be tested on its own.
 *
 * THIS FILE WILL BE REPLACED IN MODULE 2 with the real configuration:
 * a stateless JWT filter, public endpoints for /api/auth/**, and
 * role-protected endpoints for everything else (donor/recipient/admin).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // not needed for a stateless REST API
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // <-- replaced with real rules in Module 2
                );
        return http.build();
    }
}
