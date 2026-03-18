package com.investrac.transaction.dto;

import com.investrac.transaction.entity.Transaction;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private Long userId;
    private Long walletId;
    private Long accountId;
    private Transaction.TransactionType type;
    private String category;
    private String name;
    private BigDecimal amount;
    private String envelopeKey;
    private LocalDate txDate;
    private String note;
    private Transaction.TransactionSource source;
    private Transaction.TransactionStatus status;
    private String sagaId;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;
}
