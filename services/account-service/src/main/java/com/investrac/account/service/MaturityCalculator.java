package com.investrac.account.service;

import com.investrac.account.dto.response.MaturityCalculationResponse;
import com.investrac.account.entity.VirtualAccount.AccountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Financial maturity calculations for Fixed Deposits and Recurring Deposits.
 *
 * FD Formula:
 *   Maturity = Principal × (1 + Rate/100 × Years)    [Simple Interest — most banks display this]
 *   Interest = Maturity - Principal
 *
 * RD Formula (monthly compounding):
 *   M = R × [(1+i)^n - 1] / (1 - (1+i)^(-1/3))
 *   Where:
 *     R = monthly instalment
 *     i = quarterly interest rate = Rate / (4 × 100)
 *     n = number of quarters
 *
 *   Simplified approximation used by most banks:
 *   M = R × n × (1 + i×(n+1)/2)
 *   Where i = annual_rate / 1200 (monthly rate)
 */
@Component
public class MaturityCalculator {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    /**
     * Calculate FD maturity using simple interest.
     * Banks typically show simple interest maturity on FD certificates.
     *
     * @param principal  Amount deposited
     * @param annualRate Rate percent per annum (e.g. 7.2 for 7.2%)
     * @param startDate  FD start date
     * @param endDate    FD maturity date
     */
    public MaturityCalculationResponse calculateFd(
            BigDecimal principal,
            BigDecimal annualRate,
            LocalDate startDate,
            LocalDate endDate) {

        long days   = ChronoUnit.DAYS.between(startDate, endDate);
        long months = Math.round(days / 30.44);
        double years = months / 12.0;

        // Simple Interest: I = P × R × T
        BigDecimal interest = principal
            .multiply(annualRate)
            .multiply(BigDecimal.valueOf(years))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal maturity = principal.add(interest);

        return MaturityCalculationResponse.builder()
            .principal(principal)
            .interestEarned(interest.setScale(2, RoundingMode.HALF_UP))
            .maturityAmount(maturity.setScale(2, RoundingMode.HALF_UP))
            .tenureMonths((int) months)
            .tenureYears(Math.round(years * 10.0) / 10.0)
            .calculationMethod("SIMPLE_INTEREST")
            .build();
    }

    /**
     * Calculate RD maturity using monthly compounding approximation.
     * Formula: M = R × [(1+r)^n − 1] × (1+r) / r
     * Where r = monthly rate = annualRate / 1200
     *
     * @param monthlyInstalment  Monthly RD amount
     * @param annualRate         Rate percent per annum
     * @param startDate          RD start date
     * @param endDate            RD maturity date
     */
    public MaturityCalculationResponse calculateRd(
            BigDecimal monthlyInstalment,
            BigDecimal annualRate,
            LocalDate startDate,
            LocalDate endDate) {

        long days   = ChronoUnit.DAYS.between(startDate, endDate);
        int  months = (int) Math.round(days / 30.44);

        // Monthly interest rate
        double r = annualRate.doubleValue() / 1200.0;
        double n = months;

        // Maturity formula with compound monthly interest
        double maturityDouble = monthlyInstalment.doubleValue()
            * (Math.pow(1 + r, n) - 1)
            * (1 + r)
            / r;

        BigDecimal maturity   = BigDecimal.valueOf(maturityDouble).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPaid  = monthlyInstalment.multiply(BigDecimal.valueOf(months));
        BigDecimal interest   = maturity.subtract(totalPaid).max(BigDecimal.ZERO);
        double years          = Math.round((months / 12.0) * 10.0) / 10.0;

        return MaturityCalculationResponse.builder()
            .principal(totalPaid.setScale(2, RoundingMode.HALF_UP))
            .interestEarned(interest)
            .maturityAmount(maturity)
            .tenureMonths(months)
            .tenureYears(years)
            .calculationMethod("COMPOUND_INTEREST_MONTHLY")
            .build();
    }

    /**
     * Convenience method — dispatches to FD or RD calculator based on type.
     */
    public MaturityCalculationResponse calculate(
            AccountType type,
            BigDecimal principalOrInstalment,
            BigDecimal annualRate,
            LocalDate startDate,
            LocalDate endDate) {

        return switch (type) {
            case FD -> calculateFd(principalOrInstalment, annualRate, startDate, endDate);
            case RD -> calculateRd(principalOrInstalment, annualRate, startDate, endDate);
            default  -> MaturityCalculationResponse.builder()
                .principal(principalOrInstalment)
                .interestEarned(BigDecimal.ZERO)
                .maturityAmount(principalOrInstalment)
                .tenureMonths(0).tenureYears(0)
                .calculationMethod("N/A")
                .build();
        };
    }
}
