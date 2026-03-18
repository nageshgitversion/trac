package com.investrac.common.events;

import java.math.BigDecimal;

/**
 * Published by: wallet-service when balance drops below threshold
 * Consumed by:  notification-service
 */
public record WalletLowBalanceEvent(
    Long userId,
    Long walletId,
    BigDecimal currentBalance,
    BigDecimal threshold,
    BigDecimal upcomingCommitments  // total EMIs + SIPs due this month
) {
    public static final String TOPIC = "investrac.wallet.low-balance";
}
