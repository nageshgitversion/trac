package com.investrac.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notif_user",    columnList = "user_id"),
    @Index(name = "idx_notif_unread",  columnList = "user_id, is_read"),
    @Index(name = "idx_notif_type",    columnList = "type"),
    @Index(name = "idx_notif_created", columnList = "created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    /** JSON string of extra data for deep-link routing in Angular */
    @Column(name = "data_json", length = 500)
    private String dataJson;

    @Builder.Default
    @Column(name = "is_read")
    private boolean read = false;

    @Builder.Default
    @Column(name = "is_sent")
    private boolean sent = false;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum NotificationType {
        TRANSACTION_COMPLETED,
        TRANSACTION_FAILED,
        WALLET_LOW_BALANCE,
        EMI_DUE,
        PORTFOLIO_SYNCED,
        AI_INSIGHT,
        WELCOME,
        PASSWORD_RESET,
        GENERAL
    }

    public enum NotificationChannel {
        PUSH,   // Firebase Cloud Messaging
        EMAIL,  // AWS SES / SendGrid
        SMS     // Twilio / AWS SNS (critical alerts only)
    }
}
