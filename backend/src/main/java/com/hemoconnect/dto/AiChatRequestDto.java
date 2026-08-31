package com.hemoconnect.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/** What the frontend sends to POST /api/ai/chat. */
public class AiChatRequestDto {

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message is too long")
    private String message;

    /**
     * Earlier turns in this conversation, oldest first, so the assistant
     * has context (e.g. a follow-up question). Optional - a first message
     * sends an empty/absent history.
     */
    @Valid
    @Size(max = 20, message = "Conversation history is too long")
    private List<AiChatMessageDto> conversationHistory = new ArrayList<>();

    public AiChatRequestDto() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<AiChatMessageDto> getConversationHistory() {
        return conversationHistory;
    }

    public void setConversationHistory(List<AiChatMessageDto> conversationHistory) {
        this.conversationHistory = conversationHistory != null ? conversationHistory : new ArrayList<>();
    }
}
