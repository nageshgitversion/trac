package com.investrac.common.events;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Published by: transaction-service after wallet confirms debit
 * Consumed by:  notification-service
 */
public record TransactionCompletedEvent(
    String sagaId,
    Long transactionId,
    Long userId,
    BigDecimal amount,
    String transactionName,
    String category,
    Instant completedAt
) {
    public static final String TOPIC = "investrac.transaction.completed";
}
