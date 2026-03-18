package com.investrac.ai.scheduler;

import com.investrac.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Nightly insight generation — runs at 11:30 PM IST every day.
 *
 * Flow:
 *   1. Fetch all active user IDs from user-service
 *   2. For each user: generate 3-5 insights via Claude API
 *   3. Save to ai_insights table (shown on home screen next morning)
 *
 * Rate limiting:
 *   - Adds 500ms delay between users to avoid Anthropic rate limits
 *   - Skips users who already have insights generated today
 *   - Max 100 users per run (adjust via config)
 *
 * Cron: "0 30 23 * * *" = every night at 23:30:00 IST
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NightlyInsightScheduler {

    private final AiService         aiService;
    private final WebClient.Builder webClientBuilder;

    @Value("${services.gateway-url:http://api-gateway:8080}")
    private String gatewayUrl;

    @Value("${services.internal-token:}")
    private String internalToken;

    @Value("${ai.insight-scheduler.max-users-per-run:100}")
    private int maxUsersPerRun;

    @Value("${ai.insight-scheduler.delay-ms-between-users:500}")
    private long delayBetweenUsersMs;

    @Value("${ai.insight-scheduler.enabled:true}")
    private boolean enabled;

    @Scheduled(cron = "0 30 23 * * *", zone = "Asia/Kolkata")
    public void generateNightlyInsights() {
        if (!enabled) {
            log.info("NightlyInsightScheduler is disabled — skipping");
            return;
        }

        Instant start = Instant.now();
        log.info("=== Nightly Insight Generation STARTED at {} ===", start);

        // 1. Fetch active user IDs from user-service
        List<Long> userIds = fetchActiveUserIds();

        if (userIds.isEmpty()) {
            log.warn("No active users found — skipping nightly insights");
            return;
        }

        log.info("Generating insights for {} users (max {})", userIds.size(), maxUsersPerRun);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger skipped = new AtomicInteger(0);
        AtomicInteger failed  = new AtomicInteger(0);

        userIds.stream().limit(maxUsersPerRun).forEach(userId -> {
            try {
                // 2. Generate insights (service will skip if already done today)
                var insights = aiService.generateInsights(userId);

                if (insights.isEmpty()) {
                    skipped.incrementAndGet();
                    log.debug("Insights skipped for userId={} (already generated today)", userId);
                } else {
                    success.incrementAndGet();
                    log.debug("Insights generated for userId={}: {} insights", userId, insights.size());
                }

                // 3. Rate limiting — pause between users
                if (delayBetweenUsersMs > 0) {
                    Thread.sleep(delayBetweenUsersMs);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Nightly scheduler interrupted");
            } catch (Exception e) {
                failed.incrementAndGet();
                // Log error but continue with next user
                log.error("Insight generation failed for userId={}: {}", userId, e.getMessage());
            }
        });

        long durationMs = Instant.now().toEpochMilli() - start.toEpochMilli();
        log.info("=== Nightly Insights COMPLETE: success={} skipped={} failed={} duration={}ms ===",
            success.get(), skipped.get(), failed.get(), durationMs);
    }

    /**
     * Fetch active user IDs from user-service via API Gateway.
     * Returns empty list on failure (scheduler skips gracefully).
     */
    private List<Long> fetchActiveUserIds() {
        try {
            String url = gatewayUrl != null ? gatewayUrl : "http://localhost:8080";
            var response = webClientBuilder
                .baseUrl(url)
                .defaultHeader("Authorization", "Bearer " + internalToken)
                .build()
                .get()
                .uri("/api/users/ids/active")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .block();

            if (response == null) return List.of();

            Object data = response.get("data");
            if (data instanceof List) {
                return ((List<?>) data).stream()
                    .filter(id -> id instanceof Number)
                    .map(id -> ((Number) id).longValue())
                    .toList();
            }
            return List.of();

        } catch (Exception e) {
            log.error("Failed to fetch active user IDs: {}", e.getMessage());
            return List.of();
        }
    }
}
