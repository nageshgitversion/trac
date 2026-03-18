package com.investrac.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserPreferenceResponse {
    private Long    userId;
    private boolean showBalanceOnHome;
    private boolean showPortfolioOnHome;
    private boolean aiInsightsEnabled;
    private String  aiLanguage;
    private int     autoLockMinutes;
    private boolean showAmountsInLakhs;
    private int     walletBudgetAlertPercent;
    private Instant updatedAt;
}
