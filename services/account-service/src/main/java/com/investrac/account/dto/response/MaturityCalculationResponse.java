package com.investrac.account.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MaturityCalculationResponse {
    private BigDecimal principal;
    private BigDecimal interestEarned;
    private BigDecimal maturityAmount;
    private int        tenureMonths;
    private double     tenureYears;
    private String     calculationMethod;  // "SIMPLE_INTEREST" or "COMPOUND_INTEREST"
}
