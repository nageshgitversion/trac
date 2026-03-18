package com.investrac.wallet.outbox;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "outbox_events",
    indexes = @Index(name = "idx_wallet_outbox_status", columnList = "status")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Builder.Default
    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "published_at")
    private Instant publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum Status { PENDING, PUBLISHED, FAILED }
}
