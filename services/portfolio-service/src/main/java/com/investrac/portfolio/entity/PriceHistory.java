package com.investrac.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Daily price snapshot for a holding.
 *
 * Unique constraint: one price record per holding per day.
 * Used for:
 *   - Portfolio value charts (last 30 / 90 / 365 days)
 *   - Return calculation over time periods
 *   - AI service anomaly detection
 *
 * Populated by: PortfolioPriceSyncScheduler (8 PM weekdays)
 */
@Entity
@Table(name = "price_history",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_price_holding_date",
        columnNames = {"holding_id", "recorded_at"}
    ),
    indexes = {
        @Index(name = "idx_ph_holding",  columnList = "holding_id"),
        @Index(name = "idx_ph_date",     columnList = "recorded_at")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "holding_id", nullable = false)
    private Long holdingId;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal price;

    @Column(name = "recorded_at", nullable = false)
    private LocalDate recordedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
