package com.investrac.common.events;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Published by: transaction-service when SAGA fails
 * Consumed by:  notification-service
 */
public record TransactionFailedEvent(
    String sagaId,
    Long transactionId,
    Long userId,
    BigDecimal amount,
    String failureReason,
    String failureCode,
    Instant failedAt
) {
    public static final String TOPIC = "investrac.transaction.failed";
}
