package com.hemoconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** One turn in a conversation with the AI assistant. */
public class AiChatMessageDto {

    @NotBlank
    @Pattern(regexp = "user|assistant", message = "role must be 'user' or 'assistant'")
    private String role;

    @NotBlank
    private String content;

    public AiChatMessageDto() {
    }

    public AiChatMessageDto(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
