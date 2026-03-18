package com.investrac.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopUpRequest {

    @NotNull
    @DecimalMin(value = "1.00", message = "Top-up amount must be at least ₹1")
    private BigDecimal amount;

    private String source;    // "freelance" | "bonus" | "other"
    private String note;
}
