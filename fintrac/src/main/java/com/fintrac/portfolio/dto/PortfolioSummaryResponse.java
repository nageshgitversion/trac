package com.fintrac.portfolio.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data @Builder
public class PortfolioSummaryResponse {
    private BigDecimal totalInvested;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalReturnPct;
    private Map<String, BigDecimal> byType;
}
