package com.investrac.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.investrac.ai.client.ClaudeApiClient;
import com.investrac.ai.client.ClaudeApiClient.ClaudeApiException;
import com.investrac.ai.dto.request.ChatRequest;
import com.investrac.ai.dto.response.ChatResponse;
import com.investrac.ai.dto.response.InsightResponse;
import com.investrac.ai.dto.response.InsightSummaryResponse;
import com.investrac.ai.entity.AiChatHistory;
import com.investrac.ai.entity.AiInsight;
import com.investrac.ai.entity.AiInsight.InsightType;
import com.investrac.ai.prompt.PromptBuilder;
import com.investrac.ai.prompt.UserContext;
import com.investrac.ai.repository.AiChatHistoryRepository;
import com.investrac.ai.repository.AiInsightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    private final AiInsightRepository    insightRepository;
    private final AiChatHistoryRepository chatRepository;
    private final ClaudeApiClient        claudeClient;
    private final PromptBuilder          promptBuilder;
    private final ObjectMapper           objectMapper;
    private final UserContextService     userContextService;

    // ════════════════════════════════════════
    // CHAT
    // ════════════════════════════════════════

    /**
     * Answer a user's financial question using Claude with their full context.
     *
     * Flow:
     *   1. Fetch user's financial snapshot (wallet, portfolio, transactions)
     *   2. Build system prompt with context
     *   3. Format conversation history for Claude messages array
     *   4. Call Claude API
     *   5. Save question + answer to chat history
     *   6. Return answer + suggested follow-up questions
     */
    @Transactional
    public ChatResponse chat(Long userId, ChatRequest request) {
        long startMs = System.currentTimeMillis();
        log.info("AI chat for userId={} (context not logged for privacy)", userId);

        // 1. Get user financial context
        UserContext ctx = userContextService.buildContext(userId);

        // 2. Build prompts
        String systemPrompt = promptBuilder.buildChatSystemPrompt(ctx);

        // 3. Format history: convert frontend history + prepend last 10 saved messages
        List<Map<String, String>> history = buildMessageHistory(userId, request);

        // 4. Call Claude
        String answer;
        try {
            answer = claudeClient.chat(systemPrompt, history, 1024);
        } catch (ClaudeApiException e) {
            log.error("Claude API failed for userId={}: {}", userId, e.getMessage());
            // Return graceful fallback — never expose API errors to users
            answer = buildFallbackAnswer(request.getQuestion(), ctx);
        }

        // 5. Persist chat history (async-safe: within same transaction)
        saveToHistory(userId, AiChatHistory.Role.USER,      request.getQuestion());
        saveToHistory(userId, AiChatHistory.Role.ASSISTANT, answer);

        // 6. Build suggested follow-ups
        List<String> suggestions = promptBuilder.buildSuggestedQuestions(ctx);

        long duration = System.currentTimeMillis() - startMs;
        log.info("AI chat complete for userId={} durationMs={}", userId, duration);

        return ChatResponse.builder()
            .answer(answer)
            .suggestedQuestions(suggestions)
            .processingTimeMs(duration)
            .build();
    }

    // ════════════════════════════════════════
    // INSIGHT GENERATION
    // ════════════════════════════════════════

    /**
     * Generate 3-5 personalized insights for a user.
     * Called by NightlyInsightScheduler or on-demand.
     *
     * Uses JSON mode — Claude returns a structured array which we parse and save.
     */
    @Transactional
    public List<InsightResponse> generateInsights(Long userId) {
        log.info("Generating insights for userId={}", userId);

        // Prevent duplicate generation within same day
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);
        if (insightRepository.hasInsightGeneratedSince(userId, todayStart)) {
            log.info("Insights already generated today for userId={}", userId);
            return getInsights(userId).getInsights();
        }

        UserContext ctx = userContextService.buildContext(userId);
        String systemPrompt = promptBuilder.buildInsightSystemPrompt();
        String userPrompt   = promptBuilder.buildInsightUserPrompt(ctx);

        String rawJson;
        try {
            rawJson = claudeClient.generateInsights(systemPrompt, userPrompt);
        } catch (ClaudeApiException e) {
            log.error("Claude insight generation failed for userId={}: {}", userId, e.getMessage());
            return buildFallbackInsights(ctx);
        }

        // Parse JSON array from Claude response
        List<AiInsight> insights = parseInsightsFromJson(rawJson, userId);

        if (insights.isEmpty()) {
            log.warn("No insights parsed from Claude response for userId={}", userId);
            return List.of();
        }

        List<AiInsight> saved = insightRepository.saveAll(insights);
        log.info("Saved {} insights for userId={}", saved.size(), userId);

        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ════════════════════════════════════════
    // GET INSIGHTS
    // ════════════════════════════════════════

    @Transactional(readOnly = true)
    public InsightSummaryResponse getInsights(Long userId) {
        List<AiInsight> unread = insightRepository
            .findByUserIdAndReadFalseOrderByPriorityAscGeneratedAtDesc(userId);
        long count = insightRepository.countByUserIdAndReadFalse(userId);

        return InsightSummaryResponse.builder()
            .unreadCount(count)
            .insights(unread.stream().map(this::toResponse).collect(Collectors.toList()))
            .build();
    }

    @Transactional(readOnly = true)
    public List<InsightResponse> getAllInsights(Long userId, int limit) {
        return insightRepository
            .findByUserIdOrderByGeneratedAtDesc(userId, PageRequest.of(0, limit))
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ════════════════════════════════════════
    // MARK AS READ
    // ════════════════════════════════════════

    @Transactional
    public void markInsightRead(Long id, Long userId) {
        int updated = insightRepository.markRead(id, userId);
        if (updated == 0) {
            log.warn("Insight id={} not found for userId={}", id, userId);
        }
    }

    @Transactional
    public void markAllInsightsRead(Long userId) {
        insightRepository.markAllRead(userId);
        log.info("All insights marked read for userId={}", userId);
    }

    // ════════════════════════════════════════
    // GET CHAT HISTORY (for UI display)
    // ════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<Map<String, String>> getChatHistory(Long userId) {
        return chatRepository
            .findByUserIdOrderByCreatedAtAsc(userId)
            .stream()
            .map(h -> Map.of(
                "role",    h.getRole().name().toLowerCase(),
                "content", h.getContent()
            ))
            .collect(Collectors.toList());
    }

    @Transactional
    public void clearChatHistory(Long userId) {
        chatRepository.deleteOldHistory(userId, Instant.EPOCH);
        log.info("Chat history cleared for userId={}", userId);
    }

    // ════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════

    private List<Map<String, String>> buildMessageHistory(Long userId, ChatRequest request) {
        // Prefer history sent by frontend (more reliable, has all context)
        if (request.getConversationHistory() != null
                && !request.getConversationHistory().isEmpty()) {
            return request.getConversationHistory().stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toList());
        }

        // Fallback: load last 10 messages from DB
        return chatRepository
            .findRecentByUserId(userId, PageRequest.of(0, 10))
            .stream()
            .sorted(Comparator.comparing(AiChatHistory::getCreatedAt))
            .map(h -> Map.of(
                "role",    h.getRole().name().toLowerCase(),
                "content", h.getContent()
            ))
            .collect(Collectors.toList());
    }

    private void saveToHistory(Long userId, AiChatHistory.Role role, String content) {
        AiChatHistory history = AiChatHistory.builder()
            .userId(userId)
            .role(role)
            .content(content)
            .build();
        if (history != null) {
            chatRepository.save(history);
        }
    }

    private List<AiInsight> parseInsightsFromJson(String rawJson, Long userId) {
        try {
            // Strip markdown code fences if present
            String clean = rawJson
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("```", "")
                .trim();

            List<Map<String, Object>> raw = objectMapper.readValue(
                clean, new TypeReference<>() {});

            return raw.stream()
                .filter(m -> m.get("content") != null && m.get("type") != null)
                .map(m -> {
                    InsightType type;
                    try {
                        type = InsightType.valueOf(String.valueOf(m.get("type")).toUpperCase());
                    } catch (IllegalArgumentException e) {
                        type = InsightType.SPENDING; // safe default
                    }
                    int priority = m.get("priority") instanceof Number n ? n.intValue() : 3;

                    return AiInsight.builder()
                        .userId(userId)
                        .content(String.valueOf(m.get("content")))
                        .type(type)
                        .priority(Math.min(5, Math.max(1, priority)))
                        .read(false)
                        .build();
                })
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to parse insight JSON for userId={}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Graceful fallback when Claude API is unavailable.
     * Returns a helpful but generic answer based on context.
     */
    private String buildFallbackAnswer(String question, UserContext ctx) {
        log.warn("Using fallback answer (Claude API unavailable)");
        return String.format(
            "The AI advisor is currently unavailable. "
            + "Your portfolio is currently valued at ₹%s with ₹%s free in your wallet this month. "
            + "Please try again in a few minutes.",
            fmt(ctx.portfolioValue()),
            fmt(ctx.walletFreeToSpend())
        );
    }

    private List<InsightResponse> buildFallbackInsights(UserContext ctx) {
        log.warn("Using fallback insights (Claude API unavailable)");
        List<InsightResponse> fallbacks = new ArrayList<>();

        if (ctx.has80cOpportunity()) {
            fallbacks.add(InsightResponse.builder()
                .type("TAX").priority(1)
                .content("You have ₹" + fmt(ctx.get80cRemaining())
                    + " of 80C investment limit remaining. Consider investing in ELSS before March 31st.")
                .read(false).generatedAt(Instant.now()).build());
        }

        fallbacks.add(InsightResponse.builder()
            .type("PORTFOLIO").priority(3)
            .content("Your portfolio XIRR is " + ctx.portfolioXirr()
                + "%. Review your holdings to ensure you are on track with your financial goals.")
            .read(false).generatedAt(Instant.now()).build());

        return fallbacks;
    }

    private InsightResponse toResponse(AiInsight i) {
        return InsightResponse.builder()
            .id(i.getId())
            .content(i.getContent())
            .type(i.getType().name())
            .priority(i.getPriority())
            .read(i.isRead())
            .generatedAt(i.getGeneratedAt())
            .build();
    }

    private String fmt(BigDecimal v) {
        if (v == null) return "0";
        long val = v.longValue();
        if (val >= 10_000_000) return String.format("%.1fCr", val / 10_000_000.0);
        if (val >= 100_000)    return String.format("%.1fL",  val / 100_000.0);
        return String.format("₹%,d", val);
    }
}
