package com.investrac.account.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class VirtualAccountResponse {
    private Long   id;
    private Long   userId;
    private String type;
    private String name;
    private BigDecimal balance;
    private BigDecimal interestRate;
    private String bankName;
    private LocalDate startDate;
    private LocalDate maturityDate;
    private BigDecimal maturityAmount;    // Calculated or manually entered
    private BigDecimal emiAmount;
    private Integer    emiDay;
    private Long       linkedAccId;
    private BigDecimal goalAmount;
    private Integer    goalProgressPercent;
    private boolean    active;
    private boolean    emiDueToday;
    private boolean    maturingSoon;      // within next 7 days
    private Instant    createdAt;
}
