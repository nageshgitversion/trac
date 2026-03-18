package com.investrac.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
@Table(name = "holdings", indexes = {
    @Index(name = "idx_holding_user",        columnList = "user_id"),
    @Index(name = "idx_holding_type",        columnList = "type"),
    @Index(name = "idx_holding_symbol",      columnList = "symbol"),
    @Index(name = "idx_holding_updatable",   columnList = "is_updatable")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private HoldingType type;

    @Column(nullable = false, length = 200)
    private String name;

    /**
     * For Mutual Funds  : AMFI scheme code  (e.g. "119598" for HDFC Nifty 50)
     * For Stocks        : NSE symbol         (e.g. "INFY" → fetched as INFY.NS)
     * For Gold/SGB      : ISIN or series     (e.g. "SGBMAR29")
     * For NPS/PPF/Other : null (manual only)
     */
    @Column(length = 50)
    private String symbol;

    @Builder.Default
    @Column(precision = 14, scale = 4)
    private BigDecimal units = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "buy_price", precision = 14, scale = 4)
    private BigDecimal buyPrice = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "current_price", precision = 14, scale = 4)
    private BigDecimal currentPrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal invested;

    @Column(name = "current_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentValue;

    @Builder.Default
    @Column(precision = 6, scale = 2)
    private BigDecimal xirr = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "sip_amount", precision = 10, scale = 2)
    private BigDecimal sipAmount = BigDecimal.ZERO;

    /** true = price can be auto-fetched from external API (symbol is set) */
    @Builder.Default
    @Column(name = "is_updatable")
    private boolean updatable = false;

    @Column(name = "last_synced")
    private Instant lastSynced;

    @Column(length = 500)
    private String note;

    @Builder.Default
    @Column(name = "is_active")
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ════════════════════════════════════════
    // Business methods
    // ════════════════════════════════════════

    public BigDecimal getReturnAmount() {
        if (currentValue == null || invested == null) return BigDecimal.ZERO;
        return currentValue.subtract(invested);
    }

    public BigDecimal getReturnPercent() {
        if (invested == null || invested.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getReturnAmount()
            .multiply(BigDecimal.valueOf(100))
            .divide(invested, 2, RoundingMode.HALF_UP);
    }

    public boolean isProfit() {
        return getReturnAmount().compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Recalculate currentValue from units × current price.
     * Called after every price sync.
     */
    public void refreshCurrentValue() {
        if (units != null && currentPrice != null
                && units.compareTo(BigDecimal.ZERO) > 0
                && currentPrice.compareTo(BigDecimal.ZERO) > 0) {
            this.currentValue = units.multiply(currentPrice)
                .setScale(2, RoundingMode.HALF_UP);
        }
    }

    public enum HoldingType {
        EQUITY_MF,   // Equity Mutual Funds (HDFC Nifty 50, Parag Parikh)
        STOCKS,      // Direct equities (INFY, TCS, HDFC Bank)
        DEBT_MF,     // Debt / Hybrid Mutual Funds
        NPS_PPF,     // NPS, PPF — manual, no live price
        GOLD_SGB,    // Sovereign Gold Bonds, digital gold
        FD,          // FD tracked in portfolio (separate from virtual accounts)
        OTHER        // Crypto, REIT, InvIT, international funds
    }
}
