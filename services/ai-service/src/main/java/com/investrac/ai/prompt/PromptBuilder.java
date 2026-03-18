package com.investrac.ai.prompt;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds context-aware prompts for Claude API calls.
 *
 * Two prompt types:
 *   1. Chat — user asks a question, Claude answers with personal context
 *   2. Insight — nightly batch generation of 3-5 actionable insights
 *
 * Design principles:
 *   - System prompt always carries full financial context (income, portfolio, etc.)
 *   - Instructions are specific to India (tax slabs, MF categories, RBI rules)
 *   - Claude is told to be concise and actionable, not generic
 *   - Token efficiency: context is formatted compactly to save API cost
 *   - Never expose raw conversation history in logs
 */
@Component
public class PromptBuilder {

    private static final String TODAY = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

    // ════════════════════════════════════════
    // SYSTEM PROMPT (common to chat + insights)
    // ════════════════════════════════════════
    private String buildSystemPrompt(UserContext ctx) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are INVESTRAC AI — a certified Indian personal finance advisor embedded in the INVESTRAC app.\n");
        sb.append("Today's date: ").append(TODAY).append("\n\n");

        sb.append("## USER PROFILE\n");
        sb.append("Name: ").append(ctx.userName()).append("\n");
        sb.append("Tax regime: ").append(ctx.taxRegime()).append("\n");
        sb.append("Risk profile: ").append(ctx.riskProfile()).append("\n\n");

        sb.append("## WALLET (This Month)\n");
        sb.append("Monthly income:   ₹").append(fmt(ctx.monthlyIncome())).append("\n");
        sb.append("Wallet balance:   ₹").append(fmt(ctx.walletBalance())).append("\n");
        sb.append("Committed (EMI+SIP): ₹").append(fmt(ctx.walletCommitted())).append("\n");
        sb.append("Free to spend:    ₹").append(fmt(ctx.walletFreeToSpend())).append("\n");
        sb.append("Monthly expense:  ₹").append(fmt(ctx.monthlyExpense())).append("\n");
        sb.append("Savings rate:     ").append(ctx.savingsRate()).append("%\n\n");

        sb.append("## PORTFOLIO\n");
        sb.append("Current value:    ₹").append(fmt(ctx.portfolioValue())).append("\n");
        sb.append("Invested:         ₹").append(fmt(ctx.portfolioInvested())).append("\n");
        sb.append("Total return:     ₹").append(fmt(ctx.portfolioReturn()))
          .append(" (").append(ctx.portfolioReturnPct()).append("%)\n");
        sb.append("XIRR:             ").append(ctx.portfolioXirr()).append("%\n\n");

        if (!ctx.topCategories().isEmpty()) {
            sb.append("## TOP SPENDING CATEGORIES (This Month)\n");
            ctx.topCategories().entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> sb.append("  ").append(e.getKey())
                    .append(": ₹").append(fmt(e.getValue())).append("\n"));
            sb.append("\n");
        }

        if (!ctx.pendingEmis().isEmpty()) {
            sb.append("## UPCOMING EMIs\n");
            ctx.pendingEmis().forEach(e -> sb.append("  ")
                .append(e.accountName()).append(": ₹").append(fmt(e.amount()))
                .append(" on ").append(e.dueDay()).append("th\n"));
            sb.append("\n");
        }

        if (ctx.has80cOpportunity()) {
            sb.append("## TAX SAVING\n");
            sb.append("80C used: ₹").append(fmt(ctx.section80cUsed()))
              .append(" / ₹").append(fmt(ctx.section80cLimit())).append("\n");
            sb.append("80C remaining: ₹").append(fmt(ctx.get80cRemaining())).append("\n\n");
        }

        sb.append("## INSTRUCTIONS\n");
        sb.append("- Answer in clear, simple English (use Hinglish only if user writes in Hindi)\n");
        sb.append("- Be specific with numbers — always mention actual rupee amounts from the context above\n");
        sb.append("- Give 2-3 concrete actionable steps, not generic advice\n");
        sb.append("- India-specific: reference ELSS, NPS 80CCD-1B, NSE/BSE, SIP, MFAPI, etc.\n");
        sb.append("- Keep responses under 250 words for chat, 100 words per insight\n");
        sb.append("- Never recommend specific stocks or predict market movements\n");
        sb.append("- If you don't have enough data, say so and ask for more context\n");

        return sb.toString();
    }

    // ════════════════════════════════════════
    // CHAT PROMPT
    // ════════════════════════════════════════

    /**
     * Build the messages array for a chat API call.
     * Includes conversation history + new question.
     *
     * @return list of {role, content} maps ready for Claude API
     */
    public List<Map<String, String>> buildChatMessages(
            String question,
            List<Map<String, String>> history) {

        // History already formatted as Claude messages — just append new question
        var messages = new java.util.ArrayList<>(history);
        messages.add(Map.of("role", "user", "content", question));
        return messages;
    }

    /**
     * Build system prompt for chat endpoint.
     */
    public String buildChatSystemPrompt(UserContext ctx) {
        return buildSystemPrompt(ctx)
            + "\nAnswer the user's question using their actual financial data above.";
    }

    // ════════════════════════════════════════
    // INSIGHT GENERATION PROMPT
    // ════════════════════════════════════════

    /**
     * Build prompt for nightly batch insight generation.
     * Asks Claude to produce structured JSON with 3-5 insights.
     */
    public String buildInsightSystemPrompt() {
        return """
            You are INVESTRAC AI generating nightly financial insights.
            Respond ONLY with a valid JSON array — no preamble, no markdown, no explanation.
            Each insight must have: type, priority (1-5), content.
            
            Types: SPENDING | SAVINGS | TAX | PORTFOLIO | EMI
            Priority 1 = most urgent/impactful.
            
            Example format:
            [
              {"type":"TAX","priority":1,"content":"You have ₹26,200 of 80C limit unused. Investing in ELSS before March 31st saves ₹7,874 in tax at 30% slab."},
              {"type":"PORTFOLIO","priority":2,"content":"Your equity allocation is 78% which exceeds your moderate risk profile target of 60%. Consider adding ₹50,000 to debt funds to rebalance."}
            ]
            """;
    }

    public String buildInsightUserPrompt(UserContext ctx) {
        return buildSystemPrompt(ctx)
            + "\nGenerate 3-5 personalized financial insights for this user. "
            + "Focus on the most impactful actions they can take right now. "
            + "Respond ONLY with a JSON array as instructed.";
    }

    // ════════════════════════════════════════
    // SUGGESTED FOLLOW-UP QUESTIONS
    // ════════════════════════════════════════

    /**
     * Returns context-appropriate follow-up question suggestions.
     * Shown as clickable chips below Claude's response in the UI.
     */
    public List<String> buildSuggestedQuestions(UserContext ctx) {
        var questions = new java.util.ArrayList<String>();

        if (ctx.has80cOpportunity()) {
            questions.add("How do I save more tax before March 31st?");
        }
        if (ctx.portfolioXirr() != null && ctx.portfolioXirr().doubleValue() < 12.0) {
            questions.add("How can I improve my portfolio returns?");
        }
        if (ctx.savingsRate() != null && ctx.savingsRate().doubleValue() < 30.0) {
            questions.add("How do I increase my savings rate?");
        }
        if (!ctx.pendingEmis().isEmpty()) {
            questions.add("Which loan should I prepay first?");
        }

        // Always add some general ones
        questions.add("Should I increase my SIP amount?");
        questions.add("Am I on track to retire early?");

        return questions.stream().limit(4).collect(Collectors.toList());
    }

    // ════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════

    private String fmt(BigDecimal value) {
        if (value == null) return "0";
        long v = value.longValue();
        if (v >= 10_000_000) return String.format("%.1fCr", v / 10_000_000.0);
        if (v >= 1_000_000)  return String.format("%.1fL",  v / 100_000.0);
        if (v >= 1_000) {
            double kVal = v / 1_000.0;
            return kVal == Math.floor(kVal)
                ? String.format("%.0fK", kVal)
                : String.format("%.1fK", kVal);
        }
        return String.valueOf(v);
    }
}
