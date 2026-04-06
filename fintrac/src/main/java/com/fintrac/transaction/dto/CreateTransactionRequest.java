package com.fintrac.transaction.dto;

import com.fintrac.transaction.entity.TransactionCategory;
import com.fintrac.transaction.entity.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateTransactionRequest {
    @NotNull(message = "Type is required")
    private TransactionType type;

    @NotNull(message = "Category is required")
    private TransactionCategory category;

    @NotBlank(message = "Name is required")
    @Size(max = 200)
    private String name;

    @NotNull(message = "Amount is required")
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull(message = "Transaction date is required")
    private LocalDate txDate;

    @Size(max = 500)
    private String note;
}
