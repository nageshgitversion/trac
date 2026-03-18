package com.investrac.transaction.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class MonthSummaryResponse {

    private int year;
    private int month;
    private String monthLabel;          // "March 2026"

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal totalInvestment;
    private BigDecimal totalSavings;

    private BigDecimal netSavings;      // income - expense
    private int savingsRatePercent;     // (netSavings / income) * 100

    private List<CategoryBreakdown> expenseBreakdown;

    @Data @Builder
    public static class CategoryBreakdown {
        private String category;
        private BigDecimal amount;
        private int percentOfTotal;
    }
}
