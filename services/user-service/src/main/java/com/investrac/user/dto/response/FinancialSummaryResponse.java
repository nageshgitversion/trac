package com.investrac.user.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Aggregated financial summary for the home screen hero card.
 * Compiled by user-service by calling other services via HTTP.
 * Cached in Redis for 5 minutes.
 */
@Data
@Builder
public class FinancialSummaryResponse {
    private Long   userId;
    private String userName;

    // From wallet-service
    private Long walletBalance;
    private Long walletFreeToSpend;
    private int  walletUsedPercent;

    // From portfolio-service
    private Long    portfolioValue;
    private Long    portfolioInvested;
    private Double  portfolioXirr;
    private Double  portfolioReturnPercent;

    // From account-service
    private Long totalSavings;
    private Long totalFdCorpus;
    private Long monthlyEmiCommitted;

    // Derived
    private Long   estimatedNetWorth;
    private int    savingsRatePercent;
    private String riskProfile;
}
