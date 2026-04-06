package com.fintrac.wallet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallets", indexes = {@Index(name = "idx_wallet_user", columnList = "user_id", unique = true)})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Builder.Default
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    @Column(length = 3, nullable = false)
    private String currency = "INR";

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
