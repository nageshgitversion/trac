package com.investrac.wallet.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class CreateWalletRequest {

    @NotBlank(message = "Month is required (format: YYYY-MM)")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Month must be in YYYY-MM format")
    private String month;

    @NotNull(message = "Income is required")
    @DecimalMin(value = "1.00", message = "Income must be at least ₹1")
    @DecimalMax(value = "99999999.99", message = "Income cannot exceed ₹9.99 Crore")
    private BigDecimal income;

    @DecimalMin(value = "0.00")
    private BigDecimal topup = BigDecimal.ZERO;

    // Envelope budgets: key=envelopeKey, value=budget amount
    // Example: {"food": 8000, "groceries": 6000, "transport": 3000}
    private Map<String, BigDecimal> envelopes;
}
