package com.investrac.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Per-feature app preferences.
 * Separated from UserProfile to avoid a bloated entity and allow
 * independent updates from the frontend settings screen.
 */
@Entity
@Table(name = "user_preferences",
    uniqueConstraints = @UniqueConstraint(name = "uk_pref_user", columnNames = "user_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // Dashboard preferences
    @Builder.Default
    @Column(name = "show_balance_on_home")
    private boolean showBalanceOnHome = true;

    @Builder.Default
    @Column(name = "show_portfolio_on_home")
    private boolean showPortfolioOnHome = true;

    // AI preferences
    @Builder.Default
    @Column(name = "ai_insights_enabled")
    private boolean aiInsightsEnabled = true;

    @Builder.Default
    @Column(name = "ai_language", length = 10)
    private String aiLanguage = "en";   // "en" | "hi" (Hinglish)

    // Security preferences
    @Builder.Default
    @Column(name = "auto_lock_minutes")
    private int autoLockMinutes = 5;

    @Builder.Default
    @Column(name = "show_amounts_in_lakhs")
    private boolean showAmountsInLakhs = true;   // ₹1.15L vs ₹1,15,000

    // Wallet preferences
    @Builder.Default
    @Column(name = "wallet_budget_alert_percent")
    private int walletBudgetAlertPercent = 80;   // Alert when 80% of envelope used

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
