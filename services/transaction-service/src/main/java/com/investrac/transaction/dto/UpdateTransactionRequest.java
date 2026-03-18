package com.investrac.transaction.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateTransactionRequest {

    @NotBlank @Size(max = 50)
    private String category;

    @NotBlank @Size(max = 200)
    private String name;

    @NotNull
    @DecimalMin("0.01") @DecimalMax("9999999.99")
    private BigDecimal amount;

    @NotNull @PastOrPresent
    private LocalDate txDate;

    @Size(max = 500)
    private String note;

    @Size(max = 30)
    private String envelopeKey;
}
