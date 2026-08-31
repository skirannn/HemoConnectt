package com.hemoconnect.controller;

import com.hemoconnect.dto.AiChatRequestDto;
import com.hemoconnect.dto.AiChatResponseDto;
import com.hemoconnect.service.AiAssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The AI assistant's one real endpoint. Requires login (falls through to
 * SecurityConfig's default "authenticated()" rule, same as most of the
 * app) - there's no reason a stranger should be able to run up API costs
 * on an unauthenticated chat endpoint.
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiAssistantService aiAssistantService;

    public AiController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    /** POST /api/ai/chat - ask the HemoConnect Assistant a question. */
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponseDto> chat(@Valid @RequestBody AiChatRequestDto request) {
        String reply = aiAssistantService.chat(request);
        return ResponseEntity.ok(new AiChatResponseDto(reply));
    }
}
