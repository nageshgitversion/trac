package com.investrac.common.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Published by: transaction-service
 * Consumed by:  wallet-service, notification-service
 *
 * Triggers the SAGA: transaction-service → wallet-service deduction
 */
public record TransactionCreatedEvent(
    String sagaId,              // Unique SAGA instance ID (UUID)
    Long transactionId,
    Long userId,
    Long walletId,
    BigDecimal amount,          // Always positive — sign determined by transactionType
    String transactionType,     // "expense" | "income" | "investment" | "savings"
    String category,
    String envelopeKey,         // Which wallet envelope to deduct from
    String transactionName,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate txDate,
    String source               // "manual" | "voice" | "ocr" | "scheduled"
) {
    public static final String TOPIC = "investrac.transaction.created";
}
