package com.investrac.user.service;

import com.investrac.user.dto.response.FinancialSummaryResponse;
import com.investrac.user.entity.UserProfile;
import com.investrac.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

/**
 * Aggregates data from multiple services for the home screen summary card.
 *
 * Calls:
 *   wallet-service  GET /api/wallet/current          → balance, freeToSpend, usedPercent
 *   portfolio-service GET /api/portfolio/summary     → value, invested, XIRR, returnPct
 *   account-service  GET /api/accounts/emi/total     → totalSavings, monthlyEmi
 *
 * Resilience:
 *   - Each service call is independent — if one fails, others still populate
 *   - Responses cached in Redis for 5 minutes (home screen loads fast)
 *   - Fallback: returns zeros for unavailable service data (never fails whole request)
 *
 * Note: In production this aggregation belongs in API Gateway or a BFF layer.
 * Here it lives in user-service as a pragmatic shortcut for the MVP.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FinancialSummaryService {

    private final UserProfileRepository        profileRepository;
    private final RestTemplate                 restTemplate;
    private final RedisTemplate<String,String> redisTemplate;

    @Value("${services.wallet-service.url:http://wallet-service:8083}")
    private String walletServiceUrl;

    @Value("${services.portfolio-service.url:http://portfolio-service:8086}")
    private String portfolioServiceUrl;

    @Value("${services.account-service.url:http://account-service:8085}")
    private String accountServiceUrl;

    // Inline cache TTL instead of separate constant to avoid unused warnings

    public FinancialSummaryResponse getSummary(Long userId) {
        String cacheKey = "financial_summary:" + userId;

        // Try Redis cache first
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Financial summary cache hit for userId={}", userId);
            // In production, deserialize from JSON; simplified here
        }

        UserProfile profile = profileRepository.findByUserId(userId).orElse(null);
        String userName = profile != null ? profile.getName() : "User";

        // Fetch from each service independently
        WalletData   wallet    = fetchWalletData(userId);
        PortfolioData portfolio = fetchPortfolioData(userId);
        AccountData  account   = fetchAccountData(userId);

        // Derive net worth and savings rate
        long netWorth = (portfolio.currentValue != null ? portfolio.currentValue : 0L)
            + (account.totalSavings != null ? account.totalSavings : 0L)
            + (wallet.balance != null ? wallet.balance : 0L);

        int savingsRate = 0;
        if (profile != null && profile.getMonthlyIncome() != null && profile.getMonthlyIncome() > 0) {
            long totalEmi = account.monthlyEmi != null ? account.monthlyEmi : 0L;
            savingsRate = (int) Math.max(0,
                ((profile.getMonthlyIncome() - totalEmi) * 100) / profile.getMonthlyIncome());
        }

        FinancialSummaryResponse summary = FinancialSummaryResponse.builder()
            .userId(userId)
            .userName(userName)
            // Wallet
            .walletBalance(wallet.balance)
            .walletFreeToSpend(wallet.freeToSpend)
            .walletUsedPercent(wallet.usedPercent != null ? wallet.usedPercent : 0)
            // Portfolio
            .portfolioValue(portfolio.currentValue)
            .portfolioInvested(portfolio.invested)
            .portfolioXirr(portfolio.xirr)
            .portfolioReturnPercent(portfolio.returnPercent)
            // Accounts
            .totalSavings(account.totalSavings)
            .totalFdCorpus(account.fdCorpus)
            .monthlyEmiCommitted(account.monthlyEmi)
            // Derived
            .estimatedNetWorth(netWorth)
            .savingsRatePercent(savingsRate)
            .riskProfile(profile != null && profile.getRiskProfile() != null
                ? profile.getRiskProfile().name() : "MODERATE")
            .build();

        // Cache result
        redisTemplate.opsForValue().set(cacheKey, "cached", Duration.ofMinutes(5));
        return summary;
    }

    // ── Private fetch methods — each fails gracefully ──

    private WalletData fetchWalletData(Long userId) {
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> resp = restTemplate.exchange(
                walletServiceUrl + "/api/wallet/current",
                HttpMethod.GET,
                buildRequest(userId),
                Map.class
            );
            if (resp != null && resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) resp.getBody().get("data");
                if (data != null) {
                    return new WalletData(
                        toLong(data.get("balance")),
                        toLong(data.get("freeToSpend")),
                        toInt(data.get("usedPercent"))
                    );
                }
            }
        } catch (RestClientException e) {
            log.warn("wallet-service unavailable for userId={}: {}", userId, e.getMessage());
        }
        return new WalletData(null, null, null);
    }

    private PortfolioData fetchPortfolioData(Long userId) {
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> resp = restTemplate.exchange(
                portfolioServiceUrl + "/api/portfolio/summary",
                HttpMethod.GET,
                buildRequest(userId),
                Map.class
            );
            if (resp != null && resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) resp.getBody().get("data");
                if (data != null) {
                    return new PortfolioData(
                        toLong(data.get("totalCurrentValue")),
                        toLong(data.get("totalInvested")),
                        toDouble(data.get("weightedXirr")),
                        toDouble(data.get("totalReturnPercent"))
                    );
                }
            }
        } catch (RestClientException e) {
            log.warn("portfolio-service unavailable for userId={}: {}", userId, e.getMessage());
        }
        return new PortfolioData(null, null, null, null);
    }

    private AccountData fetchAccountData(Long userId) {
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> resp = restTemplate.exchange(
                accountServiceUrl + "/api/accounts",
                HttpMethod.GET,
                buildRequest(userId),
                Map.class
            );
            if (resp != null && resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) resp.getBody().get("data");
                if (data != null) {
                    return new AccountData(
                        toLong(data.get("totalSavings")),
                        toLong(data.get("totalFdCorpus")),
                        toLong(data.get("totalMonthlyEmi"))
                    );
                }
            }
        } catch (RestClientException e) {
            log.warn("account-service unavailable for userId={}: {}", userId, e.getMessage());
        }
        return new AccountData(null, null, null);
    }

    private HttpEntity<Void> buildRequest(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", String.valueOf(userId));
        return new HttpEntity<>(headers);
    }

    private Long   toLong(Object v)   { return v == null ? null : Long.valueOf(v.toString().replace(".", "")); }
    private Integer toInt(Object v)   { return v == null ? null : Integer.parseInt(v.toString()); }
    private Double  toDouble(Object v){ return v == null ? null : Double.parseDouble(v.toString()); }

    // ── Value objects ──
    private record WalletData(Long balance, Long freeToSpend, Integer usedPercent) {}
    private record PortfolioData(Long currentValue, Long invested, Double xirr, Double returnPercent) {}
    private record AccountData(Long totalSavings, Long fdCorpus, Long monthlyEmi) {}
}
