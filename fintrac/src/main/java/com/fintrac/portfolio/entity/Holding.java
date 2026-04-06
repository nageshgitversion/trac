package com.fintrac.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "holdings", indexes = {@Index(name = "idx_holding_user", columnList = "user_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Holding {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private HoldingType type;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 20)
    private String symbol;

    @Column(precision = 19, scale = 6)
    private BigDecimal units;

    @Column(name = "buy_price", precision = 19, scale = 4)
    private BigDecimal buyPrice;

    @Column(name = "current_price", precision = 19, scale = 4)
    private BigDecimal currentPrice;

    @Column(length = 500)
    private String note;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
