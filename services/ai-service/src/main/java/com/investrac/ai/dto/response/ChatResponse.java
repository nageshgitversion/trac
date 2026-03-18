package com.investrac.ai.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatResponse {
    private String       answer;
    private List<String> suggestedQuestions;  // follow-up prompts shown as chips in UI
    private long         processingTimeMs;
}
