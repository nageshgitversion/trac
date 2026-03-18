package com.investrac.common.events;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Published by: portfolio-service after daily price sync
 * Consumed by:  notification-service, ai-service
 */
public record PortfolioSyncedEvent(
    Long userId,
    int syncedHoldingsCount,
    BigDecimal totalValue,
    BigDecimal previousValue,
    BigDecimal changePercent,
    Instant syncedAt
) {
    public static final String TOPIC = "investrac.portfolio.synced";
}
