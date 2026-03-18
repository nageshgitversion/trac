package com.investrac.portfolio.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class HoldingResponse {
    private Long       id;
    private Long       userId;
    private String     type;
    private String     name;
    private String     symbol;
    private BigDecimal units;
    private BigDecimal buyPrice;
    private BigDecimal currentPrice;
    private BigDecimal invested;
    private BigDecimal currentValue;
    private BigDecimal returnAmount;    // currentValue - invested
    private BigDecimal returnPercent;   // (return / invested) × 100
    private boolean    isProfit;
    private BigDecimal xirr;
    private BigDecimal sipAmount;
    private boolean    updatable;
    private Instant    lastSynced;
    private String     note;
    private Instant    createdAt;
}
