package com.investrac.user.mapper;

import com.investrac.user.dto.response.UserPreferenceResponse;
import com.investrac.user.dto.response.UserProfileResponse;
import com.investrac.user.entity.UserPreference;
import com.investrac.user.entity.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileResponse toResponse(UserProfile p) {
        return UserProfileResponse.builder()
            .id(p.getId())
            .userId(p.getUserId())
            .name(p.getName())
            .email(p.getEmail())
            .phone(p.getPhone())
            .dateOfBirth(p.getDateOfBirth())
            .age(p.getAge())
            .profilePhotoUrl(p.getProfilePhotoUrl())
            .monthlyIncome(p.getMonthlyIncome())
            .riskProfile(p.getRiskProfile() != null ? p.getRiskProfile().name() : null)
            .taxRegime(p.getTaxRegime() != null ? p.getTaxRegime().name() : null)
            .retirementAge(p.getRetirementAge())
            .financialGoal(p.getFinancialGoal())
            .kycVerified(p.isKycVerified())
            .hasPan(p.hasPan())
            // SECURITY: Never return actual Aadhaar or PAN in response
            .aadhaarLast4(p.getAadhaarLast4())
            .language(p.getLanguage() != null ? p.getLanguage().name() : "EN")
            .currency(p.getCurrency())
            .theme(p.getTheme())
            .biometricEnabled(p.isBiometricEnabled())
            .createdAt(p.getCreatedAt())
            .updatedAt(p.getUpdatedAt())
            .build();
    }

    public UserPreferenceResponse toPreferenceResponse(UserPreference prefs) {
        return UserPreferenceResponse.builder()
            .userId(prefs.getUserId())
            .showBalanceOnHome(prefs.isShowBalanceOnHome())
            .showPortfolioOnHome(prefs.isShowPortfolioOnHome())
            .aiInsightsEnabled(prefs.isAiInsightsEnabled())
            .aiLanguage(prefs.getAiLanguage())
            .autoLockMinutes(prefs.getAutoLockMinutes())
            .showAmountsInLakhs(prefs.isShowAmountsInLakhs())
            .walletBudgetAlertPercent(prefs.getWalletBudgetAlertPercent())
            .updatedAt(prefs.getUpdatedAt())
            .build();
    }
}
