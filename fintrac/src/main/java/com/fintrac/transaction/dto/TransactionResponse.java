package com.fintrac.transaction.dto;

import com.fintrac.transaction.entity.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data @Builder
public class TransactionResponse {
    private Long id;
    private Long userId;
    private TransactionType type;
    private TransactionCategory category;
    private String name;
    private BigDecimal amount;
    private LocalDate txDate;
    private String note;
    private TransactionStatus status;
    private Instant createdAt;
}
