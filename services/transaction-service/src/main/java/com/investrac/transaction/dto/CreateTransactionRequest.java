package com.investrac.transaction.dto;

import com.investrac.transaction.entity.Transaction;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateTransactionRequest {

    @NotNull(message = "Transaction type is required")
    private Transaction.TransactionType type;

    @NotBlank(message = "Category is required")
    @Size(max = 50)
    private String category;

    @NotBlank(message = "Description is required")
    @Size(min = 1, max = 200, message = "Description must be between 1 and 200 characters")
    private String name;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "9999999.99", message = "Amount cannot exceed ₹99.99 Lakh per transaction")
    @Digits(integer = 7, fraction = 2)
    private BigDecimal amount;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate txDate;

    @Size(max = 500)
    private String note;

    // Optional — if null, transaction is not linked to wallet
    private Long walletId;

    // Optional — if null, no envelope is updated
    @Size(max = 30)
    private String envelopeKey;

    // Optional — if null, transaction is not linked to a virtual account
    private Long accountId;

    private Transaction.TransactionSource source = Transaction.TransactionSource.MANUAL;
}
