package com.investrac.common.events;

import java.math.BigDecimal;

/**
 * Published by: wallet-service when income is added
 * Consumed by:  notification-service
 */
public record WalletCreditedEvent(
    String sagaId,
    Long transactionId,
    Long userId,
    Long walletId,
    BigDecimal creditedAmount,
    String source
) {
    public static final String TOPIC = "investrac.wallet.credited";
}
