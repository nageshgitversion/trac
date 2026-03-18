package com.investrac.wallet.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class WalletResponse {
    private Long id;
    private Long userId;
    private String month;
    private BigDecimal income;
    private BigDecimal topup;
    private BigDecimal balance;
    private BigDecimal committed;
    private BigDecimal freeToSpend;
    private int usedPercent;
    private boolean active;
    private List<EnvelopeResponse> envelopes;
    private Instant createdAt;
}
