package com.investrac.portfolio.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PortfolioValueHistoryResponse {
    private List<DataPoint> dataPoints;
    private BigDecimal peakValue;
    private BigDecimal currentValue;
    private BigDecimal changeFromStart;
    private BigDecimal changeFromStartPercent;

    @Data
    @Builder
    public static class DataPoint {
        private LocalDate  date;
        private BigDecimal value;
    }
}
