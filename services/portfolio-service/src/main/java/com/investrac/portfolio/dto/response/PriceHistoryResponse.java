package com.investrac.portfolio.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PriceHistoryResponse {
    private LocalDate  date;
    private BigDecimal price;
    private BigDecimal value;    // price × units at that date
}
