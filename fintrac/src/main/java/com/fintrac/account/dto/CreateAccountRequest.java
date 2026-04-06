package com.fintrac.account.dto;

import com.fintrac.account.entity.AccountType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateAccountRequest {
    @NotNull
    private AccountType type;

    @NotBlank @Size(max = 200)
    private String name;

    @NotNull @DecimalMin("0.01")
    private BigDecimal principal;

    private BigDecimal interestRate;

    private Integer tenureMonths;

    private LocalDate startDate;

    @Size(max = 500)
    private String note;
}
