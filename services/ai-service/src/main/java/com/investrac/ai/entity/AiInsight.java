package com.investrac.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "ai_insights", indexes = {
    @Index(name = "idx_insight_user",     columnList = "user_id"),
    @Index(name = "idx_insight_unread",   columnList = "user_id, is_read"),
    @Index(name = "idx_insight_type",     columnList = "type"),
    @Index(name = "idx_insight_generated",columnList = "generated_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AiInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InsightType type;

    /** 1 = highest priority, 5 = lowest */
    @Builder.Default
    @Column(nullable = false)
    private int priority = 3;

    @Builder.Default
    @Column(name = "is_read")
    private boolean read = false;

    @Builder.Default
    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt = Instant.now();

    public enum InsightType {
        SPENDING,   // Spending pattern alerts and advice
        SAVINGS,    // Savings rate improvements
        TAX,        // Tax saving opportunities (80C, NPS, etc.)
        PORTFOLIO,  // Portfolio rebalancing, SIP suggestions
        EMI         // EMI due date, loan closure advice
    }
}
