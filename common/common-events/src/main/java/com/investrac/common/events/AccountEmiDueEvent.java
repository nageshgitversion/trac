package com.investrac.common.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Published by: account-service scheduler (3 days before EMI due)
 * Consumed by:  notification-service, wallet-service
 */
public record AccountEmiDueEvent(
    Long accountId,
    Long userId,
    String accountName,
    String accountType,         // "loan" | "rd"
    BigDecimal emiAmount,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dueDate,
    int daysUntilDue,
    BigDecimal walletBalance    // Current wallet balance for context
) {
    public static final String TOPIC = "investrac.account.emi-due";
}
