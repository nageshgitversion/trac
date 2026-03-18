package com.investrac.ai.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * HTTP client for the Anthropic Claude API.
 *
 * Endpoint: POST https://api.anthropic.com/v1/messages
 * Model: claude-sonnet-4-20250514
 *
 * Security:
 *   - API key injected from environment variable — never hardcoded
 *   - Request/response content is NEVER logged (financial data privacy)
 *   - Only metadata is logged: model, tokens used, latency
 *
 * Rate limiting:
 *   - Anthropic returns 429 when rate limited
 *   - We retry with exponential backoff: 2s → 4s → 8s (max 3 attempts)
 *   - If still failing after 3 attempts: throw so caller can use fallback
 *
 * Token budget:
 *   - max_tokens: 1024 for chat (concise responses)
 *   - max_tokens: 2048 for insight generation (JSON array of 3-5 insights)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ClaudeApiClient {

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com";
    private static final String CLAUDE_MODEL       = "claude-sonnet-4-20250514";
    private static final String ANTHROPIC_VERSION  = "2023-06-01";

    @Value("${anthropic.api-key}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;

    // ════════════════════════════════════════
    // BLOCKING CHAT CALL
    // ════════════════════════════════════════

    /**
     * Send a chat message to Claude and get a response.
     *
     * @param systemPrompt  The system context (user financial data + instructions)
     * @param messages      Conversation history + current question
     * @param maxTokens     Max response length
     * @return Claude's text response
     */
    public String chat(String systemPrompt, List<Map<String, String>> messages, int maxTokens) {
        long startMs = System.currentTimeMillis();

        // SECURITY: never log systemPrompt or messages — they contain financial data
        log.debug("Claude API call: model={} maxTokens={}", CLAUDE_MODEL, maxTokens);

        ClaudeRequest request = new ClaudeRequest(
            CLAUDE_MODEL,
            maxTokens,
            systemPrompt,
            messages
        );

        try {
            ClaudeResponse response = buildWebClient()
                .post()
                .uri("/v1/messages")
                .contentType(MediaType.valueOf("application/json"))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClaudeResponse.class)
                .retryWhen(
                    Retry.backoff(3, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(30))
                        .filter(ex -> isRateLimitOrServerError(ex))
                        .doBeforeRetry(signal ->
                            log.warn("Claude API retry attempt {} due to: {}",
                                signal.totalRetries() + 1,
                                signal.failure().getMessage()))
                )
                .timeout(Duration.ofSeconds(30))
                .block();

            if (response == null || response.content() == null || response.content().isEmpty()) {
                log.error("Claude API returned null or empty response");
                throw new ClaudeApiException("Empty response from Claude API");
            }

            String answer = response.content().get(0).text();
            long duration = System.currentTimeMillis() - startMs;

            // Log only metadata, never content
            log.info("Claude API success: model={} inputTokens={} outputTokens={} durationMs={}",
                CLAUDE_MODEL,
                response.usage() != null ? response.usage().inputTokens() : -1,
                response.usage() != null ? response.usage().outputTokens() : -1,
                duration);

            return answer;

        } catch (WebClientResponseException e) {
            log.error("Claude API HTTP error: status={} durationMs={}",
                e.getStatusCode(), System.currentTimeMillis() - startMs);
            throw new ClaudeApiException("Claude API error: " + e.getStatusCode(), e);
        } catch (ClaudeApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Claude API unexpected error after {}ms: {}",
                System.currentTimeMillis() - startMs, e.getMessage());
            throw new ClaudeApiException("Claude API call failed", e);
        }
    }

    // ════════════════════════════════════════
    // INSIGHT GENERATION (batch call)
    // ════════════════════════════════════════

    /**
     * Generate insights — expects JSON array in response.
     * Uses higher token limit for richer output.
     */
    public String generateInsights(String systemPrompt, String userPrompt) {
        return chat(systemPrompt, List.of(Map.of("role", "user", "content", userPrompt)), 2048);
    }

    // ════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════

    private WebClient buildWebClient() {
        return webClientBuilder
            .baseUrl(ANTHROPIC_API_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("x-api-key",         apiKey)
            .defaultHeader("anthropic-version", ANTHROPIC_VERSION)
            .codecs(c -> c.defaultCodecs().maxInMemorySize(4 * 1024 * 1024)) // 4MB
            .build();
    }

    private boolean isRateLimitOrServerError(Throwable ex) {
        if (ex instanceof WebClientResponseException wcEx) {
            int status = wcEx.getStatusCode().value();
            return status == 429    // Rate limited
                || status == 529    // Anthropic overloaded
                || status >= 500;   // Server error
        }
        return false; // Don't retry on 4xx client errors
    }

    // ════════════════════════════════════════
    // Request / Response records
    // ════════════════════════════════════════

    public record ClaudeRequest(
        String model,
        @JsonProperty("max_tokens") int maxTokens,
        String system,
        List<Map<String, String>> messages
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ClaudeResponse(
        String id,
        List<ContentBlock> content,
        String model,
        @JsonProperty("stop_reason") String stopReason,
        Usage usage
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentBlock(String type, String text) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(
        @JsonProperty("input_tokens")  int inputTokens,
        @JsonProperty("output_tokens") int outputTokens
    ) {}

    // Custom exception — callers can catch and provide fallback
    public static class ClaudeApiException extends RuntimeException {
        public ClaudeApiException(String message) { super(message); }
        public ClaudeApiException(String message, Throwable cause) { super(message, cause); }
    }
}
