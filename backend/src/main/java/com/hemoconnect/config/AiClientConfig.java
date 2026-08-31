package com.hemoconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Registers a reusable RestClient (Spring's modern synchronous HTTP
 * client, built into spring-web since Spring 6.1 - no extra dependency
 * needed) for calling the Anthropic API from AiAssistantService (Module
 * 10).
 *
 * The actual API key is NOT set here - it's added per-request in
 * AiAssistantService, read from an environment variable. This bean only
 * knows the fixed parts: the base URL and the API version header.
 */
@Configuration
public class AiClientConfig {

    @Bean
    public RestClient anthropicRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();
    }
}
