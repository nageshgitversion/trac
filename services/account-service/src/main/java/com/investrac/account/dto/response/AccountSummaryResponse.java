package com.investrac.account.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AccountSummaryResponse {
    private BigDecimal totalSavings;
    private BigDecimal totalFdCorpus;
    private BigDecimal totalRdCorpus;
    private BigDecimal totalLoanOutstanding;
    private BigDecimal totalMonthlyEmi;    // committed from wallet
    private int        totalAccounts;
    private List<VirtualAccountResponse> accounts;
}
