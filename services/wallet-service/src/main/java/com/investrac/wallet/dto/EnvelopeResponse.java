package com.investrac.wallet.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EnvelopeResponse {
    private Long id;
    private String envelopeKey;
    private String categoryName;
    private String icon;
    private BigDecimal budget;
    private BigDecimal spent;
    private BigDecimal remaining;
    private boolean overBudget;
    private int usedPercent;
}
