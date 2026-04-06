package com.fintrac.transaction.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder
public class TransactionSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netSavings;
    private int year;
    private int month;
}
