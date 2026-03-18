package com.investrac.account.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateAccountRequest {

    @Size(min = 2, max = 100)
    private String name;

    @DecimalMin(value = "0.00")
    private BigDecimal balance;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "99.99")
    private BigDecimal interestRate;

    @Size(max = 100)
    private String bankName;

    private LocalDate maturityDate;

    @DecimalMin(value = "0.00")
    private BigDecimal emiAmount;

    @Min(1) @Max(31)
    private Integer emiDay;

    private Long linkedAccId;

    @DecimalMin(value = "0.00")
    private BigDecimal goalAmount;
}
