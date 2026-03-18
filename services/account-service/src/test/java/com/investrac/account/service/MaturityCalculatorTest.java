package com.investrac.account.service;

import com.investrac.account.dto.response.MaturityCalculationResponse;
import com.investrac.account.entity.VirtualAccount.AccountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MaturityCalculator Unit Tests")
class MaturityCalculatorTest {

    private final MaturityCalculator calculator = new MaturityCalculator();

    // ═══════════════════════════════════════════
    // FD CALCULATIONS
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("FD: ₹5L at 7.2% for 2 years = ₹5.72L maturity")
    void fd_5Lakh_7Point2Percent_2Years() {
        MaturityCalculationResponse result = calculator.calculateFd(
            new BigDecimal("500000"),
            new BigDecimal("7.2"),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2027, 1, 1)
        );

        // Simple interest: 500000 × 7.2/100 × 2 = 72000
        assertThat(result.getInterestEarned()).isEqualByComparingTo("72000.00");
        assertThat(result.getMaturityAmount()).isEqualByComparingTo("572000.00");
        assertThat(result.getTenureMonths()).isEqualTo(24);
        assertThat(result.getTenureYears()).isEqualTo(2.0);
        assertThat(result.getCalculationMethod()).isEqualTo("SIMPLE_INTEREST");
    }

    @Test
    @DisplayName("FD: ₹3.5L at 7.4% for 6 months = correct interest")
    void fd_3Point5Lakh_7Point4Percent_6Months() {
        MaturityCalculationResponse result = calculator.calculateFd(
            new BigDecimal("350000"),
            new BigDecimal("7.4"),
            LocalDate.of(2025, 12, 22),
            LocalDate.of(2026, 6, 22)
        );

        // Simple interest: 350000 × 7.4/100 × 0.5 = 12950
        assertThat(result.getInterestEarned()).isGreaterThan(BigDecimal.valueOf(12000));
        assertThat(result.getMaturityAmount()).isGreaterThan(new BigDecimal("350000"));
        assertThat(result.getTenureMonths()).isEqualTo(6);
        assertThat(result.getPrincipal()).isEqualByComparingTo("350000.00");
    }

    @Test
    @DisplayName("FD: zero interest rate returns principal as maturity")
    void fd_ZeroRate_ReturnsPrincipal() {
        MaturityCalculationResponse result = calculator.calculateFd(
            new BigDecimal("100000"),
            BigDecimal.ZERO,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2027, 1, 1)
        );

        assertThat(result.getInterestEarned()).isEqualByComparingTo("0.00");
        assertThat(result.getMaturityAmount()).isEqualByComparingTo("100000.00");
    }

    // ═══════════════════════════════════════════
    // RD CALCULATIONS
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("RD: ₹10K/month at 6.8% for 3 years — maturity greater than total deposits")
    void rd_10K_6Point8Percent_3Years_Reasonable() {
        MaturityCalculationResponse result = calculator.calculateRd(
            new BigDecimal("10000"),
            new BigDecimal("6.8"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2027, 3, 1)
        );

        BigDecimal totalDeposits = new BigDecimal("10000").multiply(
            BigDecimal.valueOf(result.getTenureMonths()));

        // Maturity must always exceed total deposits
        assertThat(result.getMaturityAmount()).isGreaterThan(totalDeposits);
        // Interest must be positive
        assertThat(result.getInterestEarned()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getCalculationMethod()).isEqualTo("COMPOUND_INTEREST_MONTHLY");
        assertThat(result.getPrincipal()).isEqualByComparingTo(totalDeposits.toPlainString());
    }

    @Test
    @DisplayName("RD: higher interest rate produces higher maturity")
    void rd_HigherRate_HigherMaturity() {
        BigDecimal instalment  = new BigDecimal("5000");
        LocalDate start        = LocalDate.of(2026, 1, 1);
        LocalDate end          = LocalDate.of(2028, 1, 1);

        MaturityCalculationResponse low  = calculator.calculateRd(instalment, new BigDecimal("5.0"), start, end);
        MaturityCalculationResponse high = calculator.calculateRd(instalment, new BigDecimal("8.0"), start, end);

        assertThat(high.getMaturityAmount()).isGreaterThan(low.getMaturityAmount());
        assertThat(high.getInterestEarned()).isGreaterThan(low.getInterestEarned());
    }

    // ═══════════════════════════════════════════
    // DISPATCH METHOD
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("calculate() dispatches to FD path for AccountType.FD")
    void dispatch_Fd_UsesSimpleInterest() {
        MaturityCalculationResponse result = calculator.calculate(
            AccountType.FD,
            new BigDecimal("500000"),
            new BigDecimal("7.2"),
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2028, 1, 1)
        );
        assertThat(result.getCalculationMethod()).isEqualTo("SIMPLE_INTEREST");
    }

    @Test
    @DisplayName("calculate() dispatches to RD path for AccountType.RD")
    void dispatch_Rd_UsesCompoundInterest() {
        MaturityCalculationResponse result = calculator.calculate(
            AccountType.RD,
            new BigDecimal("10000"),
            new BigDecimal("6.8"),
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2028, 1, 1)
        );
        assertThat(result.getCalculationMethod()).isEqualTo("COMPOUND_INTEREST_MONTHLY");
    }

    @Test
    @DisplayName("calculate() returns N/A for unsupported types (SAVINGS, LOAN)")
    void dispatch_Savings_ReturnsNA() {
        MaturityCalculationResponse result = calculator.calculate(
            AccountType.SAVINGS,
            new BigDecimal("100000"),
            BigDecimal.ZERO,
            LocalDate.now(),
            LocalDate.now().plusYears(1)
        );
        assertThat(result.getCalculationMethod()).isEqualTo("N/A");
        assertThat(result.getInterestEarned()).isEqualByComparingTo("0.00");
    }
}
