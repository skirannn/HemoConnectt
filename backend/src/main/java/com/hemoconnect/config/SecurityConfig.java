package com.hemoconnect.config;

import com.hemoconnect.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * The REAL security configuration (replaces Module 1's temporary
 * "permit everything" version).
 *
 * Three key ideas:
 *   1. STATELESS sessions - we never use HttpSession or cookies to track
 *      who's logged in. Every request must carry its own JWT.
 *   2. Public vs protected endpoints - /api/auth/** (signup, login,
 *      password reset) must be reachable WITHOUT already being logged in.
 *      Everything else requires a valid JWT.
 *   3. Our JwtAuthenticationFilter runs before Spring Security's own
 *      username/password filter, so by the time Spring Security decides
 *      "is this request authenticated?", our filter has already read the
 *      JWT and populated the SecurityContext if it was valid.
 *
 * Fine-grained rules (donor-only, admin-only endpoints) are added
 * endpoint-by-endpoint with @PreAuthorize in later modules (3, 5, 7) -
 * this class only draws the line between "public" and "must be logged in".
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // turns on @PreAuthorize("...") on controller/service methods
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authProvider) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // not needed for a stateless, token-based REST API
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Tells Spring Security HOW to check a login attempt: look the user up
     * with CustomUserDetailsService, then compare the submitted password
     * against the stored BCrypt hash using our PasswordEncoder bean.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            com.hemoconnect.security.CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Exposes Spring Security's AuthenticationManager as a bean so
     * AuthService can call authenticationManager.authenticate(...) during
     * login, instead of us manually comparing passwords ourselves.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
