package com.fintrac.portfolio.dto;

import com.fintrac.portfolio.entity.HoldingType;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder
public class HoldingResponse {
    private Long id;
    private Long userId;
    private HoldingType type;
    private String name;
    private String symbol;
    private BigDecimal units;
    private BigDecimal buyPrice;
    private BigDecimal currentPrice;
    private BigDecimal invested;
    private BigDecimal currentValue;
    private BigDecimal returnPct;
    private String note;
    private Instant createdAt;
}
