package com.investrac.portfolio.service;

import com.investrac.common.dto.ErrorCodes;
import com.investrac.common.events.PortfolioSyncedEvent;
import com.investrac.portfolio.dto.request.CreateHoldingRequest;
import com.investrac.portfolio.dto.request.UpdateHoldingRequest;
import com.investrac.portfolio.dto.response.*;
import com.investrac.portfolio.entity.Holding;
import com.investrac.portfolio.entity.Holding.HoldingType;
import com.investrac.portfolio.entity.PriceHistory;
import com.investrac.portfolio.exception.PortfolioException;
import com.investrac.portfolio.mapper.HoldingMapper;
import com.investrac.portfolio.outbox.PortfolioOutboxService;
import com.investrac.portfolio.repository.HoldingRepository;
import com.investrac.portfolio.repository.PriceHistoryRepository;
import com.investrac.portfolio.sync.PriceSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository      holdingRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceSyncService       priceSyncService;
    private final HoldingMapper          mapper;
    private final PortfolioOutboxService outboxService;

    // ══════════════════════════════════════════
    // CREATE
    // ══════════════════════════════════════════
    @Transactional
    public HoldingResponse createHolding(Long userId, CreateHoldingRequest req) {
        log.info("Creating holding for userId={} type={} name={}", userId, req.getType(), req.getName());

        BigDecimal currentVal = req.getCurrentValue() != null ? req.getCurrentValue() : req.getInvested();

        // Determine if this holding can be auto-price-updated
        boolean canUpdate = req.getSymbol() != null && !req.getSymbol().isBlank()
            && (req.getType() == HoldingType.EQUITY_MF
             || req.getType() == HoldingType.DEBT_MF
             || req.getType() == HoldingType.STOCKS
             || req.getType() == HoldingType.GOLD_SGB);

        Holding holding = Holding.builder()
            .userId(userId)
            .type(req.getType())
            .name(req.getName().trim())
            .symbol(req.getSymbol())
            .units(req.getUnits() != null ? req.getUnits() : BigDecimal.ZERO)
            .buyPrice(req.getBuyPrice() != null ? req.getBuyPrice() : BigDecimal.ZERO)
            .currentPrice(req.getBuyPrice() != null ? req.getBuyPrice() : BigDecimal.ZERO)
            .invested(req.getInvested())
            .currentValue(currentVal)
            .xirr(req.getXirr() != null ? req.getXirr() : BigDecimal.ZERO)
            .sipAmount(req.getSipAmount() != null ? req.getSipAmount() : BigDecimal.ZERO)
            .updatable(canUpdate)
            .note(req.getNote())
            .active(true)
            .build();

        holding = holdingRepository.save(holding);

        // Record initial price in history
        if (holding.getBuyPrice().compareTo(BigDecimal.ZERO) > 0) {
            recordPriceHistory(holding.getId(), holding.getBuyPrice(), LocalDate.now());
        }

        log.info("Holding created id={} for userId={}", holding.getId(), userId);
        return mapper.toResponse(holding);
    }

    // ══════════════════════════════════════════
    // GET PORTFOLIO SUMMARY
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getPortfolioSummary(Long userId) {
        List<Holding> holdings = holdingRepository
            .findByUserIdAndActiveTrueOrderByCurrentValueDesc(userId);

        if (holdings.isEmpty()) {
            return PortfolioSummaryResponse.builder()
                .totalInvested(BigDecimal.ZERO)
                .totalCurrentValue(BigDecimal.ZERO)
                .totalReturn(BigDecimal.ZERO)
                .totalReturnPercent(BigDecimal.ZERO)
                .xirr(BigDecimal.ZERO)
                .holdingCount(0)
                .holdingsByType(Map.of())
                .allocationPercent(Map.of())
                .holdings(List.of())
                .build();
        }

        BigDecimal totalInvested = holdingRepository.sumInvested(userId);
        BigDecimal totalValue    = holdingRepository.sumCurrentValue(userId);
        BigDecimal totalReturn   = totalValue.subtract(totalInvested);

        BigDecimal totalReturnPct = totalInvested.compareTo(BigDecimal.ZERO) > 0
            ? totalReturn.multiply(BigDecimal.valueOf(100))
                         .divide(totalInvested, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        // Asset allocation by type
        List<Object[]> typeData = holdingRepository.getValueByType(userId);
        Map<String, BigDecimal> byType = typeData.stream()
            .collect(Collectors.toMap(
                row -> ((HoldingType) row[0]).name(),
                row -> (BigDecimal) row[1]
            ));

        // Allocation percentages
        Map<String, BigDecimal> allocPct = new LinkedHashMap<>();
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            byType.forEach((type, value) -> allocPct.put(type,
                value.multiply(BigDecimal.valueOf(100))
                     .divide(totalValue, 1, RoundingMode.HALF_UP)));
        }

        // Portfolio XIRR — weighted average across holdings
        // (Stub: proper XIRR needs cash flow dates — left for full XIRR implementation)
        BigDecimal portfolioXirr = calculateWeightedXirr(holdings, totalValue);

        // Latest sync time
        Instant lastSync = holdings.stream()
            .map(Holding::getLastSynced)
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .orElse(null);

        return PortfolioSummaryResponse.builder()
            .totalInvested(totalInvested)
            .totalCurrentValue(totalValue)
            .totalReturn(totalReturn)
            .totalReturnPercent(totalReturnPct)
            .xirr(portfolioXirr)
            .holdingCount(holdings.size())
            .lastSyncedAt(lastSync)
            .holdingsByType(byType)
            .allocationPercent(allocPct)
            .holdings(holdings.stream().map(mapper::toResponse).collect(Collectors.toList()))
            .build();
    }

    // ══════════════════════════════════════════
    // GET BY ID
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public HoldingResponse getById(Long id, Long userId) {
        return mapper.toResponse(findByIdAndUser(id, userId));
    }

    // ══════════════════════════════════════════
    // UPDATE
    // ══════════════════════════════════════════
    @Transactional
    public HoldingResponse updateHolding(Long id, Long userId, UpdateHoldingRequest req) {
        Holding holding = findByIdAndUser(id, userId);

        if (req.getName()         != null) holding.setName(req.getName().trim());
        if (req.getSymbol()       != null) holding.setSymbol(req.getSymbol());
        if (req.getUnits()        != null) holding.setUnits(req.getUnits());
        if (req.getBuyPrice()     != null) holding.setBuyPrice(req.getBuyPrice());
        if (req.getInvested()     != null) holding.setInvested(req.getInvested());
        if (req.getCurrentValue() != null) holding.setCurrentValue(req.getCurrentValue());
        if (req.getXirr()         != null) holding.setXirr(req.getXirr());
        if (req.getSipAmount()    != null) holding.setSipAmount(req.getSipAmount());
        if (req.getNote()         != null) holding.setNote(req.getNote());

        holding = holdingRepository.save(holding);
        log.info("Holding updated id={} userId={}", id, userId);
        return mapper.toResponse(holding);
    }

    // ══════════════════════════════════════════
    // DELETE (soft)
    // ══════════════════════════════════════════
    @Transactional
    public void deleteHolding(Long id, Long userId) {
        int updated = holdingRepository.deactivate(id, userId);
        if (updated == 0) throw new PortfolioException(
            ErrorCodes.HOLDING_NOT_FOUND, "Holding not found", HttpStatus.NOT_FOUND);
        log.info("Holding deactivated id={} userId={}", id, userId);
    }

    // ══════════════════════════════════════════
    // MANUAL SYNC (on-demand by user)
    // ══════════════════════════════════════════
    @Transactional
    public SyncResultResponse syncPricesForUser(Long userId) {
        log.info("Manual price sync triggered for userId={}", userId);

        List<Holding> updatable = holdingRepository
            .findByUserIdAndActiveTrueOrderByCurrentValueDesc(userId)
            .stream()
            .filter(Holding::isUpdatable)
            .filter(h -> h.getSymbol() != null)
            .collect(Collectors.toList());

        int synced = 0, failed = 0;
        List<String> failedSymbols = new ArrayList<>();

        for (Holding h : updatable) {
            Optional<BigDecimal> newPrice = fetchPrice(h);
            if (newPrice.isPresent()) {
                updateHoldingPrice(h, newPrice.get());
                synced++;
            } else {
                failed++;
                failedSymbols.add(h.getSymbol() + " (" + h.getName() + ")");
            }
        }

        Instant now = Instant.now();
        log.info("Manual sync complete for userId={}: synced={} failed={}", userId, synced, failed);

        // Publish event if at least one holding was updated
        if (synced > 0) {
            BigDecimal totalVal = holdingRepository.sumCurrentValue(userId);
            outboxService.publish(PortfolioSyncedEvent.TOPIC,
                new PortfolioSyncedEvent(userId, synced, totalVal, BigDecimal.ZERO, BigDecimal.ZERO, now));
        }

        return SyncResultResponse.builder()
            .totalHoldings(updatable.size())
            .syncedCount(synced)
            .failedCount(failed)
            .failedSymbols(failedSymbols)
            .syncedAt(now)
            .build();
    }

    // ══════════════════════════════════════════
    // PRICE HISTORY
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<PriceHistoryResponse> getPriceHistory(Long id, Long userId, int days) {
        findByIdAndUser(id, userId); // ownership check
        LocalDate from = LocalDate.now().minusDays(days);

        return priceHistoryRepository
            .findByHoldingIdSince(id, from).stream()
            .map(ph -> PriceHistoryResponse.builder()
                .date(ph.getRecordedAt())
                .price(ph.getPrice())
                .build())
            .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════
    // PORTFOLIO VALUE HISTORY (all holdings)
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public PortfolioValueHistoryResponse getPortfolioHistory(Long userId, int days) {
        LocalDate from = LocalDate.now().minusDays(days);
        List<Object[]> raw = priceHistoryRepository.getPortfolioValueHistory(userId, from);

        List<PortfolioValueHistoryResponse.DataPoint> points = raw.stream()
            .map(row -> PortfolioValueHistoryResponse.DataPoint.builder()
                .date((LocalDate) row[0])
                .value((BigDecimal) row[1])
                .build())
            .collect(Collectors.toList());

        BigDecimal current = holdingRepository.sumCurrentValue(userId);
        BigDecimal start   = points.isEmpty() ? current : points.get(0).getValue();
        BigDecimal change  = current.subtract(start);
        BigDecimal changePct = start.compareTo(BigDecimal.ZERO) > 0
            ? change.multiply(BigDecimal.valueOf(100)).divide(start, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal peak = points.stream()
            .map(PortfolioValueHistoryResponse.DataPoint::getValue)
            .max(BigDecimal::compareTo)
            .orElse(current);

        return PortfolioValueHistoryResponse.builder()
            .dataPoints(points)
            .currentValue(current)
            .peakValue(peak)
            .changeFromStart(change)
            .changeFromStartPercent(changePct)
            .build();
    }

    // ══════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════

    public void updateHoldingPrice(Holding holding, BigDecimal newPrice) {
        holding.setCurrentPrice(newPrice);
        holding.refreshCurrentValue();
        holding.setLastSynced(Instant.now());
        holdingRepository.updatePriceAndValue(
            holding.getId(), newPrice, holding.getCurrentValue(), holding.getLastSynced());
        recordPriceHistory(holding.getId(), newPrice, LocalDate.now());
    }

    public Optional<BigDecimal> fetchPrice(Holding holding) {
        return switch (holding.getType()) {
            case EQUITY_MF, DEBT_MF -> priceSyncService.syncMutualFundNav(holding.getSymbol());
            case STOCKS              -> priceSyncService.syncStockPrice(holding.getSymbol());
            case GOLD_SGB            -> priceSyncService.syncStockPrice(holding.getSymbol());
            default                  -> Optional.empty();
        };
    }

    private void recordPriceHistory(Long holdingId, BigDecimal price, LocalDate date) {
        if (!priceHistoryRepository.existsByHoldingIdAndRecordedAt(holdingId, date)) {
            priceHistoryRepository.save(PriceHistory.builder()
                .holdingId(holdingId)
                .price(price)
                .recordedAt(date)
                .build());
        }
    }

    private Holding findByIdAndUser(Long id, Long userId) {
        return holdingRepository.findByIdAndUserId(id, userId)
            .filter(Holding::isActive)
            .orElseThrow(() -> new PortfolioException(
                ErrorCodes.HOLDING_NOT_FOUND, "Holding not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Portfolio XIRR — weighted average XIRR across holdings.
     *
     * NOTE: True XIRR requires exact cash flow dates and iterative
     * Newton-Raphson solver. This is a weighted approximation suitable
     * for display. Full XIRR implementation is a Session 5 enhancement.
     */
    private BigDecimal calculateWeightedXirr(List<Holding> holdings, BigDecimal totalValue) {
        if (totalValue.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        BigDecimal weightedSum = holdings.stream()
            .filter(h -> h.getXirr() != null && h.getXirr().compareTo(BigDecimal.ZERO) > 0
                      && h.getCurrentValue() != null)
            .reduce(BigDecimal.ZERO, (acc, h) ->
                acc.add(h.getXirr().multiply(h.getCurrentValue())), BigDecimal::add);

        return weightedSum.divide(totalValue, 2, RoundingMode.HALF_UP);
    }
}
