package com.investrac.wallet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wallets",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_wallet_user_month",
        columnNames = {"user_id", "month"}
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "month", nullable = false, length = 7)   // 'YYYY-MM'
    private String month;

    @Column(name = "income", nullable = false, precision = 15, scale = 2)
    private BigDecimal income;

    @Builder.Default
    @Column(name = "topup", precision = 15, scale = 2)
    private BigDecimal topup = BigDecimal.ZERO;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Builder.Default
    @Column(name = "committed", precision = 15, scale = 2)
    private BigDecimal committed = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "is_active")
    private boolean active = true;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WalletEnvelope> envelopes = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── Business logic ──
    public BigDecimal getFreeToSpend() {
        return income.add(topup).subtract(committed).max(BigDecimal.ZERO);
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    public void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public int getUsedPercent() {
        if (income.compareTo(BigDecimal.ZERO) == 0) return 0;
        BigDecimal total = income.add(topup);
        BigDecimal used  = total.subtract(balance);
        return used.multiply(BigDecimal.valueOf(100))
                   .divide(total, 0, java.math.RoundingMode.HALF_UP)
                   .intValue();
    }
}
