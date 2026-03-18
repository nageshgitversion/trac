package com.investrac.ai.service;

import com.investrac.ai.prompt.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

/**
 * Assembles a UserContext by calling other INVESTRAC microservices.
 *
 * Calls (all via API Gateway, JWT header passed through):
 *   GET /api/wallet/current            → wallet-service
 *   GET /api/portfolio                 → portfolio-service
 *   GET /api/transactions/summary/{year}/{month} → transaction-service
 *   GET /api/accounts/emi/total        → account-service
 *   GET /api/users/me                  → user-service
 *
 * All calls have 3s timeout and return safe defaults on failure.
 * A partial context (some services unavailable) is still useful.
 *
 * SECURITY: Never log any response content — financial data.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserContextService {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.gateway-url:http://api-gateway:8080}")
    private String gatewayUrl;

    @Value("${security.jwt.internal-token:}")
    private String internalToken;

    public UserContext buildContext(Long userId) {
        log.debug("Building context for userId={}", userId);

        // Fetch all data — failures return safe defaults
        var walletData      = fetchWallet(userId);
        var portfolioData   = fetchPortfolio(userId);
        var txSummaryData   = fetchTransactionSummary(userId);
        var userProfileData = fetchUserProfile(userId);

        // Extract values safely
        String   name         = extractStr(userProfileData,  "name",           "User");
        String   taxRegime    = extractStr(userProfileData,  "taxRegime",       "NEW");
        String   riskProfile  = extractStr(userProfileData,  "riskProfile",     "MODERATE");

        BigDecimal income     = extractBD(walletData, "income",         BigDecimal.ZERO);
        BigDecimal balance    = extractBD(walletData, "balance",        BigDecimal.ZERO);
        BigDecimal committed  = extractBD(walletData, "committed",      BigDecimal.ZERO);
        BigDecimal free       = extractBD(walletData, "freeToSpend",    BigDecimal.ZERO);

        BigDecimal pfValue    = extractBD(portfolioData, "totalCurrentValue", BigDecimal.ZERO);
        BigDecimal pfInvested = extractBD(portfolioData, "totalInvested",     BigDecimal.ZERO);
        BigDecimal pfReturn   = extractBD(portfolioData, "totalReturn",       BigDecimal.ZERO);
        BigDecimal pfRetPct   = extractBD(portfolioData, "totalReturnPercent",BigDecimal.ZERO);
        BigDecimal pfXirr     = extractBD(portfolioData, "xirr",              BigDecimal.ZERO);

        BigDecimal expense    = extractBD(txSummaryData, "totalExpense",   BigDecimal.ZERO).abs();
        BigDecimal savingsRt  = extractBD(txSummaryData, "savingsRate",    BigDecimal.ZERO);

        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> categories = (Map<String, BigDecimal>)
            txSummaryData.getOrDefault("categoryBreakdown", Map.of());

        // Build pending EMIs from committed wallet items
        List<UserContext.PendingEmi> emis = buildPendingEmis(userId);

        // 80C — simplified: assume ELSS portion of portfolio
        BigDecimal c80Used  = fetchSection80cUsed(userId);
        BigDecimal c80Limit = new BigDecimal("150000");

        log.debug("Context built for userId={} (amounts not logged for privacy)", userId);

        return new UserContext(
            name, taxRegime, riskProfile,
            income, balance, committed, free,
            pfValue, pfInvested, pfReturn, pfRetPct, pfXirr,
            expense, savingsRt,
            categories, emis,
            c80Used, c80Limit
        );
    }

    // ── Internal service calls ──

    private Map<String, Object> fetchWallet(Long userId) {
        return safeGet("/api/wallet/current", userId);
    }

    private Map<String, Object> fetchPortfolio(Long userId) {
        return safeGet("/api/portfolio", userId);
    }

    private Map<String, Object> fetchTransactionSummary(Long userId) {
        java.time.LocalDate now = java.time.LocalDate.now();
        return safeGet("/api/transactions/summary/" + now.getYear() + "/" + now.getMonthValue(), userId);
    }

    private Map<String, Object> fetchUserProfile(Long userId) {
        return safeGet("/api/users/me", userId);
    }

    @SuppressWarnings("unchecked")
    private List<UserContext.PendingEmi> buildPendingEmis(Long userId) {
        // Fetch from account-service
        try {
            var resp = safeGetList("/api/accounts", userId);
            return resp.stream()
                .filter(a -> {
                    String type = extractStr(a, "type", "");
                    return "LOAN".equals(type) || "RD".equals(type);
                })
                .filter(a -> extractBD(a, "emiAmount", BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0)
                .map(a -> new UserContext.PendingEmi(
                    extractStr(a, "name", "Account"),
                    extractBD(a, "emiAmount", BigDecimal.ZERO),
                    (int) extractBD(a, "emiDay", BigDecimal.ONE).longValue(),
                    extractStr(a, "type", "loan").toLowerCase()
                ))
                .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.warn("Could not fetch EMIs for userId={}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    private BigDecimal fetchSection80cUsed(Long userId) {
        // Simplified: sum ELSS + DEBT_MF invested amounts from portfolio
        // Full implementation reads from tax records
        return BigDecimal.ZERO; // TODO: implement when tax module added
    }

    /**
     * Generic safe GET — returns empty map on any failure.
     * Response is parsed into a Map but never logged.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> safeGet(String path, Long userId) {
        try {
            String safePath = path != null ? path : "/";
            var response = buildWebClient(userId)
                .get()
                .uri(safePath)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(3))
                .block();

            if (response == null) return Map.of();

            // Unwrap ApiResponse wrapper: response.data
            Object data = response.get("data");
            if (data instanceof Map) {
                return (Map<String, Object>) data;
            }
            return Map.of();

        } catch (Exception e) {
            log.warn("Context fetch failed for path={} userId={}: {}", path, userId, e.getMessage());
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> safeGetList(String path, Long userId) {
        try {
            String safePath = path != null ? path : "/";
            var response = buildWebClient(userId)
                .get()
                .uri(safePath)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(3))
                .block();
            if (response == null) return List.of();
            Object data = response.get("data");
            if (data instanceof Map) {
                Object accounts = ((Map<?,?>) data).get("accounts");
                if (accounts instanceof List) return (List<Map<String, Object>>) accounts;
            }
            return List.of();
        } catch (Exception e) {
            log.warn("Context list fetch failed path={}: {}", path, e.getMessage());
            return List.of();
        }
    }

    private WebClient buildWebClient(Long userId) {
        String url = gatewayUrl != null ? gatewayUrl : "http://localhost:8080";
        return webClientBuilder
            .baseUrl(url)
            .defaultHeader("X-User-Id",      String.valueOf(userId))
            .defaultHeader("Authorization",   "Bearer " + internalToken)
            .build();
    }

    // ── Safe extractors ──
    private String extractStr(Map<String, Object> map, String key, String def) {
        Object val = map.get(key);
        return val != null ? String.valueOf(val) : def;
    }

    private BigDecimal extractBD(Map<String, Object> map, String key, BigDecimal def) {
        Object val = map.get(key);
        if (val == null) return def;
        try {
            return new BigDecimal(String.valueOf(val));
        } catch (Exception e) {
            return def;
        }
    }
}
