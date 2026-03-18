package com.investrac.wallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "wallet_envelopes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletEnvelope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "envelope_key", nullable = false, length = 30)
    private String envelopeKey;     // "food" | "groceries" | "transport" etc.

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;    // Display name

    @Column(name = "icon", length = 10)
    private String icon;

    @Builder.Default
    @Column(name = "budget", precision = 12, scale = 2)
    private BigDecimal budget = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "spent", precision = 12, scale = 2)
    private BigDecimal spent = BigDecimal.ZERO;

    public BigDecimal getRemaining() {
        return budget.subtract(spent);
    }

    public boolean isOverBudget() {
        return spent.compareTo(budget) > 0;
    }

    public void addSpending(BigDecimal amount) {
        this.spent = this.spent.add(amount);
    }
}
