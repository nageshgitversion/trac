package com.investrac.transaction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_tx_user_date",   columnList = "user_id, tx_date"),
    @Index(name = "idx_tx_user_type",   columnList = "user_id, type"),
    @Index(name = "idx_tx_user_cat",    columnList = "user_id, category"),
    @Index(name = "idx_tx_saga",        columnList = "saga_id"),
    @Index(name = "idx_tx_status",      columnList = "status"),
    @Index(name = "idx_tx_wallet",      columnList = "wallet_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallet_id")
    private Long walletId;          // nullable — transaction may not be wallet-linked

    @Column(name = "account_id")
    private Long accountId;         // nullable — for account-specific transactions

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Always positive value — direction determined by type.
     * expense/investment/savings = debit from wallet.
     * income = credit to wallet.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "envelope_key", length = 30)
    private String envelopeKey;     // Which wallet envelope was debited

    @Column(name = "tx_date", nullable = false)
    private LocalDate txDate;

    @Column(length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private TransactionSource source = TransactionSource.MANUAL;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "saga_id", length = 36)
    private String sagaId;          // SAGA correlation ID

    @Builder.Default
    @Column(name = "is_deleted")
    private boolean deleted = false;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;   // Populated when status = FAILED

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── Enums ──

    public enum TransactionType {
        INCOME, EXPENSE, INVESTMENT, SAVINGS, TRANSFER
    }

    public enum TransactionSource {
        MANUAL, VOICE, OCR, SCHEDULED, IMPORT
    }

    public enum TransactionStatus {
        PENDING,    // Saved, SAGA in progress
        COMPLETED,  // Wallet debited/credited successfully
        FAILED,     // SAGA failed (insufficient balance etc.)
        CANCELLED   // Soft deleted
    }

    // ── Business methods ──

    public boolean isDebit() {
        return type == TransactionType.EXPENSE
            || type == TransactionType.INVESTMENT
            || type == TransactionType.SAVINGS;
    }

    public boolean isCredit() {
        return type == TransactionType.INCOME;
    }

    public void markCompleted() {
        this.status = TransactionStatus.COMPLETED;
    }

    public void markFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
    }

    public void softDelete() {
        this.deleted = true;
        this.status = TransactionStatus.CANCELLED;
    }
}
