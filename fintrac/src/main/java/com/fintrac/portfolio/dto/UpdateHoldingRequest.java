package com.fintrac.portfolio.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateHoldingRequest {
    private BigDecimal units;
    private BigDecimal currentPrice;
    private String note;
}
