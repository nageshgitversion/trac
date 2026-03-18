package com.investrac.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatRequest {

    @NotBlank(message = "Question cannot be empty")
    @Size(max = 2000, message = "Question too long (max 2000 chars)")
    private String question;

    /**
     * Previous messages in this conversation.
     * Frontend maintains the conversation state and sends history.
     * Max last 10 pairs to keep context window manageable.
     */
    @Size(max = 20, message = "Too many conversation history items (max 20)")
    private List<ConversationMessage> conversationHistory = new ArrayList<>();

    @Data
    public static class ConversationMessage {
        private String role;     // "user" | "assistant"
        private String content;
    }
}
