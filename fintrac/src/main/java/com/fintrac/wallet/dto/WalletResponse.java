package com.fintrac.wallet.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder
public class WalletResponse {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private String currency;
    private Instant updatedAt;
}
