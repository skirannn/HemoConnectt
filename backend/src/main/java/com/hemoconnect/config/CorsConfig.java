package com.hemoconnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Module 9: allows the React frontend (running on a different port, e.g.
 * Vite's default 5173) to call this API directly.
 *
 * In local development, Vite's dev server proxies /api/** to this backend
 * (see frontend/vite.config.js), so the browser never technically makes a
 * cross-origin request during `npm run dev`. This config exists for the
 * cases the proxy doesn't cover: `npm run preview`, a production build
 * served from its own domain, or calling the API directly with a tool
 * like Postman/curl from a browser-based client.
 */
@Configuration
public class CorsConfig {

    @Value("${hemoconnect.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
