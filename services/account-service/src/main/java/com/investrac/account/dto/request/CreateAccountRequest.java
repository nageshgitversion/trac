package com.investrac.account.dto.request;

import com.investrac.account.config.validation.MaturityAfterStart;
import com.investrac.account.entity.VirtualAccount.AccountType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@MaturityAfterStart   // class-level: validates maturityDate > startDate
public class CreateAccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType type;

    @NotBlank(message = "Account name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String name;

    @NotNull(message = "Balance / principal amount is required")
    @DecimalMin(value = "1.00",       message = "Amount must be at least ₹1")
    @DecimalMax(value = "99999999.99",message = "Amount cannot exceed ₹9.99 Crore")
    private BigDecimal balance;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "99.99", message = "Interest rate cannot exceed 99.99%")
    private BigDecimal interestRate = BigDecimal.ZERO;

    @Size(max = 100, message = "Bank name too long")
    private String bankName;

    private LocalDate startDate;

    private LocalDate maturityDate;    // Required for FD and RD

    @DecimalMin(value = "0.00")
    private BigDecimal emiAmount = BigDecimal.ZERO;    // For LOAN / RD

    @Min(value = 1,  message = "EMI day must be between 1 and 31")
    @Max(value = 31, message = "EMI day must be between 1 and 31")
    private Integer emiDay;

    private Long linkedAccId;          // Savings account to receive maturity proceeds

    @DecimalMin(value = "0.00")
    private BigDecimal goalAmount;     // For SAVINGS goal tracking
}
