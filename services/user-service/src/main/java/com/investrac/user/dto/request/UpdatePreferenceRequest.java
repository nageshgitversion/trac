package com.investrac.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdatePreferenceRequest {
    private Boolean showBalanceOnHome;
    private Boolean showPortfolioOnHome;
    private Boolean aiInsightsEnabled;

    @Pattern(regexp = "^(en|hi)$", message = "AI language must be 'en' or 'hi'")
    private String aiLanguage;

    @Min(1) @Max(60)
    private Integer autoLockMinutes;

    private Boolean showAmountsInLakhs;

    @Min(50) @Max(100)
    private Integer walletBudgetAlertPercent;
}
