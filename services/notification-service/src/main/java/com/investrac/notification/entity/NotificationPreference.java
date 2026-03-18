package com.investrac.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * User notification preferences.
 * Created with all-enabled defaults on first notification send.
 */
@Entity
@Table(name = "notification_preferences",
    uniqueConstraints = @UniqueConstraint(name = "uk_pref_user", columnNames = "user_id")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /** Firebase Cloud Messaging device token for push notifications */
    @Column(name = "fcm_token", length = 512)
    private String fcmToken;

    @Column(name = "email", length = 150)
    private String email;

    // Per-type toggles
    @Builder.Default @Column(name = "push_enabled")          private boolean pushEnabled         = true;
    @Builder.Default @Column(name = "email_enabled")         private boolean emailEnabled        = true;
    @Builder.Default @Column(name = "transaction_notif")     private boolean transactionNotif    = true;
    @Builder.Default @Column(name = "emi_notif")             private boolean emiNotif            = true;
    @Builder.Default @Column(name = "wallet_alert_notif")    private boolean walletAlertNotif    = true;
    @Builder.Default @Column(name = "portfolio_notif")       private boolean portfolioNotif      = false; // off by default (too frequent)
    @Builder.Default @Column(name = "ai_insight_notif")      private boolean aiInsightNotif      = true;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
