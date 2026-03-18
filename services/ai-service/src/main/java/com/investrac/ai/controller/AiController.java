package com.investrac.ai.controller;

import com.investrac.ai.dto.request.ChatRequest;
import com.investrac.ai.dto.response.ChatResponse;
import com.investrac.ai.dto.response.InsightResponse;
import com.investrac.ai.dto.response.InsightSummaryResponse;
import com.investrac.ai.service.AiService;
import com.investrac.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Advisor",
     description = "Claude-powered financial advisor with personalized insights and chat")
public class AiController {

    private final AiService aiService;

    // ── CHAT ─────────────────────────────────────────────────

    @PostMapping("/chat")
    @Operation(
        summary     = "Ask the AI financial advisor a question",
        description = "Send a question with optional conversation history. " +
                      "Returns personalized answer based on your portfolio, wallet, and transactions. " +
                      "Powered by Claude claude-sonnet-4-20250514."
    )
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(aiService.chat(userId, request)));
    }

    @GetMapping("/chat/history")
    @Operation(
        summary     = "Get chat conversation history",
        description = "Returns all previous messages in chronological order."
    )
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getChatHistory(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(aiService.getChatHistory(userId)));
    }

    @DeleteMapping("/chat/history")
    @Operation(summary = "Clear all chat history for the user")
    public ResponseEntity<ApiResponse<Void>> clearChatHistory(
            @RequestHeader("X-User-Id") Long userId) {
        aiService.clearChatHistory(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Chat history cleared"));
    }

    // ── INSIGHTS ─────────────────────────────────────────────

    @GetMapping("/insights")
    @Operation(
        summary     = "Get unread AI insights",
        description = "Returns personalized financial insights generated nightly. " +
                      "Includes unread count for notification badge."
    )
    public ResponseEntity<ApiResponse<InsightSummaryResponse>> getInsights(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(aiService.getInsights(userId)));
    }

    @GetMapping("/insights/all")
    @Operation(
        summary     = "Get all insights (read + unread)",
        description = "Returns paginated history of all insights."
    )
    public ResponseEntity<ApiResponse<List<InsightResponse>>> getAllInsights(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "20")
            @Parameter(description = "Max insights to return") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
            aiService.getAllInsights(userId, Math.min(limit, 50))));
    }

    @PostMapping("/insights/generate")
    @Operation(
        summary     = "Trigger insight generation on-demand",
        description = "Generate personalized insights now (normally runs at 11:30 PM). " +
                      "Skipped if insights already generated today."
    )
    public ResponseEntity<ApiResponse<List<InsightResponse>>> generateInsights(
            @RequestHeader("X-User-Id") Long userId) {
        var insights = aiService.generateInsights(userId);
        return ResponseEntity.ok(ApiResponse.success(insights,
            insights.isEmpty() ? "Insights already generated today" : "New insights generated"));
    }

    @PatchMapping("/insights/{id}/read")
    @Operation(summary = "Mark a specific insight as read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        aiService.markInsightRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Marked as read"));
    }

    @PatchMapping("/insights/read-all")
    @Operation(summary = "Mark all insights as read")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @RequestHeader("X-User-Id") Long userId) {
        aiService.markAllInsightsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "All insights marked as read"));
    }
}
