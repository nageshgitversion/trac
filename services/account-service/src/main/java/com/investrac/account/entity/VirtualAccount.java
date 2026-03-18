package com.investrac.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "virtual_accounts", indexes = {
    @Index(name = "idx_acc_user",    columnList = "user_id"),
    @Index(name = "idx_acc_type",    columnList = "type"),
    @Index(name = "idx_acc_emi_day", columnList = "emi_day")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VirtualAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Builder.Default
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate = BigDecimal.ZERO;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "maturity_amount", precision = 15, scale = 2)
    private BigDecimal maturityAmount;

    // EMI amount (for loan and RD)
    @Builder.Default
    @Column(name = "emi_amount", precision = 12, scale = 2)
    private BigDecimal emiAmount = BigDecimal.ZERO;

    // Day of month EMI is due (1-31)
    @Column(name = "emi_day")
    private Integer emiDay;

    // For savings — linked account that receives FD/RD maturity proceeds
    @Column(name = "linked_acc_id")
    private Long linkedAccId;

    // For savings accounts — savings goal target
    @Column(name = "goal_amount", precision = 15, scale = 2)
    private BigDecimal goalAmount;

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

    public boolean hasEmiSchedule() {
        return emiAmount != null
            && emiAmount.compareTo(BigDecimal.ZERO) > 0
            && emiDay != null
            && emiDay >= 1
            && emiDay <= 31;
    }

    public boolean isEmiDueToday() {
        return hasEmiSchedule()
            && LocalDate.now().getDayOfMonth() == emiDay;
    }

    public boolean isMaturingSoon(int withinDays) {
        return maturityDate != null
            && !maturityDate.isBefore(LocalDate.now())
            && ChronoUnit.DAYS.between(LocalDate.now(), maturityDate) <= withinDays;
    }

    public boolean hasGoal() {
        return goalAmount != null && goalAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public int getGoalProgressPercent() {
        if (!hasGoal() || balance == null) return 0;
        return balance
            .multiply(BigDecimal.valueOf(100))
            .divide(goalAmount, 0, java.math.RoundingMode.HALF_UP)
            .min(BigDecimal.valueOf(100))
            .intValue();
    }

    public enum AccountType {
        SAVINGS,    // Emergency fund, goal-based savings
        FD,         // Fixed Deposit — lump sum, earns interest till maturity
        RD,         // Recurring Deposit — monthly EMI, earns interest
        LOAN        // Home loan, car loan — tracks outstanding + EMI
    }
}
