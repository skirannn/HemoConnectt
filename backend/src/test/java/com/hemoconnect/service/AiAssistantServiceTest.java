package com.hemoconnect.service;

import com.hemoconnect.dto.AiChatMessageDto;
import com.hemoconnect.dto.AiChatRequestDto;
import com.hemoconnect.exception.AiNotConfiguredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Rather than mocking RestClient's whole fluent chain (post().uri()...),
 * these tests target the two pure, easily-testable pieces of this
 * service: turning a request into the API's message list, and turning
 * the API's response back into plain text - plus the "not configured"
 * guard, which needs no HTTP call at all.
 */
@ExtendWith(MockitoExtension.class)
class AiAssistantServiceTest {

    @Mock
    private RestClient restClient;

    private AiAssistantService aiAssistantService;

    @BeforeEach
    void setUp() {
        aiAssistantService = new AiAssistantService(restClient);
    }

    @Test
    void chat_throwsAiNotConfiguredException_whenApiKeyIsBlank() {
        ReflectionTestUtils.setField(aiAssistantService, "apiKey", "");

        AiChatRequestDto request = new AiChatRequestDto();
        request.setMessage("How does donor matching work?");

        assertThatThrownBy(() -> aiAssistantService.chat(request))
                .isInstanceOf(AiNotConfiguredException.class);
    }

    @Test
    void buildMessages_appendsNewMessageAfterHistory_inOrder() {
        AiChatRequestDto request = new AiChatRequestDto();
        request.setMessage("And how long is the cooldown?");
        request.setConversationHistory(List.of(
                new AiChatMessageDto("user", "What is donor eligibility?"),
                new AiChatMessageDto("assistant", "It depends on several factors...")
        ));

        var messages = aiAssistantService.buildMessages(request);

        assertThat(messages).hasSize(3);
        assertThat(messages.get(0).role()).isEqualTo("user");
        assertThat(messages.get(1).role()).isEqualTo("assistant");
        assertThat(messages.get(2).role()).isEqualTo("user");
        assertThat(messages.get(2).content()).isEqualTo("And how long is the cooldown?");
    }

    @Test
    void extractReplyText_concatenatesOnlyTextBlocks() {
        var response = new AiAssistantService.AnthropicResponse(List.of(
                new AiAssistantService.ContentBlock("text", "Donors must wait "),
                new AiAssistantService.ContentBlock("text", "56 days between donations."),
                new AiAssistantService.ContentBlock("tool_use", "ignored")
        ));

        String reply = aiAssistantService.extractReplyText(response);

        assertThat(reply).isEqualTo("Donors must wait 56 days between donations.");
    }

    @Test
    void extractReplyText_returnsEmptyString_whenResponseIsNull() {
        assertThat(aiAssistantService.extractReplyText(null)).isEmpty();
    }
}
