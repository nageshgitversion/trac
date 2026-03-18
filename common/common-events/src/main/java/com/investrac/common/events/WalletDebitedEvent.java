package com.investrac.common.events;

import java.math.BigDecimal;

/**
 * Published by: wallet-service
 * Consumed by:  transaction-service
 *
 * Response to TransactionCreatedEvent SAGA step.
 */
public record WalletDebitedEvent(
    String sagaId,
    Long transactionId,
    Long userId,
    Long walletId,
    BigDecimal debitedAmount,
    boolean success,
    String failureReason,
    String failureCode
) {
    public static final String TOPIC = "investrac.wallet.debited";

    public static WalletDebitedEvent success(String sagaId, Long txId, Long userId,
                                             Long walletId, BigDecimal amount) {
        return new WalletDebitedEvent(sagaId, txId, userId, walletId, amount, true, null, null);
    }

    public static WalletDebitedEvent failure(String sagaId, Long txId, Long userId,
                                             Long walletId, BigDecimal amount,
                                             String code, String reason) {
        return new WalletDebitedEvent(sagaId, txId, userId, walletId, amount, false, reason, code);
    }
}
