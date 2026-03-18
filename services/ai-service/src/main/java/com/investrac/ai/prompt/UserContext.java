package com.investrac.ai.prompt;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * A snapshot of the user's financial state assembled before each Claude API call.
 *
 * Built by AiService from data fetched via internal REST calls to:
 *   - wallet-service    → income, balance, committed
 *   - portfolio-service → current value, XIRR
 *   - transaction-service → monthly expense, category breakdown
 *   - account-service   → pending EMIs
 *
 * This record is passed to PromptBuilder to construct the system prompt.
 *
 * SECURITY: This object must never be logged — it contains sensitive financial data.
 */
public record UserContext(

    // Identity
    String userName,
    String taxRegime,      // "OLD" | "NEW"
    String riskProfile,    // "CONSERVATIVE" | "MODERATE" | "AGGRESSIVE"

    // Wallet
    BigDecimal monthlyIncome,
    BigDecimal walletBalance,
    BigDecimal walletCommitted,   // total EMIs + SIPs scheduled
    BigDecimal walletFreeToSpend,

    // Portfolio
    BigDecimal portfolioValue,
    BigDecimal portfolioInvested,
    BigDecimal portfolioReturn,
    BigDecimal portfolioReturnPct,
    BigDecimal portfolioXirr,

    // Monthly spending (current month)
    BigDecimal monthlyExpense,
    BigDecimal savingsRate,        // (income - expense) / income × 100

    // Category breakdown: {"Food & Dining": 8420, "Shopping": 12300}
    Map<String, BigDecimal> topCategories,

    // Upcoming EMIs
    List<PendingEmi> pendingEmis,

    // 80C status
    BigDecimal section80cUsed,     // amount already invested in ELSS/PPF etc.
    BigDecimal section80cLimit     // ₹1,50,000

) {
    public record PendingEmi(
        String      accountName,
        BigDecimal  amount,
        int         dueDay,
        String      type   // "loan" | "rd"
    ) {}

    /** Returns true if 80C still has room */
    public boolean has80cOpportunity() {
        if (section80cUsed == null || section80cLimit == null) return false;
        return section80cUsed.compareTo(section80cLimit) < 0;
    }

    public BigDecimal get80cRemaining() {
        if (!has80cOpportunity()) return BigDecimal.ZERO;
        return section80cLimit.subtract(section80cUsed);
    }

    /** Returns empty context for users with no data yet */
    public static UserContext empty(String name) {
        return new UserContext(
            name, "NEW", "MODERATE",
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO,
            Map.of(), List.of(),
            BigDecimal.ZERO, new BigDecimal("150000")
        );
    }
}
