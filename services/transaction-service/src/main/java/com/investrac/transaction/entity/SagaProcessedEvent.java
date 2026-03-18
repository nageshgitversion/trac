package com.investrac.transaction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "saga_processed_events",
    indexes = @Index(name = "idx_tx_saga_id", columnList = "saga_id", unique = true))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SagaProcessedEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saga_id", nullable = false, unique = true, length = 36)
    private String sagaId;

    @Column(name = "event_type", length = 60)
    private String eventType;

    @Column(name = "result", length = 20)
    private String result;          // "COMPLETED" | "FAILED"

    @CreationTimestamp
    @Column(name = "processed_at", updatable = false)
    private Instant processedAt;
}
