package com.fintrac.account.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder
public class AccountProjectionResponse {
    private BigDecimal maturityAmount;
    private BigDecimal totalInterest;
    private BigDecimal monthlyEmi;
}
