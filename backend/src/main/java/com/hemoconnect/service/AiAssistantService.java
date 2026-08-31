package com.hemoconnect.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hemoconnect.dto.AiChatMessageDto;
import com.hemoconnect.dto.AiChatRequestDto;
import com.hemoconnect.exception.AiNotConfiguredException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * The HemoConnect AI Assistant (Module 10) - answers questions about how
 * to use the app, explains the blood request workflow, and gives general,
 * publicly-known blood donation information. It must never give medical
 * diagnosis or personalized treatment advice - that instruction is baked
 * into the fixed SYSTEM_PROMPT below, sent with every request.
 *
 * The API key never reaches the frontend: it's read here, server-side,
 * from an environment variable, and only ever placed in a header on a
 * server-to-server HTTP call to Anthropic's API.
 */
@Service
public class AiAssistantService {

    private static final String SYSTEM_PROMPT = """
            You are the HemoConnect Assistant, a help assistant embedded in the
            HemoConnect blood donation platform.

            You can help with:
            - Explaining how to use HemoConnect's features (signing up, creating a
              donor profile, requesting blood, responding to a request, notifications).
            - Explaining the blood request workflow (pending -> matched -> confirmed
              -> fulfilled) and what each status means.
            - General, publicly-known information about blood donation (how blood
              types and compatibility work in general terms, typical eligibility
              guidelines, what to expect during a donation, why donation cooldown
              periods exist).
            - Helping users figure out which part of the app to use for what they're
              trying to do.

            You must NOT:
            - Diagnose any medical condition.
            - Tell a specific person whether THEY personally are eligible or safe to
              donate or receive blood - always direct that question to a doctor,
              nurse, or the blood bank staff.
            - Recommend any treatment, medication, or medical course of action.
            - Make up information about a specific user's account, request, or
              donation history - you don't have access to that data, so say so if asked.

            Keep answers concise and friendly. If a question is a personal medical
            question rather than a general/navigational one, clearly say you can't
            help with that and recommend they consult a medical professional.
            """;

    private final RestClient anthropicRestClient;

    @Value("${hemoconnect.ai.api-key:}")
    private String apiKey;

    @Value("${hemoconnect.ai.model:claude-sonnet-4-6}")
    private String model;

    @Value("${hemoconnect.ai.max-tokens:1024}")
    private int maxTokens;

    public AiAssistantService(RestClient anthropicRestClient) {
        this.anthropicRestClient = anthropicRestClient;
    }

    public String chat(AiChatRequestDto request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiNotConfiguredException(
                    "The AI assistant isn't configured yet - set AI_API_KEY (see .env.example) to enable it.");
        }

        List<AnthropicMessage> messages = buildMessages(request);

        AnthropicRequest anthropicRequest = new AnthropicRequest(model, maxTokens, SYSTEM_PROMPT, messages);

        AnthropicResponse response = anthropicRestClient.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .body(anthropicRequest)
                .retrieve()
                .body(AnthropicResponse.class);

        return extractReplyText(response);
    }

    /**
     * Turns the conversation history + new message into the flat list of
     * {role, content} turns the Anthropic API expects. Pulled out as its
     * own method so it's easy to unit test without mocking any HTTP call.
     */
    List<AnthropicMessage> buildMessages(AiChatRequestDto request) {
        List<AnthropicMessage> messages = new ArrayList<>();
        for (AiChatMessageDto turn : request.getConversationHistory()) {
            messages.add(new AnthropicMessage(turn.getRole(), turn.getContent()));
        }
        messages.add(new AnthropicMessage("user", request.getMessage()));
        return messages;
    }

    /**
     * The API can return several content blocks; we only care about the
     * text ones, concatenated. Also its own method so it's easy to unit
     * test against a hand-built response object.
     */
    String extractReplyText(AnthropicResponse response) {
        if (response == null || response.content() == null) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (ContentBlock block : response.content()) {
            if ("text".equals(block.type()) && block.text() != null) {
                text.append(block.text());
            }
        }
        return text.toString();
    }

    // ----- Minimal wire-format records for the Anthropic Messages API -----
    // These are internal to this service - never returned directly from a
    // controller (see AiChatResponseDto for what the frontend actually gets).

    record AnthropicRequest(
            String model,
            @JsonProperty("max_tokens") int maxTokens,
            String system,
            List<AnthropicMessage> messages) {
    }

    record AnthropicMessage(String role, String content) {
    }

    record AnthropicResponse(List<ContentBlock> content) {
    }

    record ContentBlock(String type, String text) {
    }
}
