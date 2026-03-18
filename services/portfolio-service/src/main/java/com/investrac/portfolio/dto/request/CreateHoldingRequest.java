package com.investrac.portfolio.dto.request;

import com.investrac.portfolio.entity.Holding.HoldingType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateHoldingRequest {

    @NotNull(message = "Holding type is required")
    private HoldingType type;

    @NotBlank(message = "Holding name is required")
    @Size(min = 2, max = 200, message = "Name must be 2-200 characters")
    private String name;

    /**
     * MF: AMFI scheme code  e.g. "119598"
     * Stocks: NSE symbol    e.g. "INFY"
     * SGB: series code      e.g. "SGBMAR29"
     * Leave null for NPS/PPF/manual holdings
     */
    @Size(max = 50)
    private String symbol;

    @DecimalMin(value = "0.0001", message = "Units must be greater than 0")
    private BigDecimal units;

    @DecimalMin(value = "0.00")
    private BigDecimal buyPrice;

    @NotNull(message = "Invested amount is required")
    @DecimalMin(value = "1.00",       message = "Invested amount must be at least ₹1")
    @DecimalMax(value = "999999999.99",message = "Invested amount too large")
    private BigDecimal invested;

    @DecimalMin(value = "0.00")
    private BigDecimal currentValue;   // If null, defaults to invested

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "999.99")
    private BigDecimal xirr = BigDecimal.ZERO;

    @DecimalMin(value = "0.00")
    private BigDecimal sipAmount = BigDecimal.ZERO;

    @Size(max = 500)
    private String note;
}
