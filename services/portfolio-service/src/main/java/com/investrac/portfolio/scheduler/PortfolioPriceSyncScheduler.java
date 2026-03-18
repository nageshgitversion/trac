package com.investrac.portfolio.scheduler;

import com.investrac.common.events.PortfolioSyncedEvent;
import com.investrac.portfolio.entity.Holding;
import com.investrac.portfolio.outbox.PortfolioOutboxService;
import com.investrac.portfolio.repository.HoldingRepository;
import com.investrac.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Automated daily price sync for all updatable holdings.
 *
 * Schedule: 8:00 PM IST, Monday–Friday
 * Why 8 PM: NSE closes at 3:30 PM, NAV updates reach mfapi.in by ~7 PM
 *
 * Strategy:
 *   1. Fetch all holdings where is_updatable = true across ALL users
 *   2. Deduplicate by symbol — if 100 users hold HDFC Nifty 50,
 *      we call mfapi.in ONCE and apply price to all 100 holdings
 *   3. Save updated prices + record in price_history
 *   4. Publish PortfolioSyncedEvent per user via Outbox
 *
 * Failure handling:
 *   - Per-holding failure is isolated (others continue)
 *   - Failed symbols logged for ops alerting
 *   - No transaction rollback on price failure (keep old prices)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PortfolioPriceSyncScheduler {

    private final HoldingRepository  holdingRepository;
    private final PortfolioService   portfolioService;
    private final PortfolioOutboxService outboxService;

    /**
     * Main scheduled sync — 8:00 PM IST, Mon–Fri.
     * Cron: second minute hour dayOfMonth month dayOfWeek
     */
    @Scheduled(cron = "0 0 20 * * MON-FRI", zone = "Asia/Kolkata")
    @Transactional
    public void scheduledSync() {
        log.info("=== Portfolio Price Sync STARTED at {} ===", Instant.now());
        Instant startTime = Instant.now();

        // 1. Get all updatable holdings across all users
        List<Holding> allUpdatable = holdingRepository.findAllUpdatableHoldings();

        if (allUpdatable.isEmpty()) {
            log.info("No updatable holdings found — sync complete");
            return;
        }

        log.info("Found {} updatable holdings to sync", allUpdatable.size());

        // 2. Deduplicate by symbol — fetch each unique symbol once
        Map<String, BigDecimal> priceCache = new HashMap<>();
        AtomicInteger syncedCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);
        List<String> failedSymbols = new ArrayList<>();

        // Group by symbol to batch API calls
        Map<String, List<Holding>> bySymbol = allUpdatable.stream()
            .collect(Collectors.groupingBy(Holding::getSymbol));

        bySymbol.forEach((symbol, holdingsForSymbol) -> {
            // Fetch price once per symbol
            BigDecimal newPrice;
            if (priceCache.containsKey(symbol)) {
                newPrice = priceCache.get(symbol);
            } else {
                // Use first holding's type to determine API
                Holding sampleHolding = holdingsForSymbol.get(0);
                Optional<BigDecimal> fetched = portfolioService.fetchPrice(sampleHolding);

                if (fetched.isEmpty()) {
                    log.warn("Price fetch FAILED for symbol={}", symbol);
                    failedCount.addAndGet(holdingsForSymbol.size());
                    failedSymbols.add(symbol);
                    return;  // skip all holdings with this symbol
                }

                newPrice = fetched.get();
                priceCache.put(symbol, newPrice);
                log.debug("Price fetched: symbol={} price={}", symbol, newPrice);
            }

            // 3. Apply new price to all holdings with this symbol
            final BigDecimal finalPrice = newPrice;
            holdingsForSymbol.forEach(holding -> {
                try {
                    portfolioService.updateHoldingPrice(holding, finalPrice);
                    syncedCount.incrementAndGet();
                    log.debug("Updated holding id={} symbol={} price={} value={}",
                        holding.getId(), symbol, finalPrice, holding.getCurrentValue());
                } catch (Exception e) {
                    log.error("Failed to update holding id={} symbol={}: {}",
                        holding.getId(), symbol, e.getMessage(), e);
                    failedCount.incrementAndGet();
                }
            });
        });

        // 4. Publish PortfolioSyncedEvent per user via Outbox
        publishSyncEventsPerUser(allUpdatable, syncedCount.get());

        long durationMs = Instant.now().toEpochMilli() - startTime.toEpochMilli();
        log.info("=== Portfolio Sync COMPLETE: synced={} failed={} duration={}ms ===",
            syncedCount.get(), failedCount.get(), durationMs);

        if (!failedSymbols.isEmpty()) {
            log.warn("Failed symbols: {}", failedSymbols);
        }
    }

    /**
     * Publish one PortfolioSyncedEvent per user whose holdings were updated.
     */
    private void publishSyncEventsPerUser(List<Holding> updatedHoldings, int totalSynced) {
        // Group synced holdings by userId
        Map<Long, List<Holding>> byUser = updatedHoldings.stream()
            .collect(Collectors.groupingBy(Holding::getUserId));

        byUser.forEach((userId, userHoldings) -> {
            try {
                BigDecimal totalVal = holdingRepository.sumCurrentValue(userId);

                outboxService.publish(
                    PortfolioSyncedEvent.TOPIC,
                    new PortfolioSyncedEvent(
                        userId,
                        userHoldings.size(),
                        totalVal,
                        BigDecimal.ZERO,    // previousValue — populate from price_history if needed
                        BigDecimal.ZERO,    // changePercent — same
                        Instant.now()
                    )
                );
            } catch (Exception e) {
                log.error("Failed to publish PortfolioSyncedEvent for userId={}: {}", userId, e.getMessage());
            }
        });

        log.debug("PortfolioSyncedEvent published for {} users", byUser.size());
    }
}
