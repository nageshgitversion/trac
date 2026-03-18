package com.investrac.portfolio.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateHoldingRequest {

    @Size(min = 2, max = 200)
    private String name;

    @Size(max = 50)
    private String symbol;

    @DecimalMin("0.0001")
    private BigDecimal units;

    @DecimalMin("0.00")
    private BigDecimal buyPrice;

    @DecimalMin("1.00")
    private BigDecimal invested;

    @DecimalMin("0.00")
    private BigDecimal currentValue;

    @DecimalMin("0.00")
    @DecimalMax("999.99")
    private BigDecimal xirr;

    @DecimalMin("0.00")
    private BigDecimal sipAmount;

    @Size(max = 500)
    private String note;
}
