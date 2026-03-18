package com.investrac.user.service;

import com.investrac.user.dto.response.FinancialSummaryResponse;
import com.investrac.user.entity.UserProfile;
import com.investrac.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialSummaryService Tests")
class FinancialSummaryServiceTest {

    @Mock UserProfileRepository        profileRepository;
    @Mock RestTemplate                 restTemplate;
    @Mock RedisTemplate<String,String> redisTemplate;
    @Mock ValueOperations<String,String> valueOps;

    @InjectMocks
    FinancialSummaryService summaryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(summaryService, "walletServiceUrl",    "http://wallet-service:8083");
        ReflectionTestUtils.setField(summaryService, "portfolioServiceUrl", "http://portfolio-service:8086");
        ReflectionTestUtils.setField(summaryService, "accountServiceUrl",   "http://account-service:8085");
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null); // No cache
    }

    @Test
    @DisplayName("getSummary: assembles data from all 3 services correctly")
    void getSummary_AllServicesAvailable() {
        UserProfile profile = UserProfile.builder()
            .userId(100L).name("Arjun Kumar")
            .monthlyIncome(115000L)
            .riskProfile(UserProfile.RiskProfile.MODERATE)
            .build();
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(profile));

        // Wallet returns ₹51,400 balance
        Map<String,Object> walletResp = Map.of(
            "success", true,
            "data", Map.of("balance", 51400, "freeToSpend", 51400, "usedPercent", 55));
        when(restTemplate.exchange(contains("/api/wallet"), any(), any(), eq(Map.class)))
            .thenReturn(org.springframework.http.ResponseEntity.ok(walletResp));

        // Portfolio returns ₹18.6L
        Map<String,Object> portResp = Map.of(
            "success", true,
            "data", Map.of("totalCurrentValue", 1860000, "totalInvested", 1508000,
                           "weightedXirr", 16.2, "totalReturnPercent", 23.3));
        when(restTemplate.exchange(contains("/api/portfolio"), any(), any(), eq(Map.class)))
            .thenReturn(org.springframework.http.ResponseEntity.ok(portResp));

        // Accounts returns savings
        Map<String,Object> accResp = Map.of(
            "success", true,
            "data", Map.of("totalSavings", 320000, "totalFdCorpus", 850000,
                           "totalMonthlyEmi", 53600));
        when(restTemplate.exchange(contains("/api/accounts"), any(), any(), eq(Map.class)))
            .thenReturn(org.springframework.http.ResponseEntity.ok(accResp));

        FinancialSummaryResponse result = summaryService.getSummary(100L);

        assertThat(result.getUserName()).isEqualTo("Arjun Kumar");
        assertThat(result.getWalletBalance()).isEqualTo(51400L);
        assertThat(result.getPortfolioXirr()).isEqualTo(16.2);
        assertThat(result.getEstimatedNetWorth()).isPositive();
        assertThat(result.getRiskProfile()).isEqualTo("MODERATE");
    }

    @Test
    @DisplayName("getSummary: returns partial data gracefully when wallet-service is down")
    void getSummary_WalletDown_PartialResponse() {
        UserProfile profile = UserProfile.builder()
            .userId(100L).name("Arjun").monthlyIncome(100000L)
            .riskProfile(UserProfile.RiskProfile.MODERATE).build();
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(profile));

        // Wallet throws connection error
        when(restTemplate.exchange(contains("/api/wallet"), any(), any(), eq(Map.class)))
            .thenThrow(new ResourceAccessException("wallet-service down"));

        // Portfolio succeeds
        Map<String,Object> portResp = Map.of(
            "success", true,
            "data", Map.of("totalCurrentValue", 500000, "totalInvested", 400000,
                           "weightedXirr", 12.5, "totalReturnPercent", 25.0));
        when(restTemplate.exchange(contains("/api/portfolio"), any(), any(), eq(Map.class)))
            .thenReturn(org.springframework.http.ResponseEntity.ok(portResp));

        // Account down too
        when(restTemplate.exchange(contains("/api/accounts"), any(), any(), eq(Map.class)))
            .thenThrow(new ResourceAccessException("account-service down"));

        // Should NOT throw — degrades gracefully
        assertThatCode(() -> summaryService.getSummary(100L))
            .doesNotThrowAnyException();

        FinancialSummaryResponse result = summaryService.getSummary(100L);
        // Wallet data null, portfolio populated
        assertThat(result.getWalletBalance()).isNull();
        assertThat(result.getPortfolioValue()).isEqualTo(500000L);
    }

    @Test
    @DisplayName("getSummary: returns zeros/nulls when all services are down")
    void getSummary_AllServicesDown_ReturnsEmptySummary() {
        UserProfile profile = UserProfile.builder()
            .userId(100L).name("Arjun")
            .riskProfile(UserProfile.RiskProfile.CONSERVATIVE).build();
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(profile));

        when(restTemplate.exchange(any(), any(), any(), eq(Map.class)))
            .thenThrow(new ResourceAccessException("All services down"));

        FinancialSummaryResponse result = summaryService.getSummary(100L);

        assertThat(result.getUserName()).isEqualTo("Arjun");
        assertThat(result.getWalletBalance()).isNull();
        assertThat(result.getPortfolioValue()).isNull();
        assertThat(result.getEstimatedNetWorth()).isEqualTo(0L);
    }

    @Test
    @DisplayName("getSummary: calculates savings rate from monthly income and EMI")
    void getSummary_CalculatesSavingsRate() {
        UserProfile profile = UserProfile.builder()
            .userId(100L).name("Arjun").monthlyIncome(115000L)
            .riskProfile(UserProfile.RiskProfile.MODERATE).build();
        when(profileRepository.findByUserId(100L)).thenReturn(Optional.of(profile));

        when(restTemplate.exchange(any(), any(), any(), eq(Map.class)))
            .thenThrow(new ResourceAccessException("down"));

        FinancialSummaryResponse result = summaryService.getSummary(100L);

        // With 0 committed EMI: savingsRate = (115000 - 0) / 115000 * 100 = 100%
        assertThat(result.getSavingsRatePercent()).isEqualTo(100);
    }

    @Test
    @DisplayName("getSummary: handles missing profile gracefully")
    void getSummary_NoProfile_UsesDefaultName() {
        when(profileRepository.findByUserId(999L)).thenReturn(Optional.empty());
        when(restTemplate.exchange(any(), any(), any(), eq(Map.class)))
            .thenThrow(new ResourceAccessException("down"));

        FinancialSummaryResponse result = summaryService.getSummary(999L);

        assertThat(result.getUserName()).isEqualTo("User");  // Default fallback name
        assertThat(result.getRiskProfile()).isEqualTo("MODERATE"); // Default risk
    }
}
