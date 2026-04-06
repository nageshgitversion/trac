package com.fintrac.account.dto;

import com.fintrac.account.entity.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data @Builder
public class AccountResponse {
    private Long id;
    private Long userId;
    private AccountType type;
    private String name;
    private BigDecimal principal;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private LocalDate startDate;
    private LocalDate maturityDate;
    private AccountStatus status;
    private String note;
    private Instant createdAt;
}
