package com.investrac.portfolio.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PortfolioSummaryResponse {

    private BigDecimal totalInvested;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalReturn;            // totalCurrentValue - totalInvested
    private BigDecimal totalReturnPercent;     // (totalReturn / totalInvested) × 100
    private BigDecimal xirr;                   // portfolio-level XIRR

    private int holdingCount;
    private Instant lastSyncedAt;

    // Asset allocation by type: {"EQUITY_MF": 612000, "STOCKS": 410000, ...}
    private Map<String, BigDecimal> holdingsByType;

    // Asset allocation as percentage: {"EQUITY_MF": 42.3, ...}
    private Map<String, BigDecimal> allocationPercent;

    private List<HoldingResponse> holdings;
}
