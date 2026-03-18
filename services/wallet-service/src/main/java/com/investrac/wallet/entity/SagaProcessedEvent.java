package com.investrac.wallet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Idempotency guard for SAGA events.
 * Before processing any Kafka event, check if sagaId was already processed.
 * Prevents double-deduction if Kafka redelivers a message.
 */
@Entity
@Table(name = "saga_processed_events",
    indexes = @Index(name = "idx_saga_id", columnList = "saga_id", unique = true)
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SagaProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saga_id", nullable = false, unique = true, length = 36)
    private String sagaId;

    @Column(name = "event_type", length = 50)
    private String eventType;

    @CreationTimestamp
    @Column(name = "processed_at", updatable = false)
    private Instant processedAt;
}
