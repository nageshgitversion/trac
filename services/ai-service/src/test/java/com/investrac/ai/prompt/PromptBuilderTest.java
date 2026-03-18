package com.investrac.ai.prompt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PromptBuilder Unit Tests")
class PromptBuilderTest {

    private PromptBuilder promptBuilder;
    private UserContext fullCtx;
    private UserContext emptyCtx;

    @BeforeEach
    void setUp() {
        promptBuilder = new PromptBuilder();

        fullCtx = new UserContext(
            "Arjun Kumar", "OLD", "AGGRESSIVE",
            new BigDecimal("115000"), new BigDecimal("51400"),
            new BigDecimal("63600"), new BigDecimal("51400"),
            new BigDecimal("1860000"), new BigDecimal("1508000"),
            new BigDecimal("352000"), new BigDecimal("23.34"),
            new BigDecimal("16.20"),
            new BigDecimal("38420"), new BigDecimal("42.00"),
            Map.of("Food & Dining",  new BigDecimal("8420"),
                   "Shopping",       new BigDecimal("12300"),
                   "Entertainment",  new BigDecimal("2800")),
            List.of(
                new UserContext.PendingEmi("HDFC Home Loan", new BigDecimal("35200"), 17, "loan"),
                new UserContext.PendingEmi("Car Loan",       new BigDecimal("8400"),  25, "loan")
            ),
            new BigDecimal("80000"), new BigDecimal("150000")  // 80C: 80K used / 1.5L limit
        );

        emptyCtx = UserContext.empty("New User");
    }

    // ═══════════════════════════════════════════
    // SYSTEM PROMPT CONTENT
    // ═══════════════════════════════════════════
    @Nested
    @DisplayName("buildChatSystemPrompt()")
    class SystemPromptTests {

        @Test
        @DisplayName("contains user name and tax regime")
        void prompt_ContainsUserNameAndTaxRegime() {
            String prompt = promptBuilder.buildChatSystemPrompt(fullCtx);
            assertThat(prompt).contains("Arjun Kumar");
            assertThat(prompt).contains("OLD");
        }

        @Test
        @DisplayName("contains wallet financial figures")
        void prompt_ContainsWalletData() {
            String prompt = promptBuilder.buildChatSystemPrompt(fullCtx);
            assertThat(prompt).contains("115");   // income ~₹115K
            assertThat(prompt).contains("51");    // free to spend ~₹51K
            assertThat(prompt).contains("42.00"); // savings rate
        }

        @Test
        @DisplayName("contains portfolio XIRR")
        void prompt_ContainsPortfolioXirr() {
            String prompt = promptBuilder.buildChatSystemPrompt(fullCtx);
            assertThat(prompt).contains("16.20");
        }

        @Test
        @DisplayName("contains spending categories section")
        void prompt_ContainsCategoryData() {
            String prompt = promptBuilder.buildChatSystemPrompt(fullCtx);
            assertThat(prompt).contains("Food & Dining");
            assertThat(prompt).contains("Shopping");
        }

        @Test
        @DisplayName("contains EMI section when pending EMIs exist")
        void prompt_ContainsEmiSection() {
            String prompt = promptBuilder.buildChatSystemPrompt(fullCtx);
            assertThat(prompt).contains("HDFC Home Loan");
            assertThat(prompt.contains("35200") || prompt.contains("35.2")).isTrue();
        }

        @Test
        @DisplayName("contains 80C section when opportunity exists")
        void prompt_Contains80cWhenOpportunity() {
            String prompt = promptBuilder.buildChatSystemPrompt(fullCtx);
            // 80K used of 1.5L = 70K remaining opportunity
            assertThat(prompt).contains("80C");
            assertThat(prompt.contains("80000") || prompt.contains("80K")).isTrue();
        }

        @Test
        @DisplayName("does NOT contain 80C section when limit is fully used")
        void prompt_No80cWhenFullyUsed() {
            UserContext fullyUsed = new UserContext(
                "Test", "NEW", "MODERATE",
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO,
                Map.of(), List.of(),
                new BigDecimal("150000"), new BigDecimal("150000") // 80C fully used
            );
            String prompt = promptBuilder.buildChatSystemPrompt(fullyUsed);
            assertThat(prompt).doesNotContain("TAX SAVING");
        }

        @Test
        @DisplayName("contains India-specific instructions")
        void prompt_ContainsIndiaSpecificGuidance() {
            String prompt = promptBuilder.buildChatSystemPrompt(fullCtx);
            assertThat(prompt).contains("ELSS");
            assertThat(prompt).contains("NPS");
        }

        @Test
        @DisplayName("empty context produces valid prompt without errors")
        void prompt_EmptyContext_NoException() {
            assertThatCode(() -> promptBuilder.buildChatSystemPrompt(emptyCtx))
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════
    // INSIGHT PROMPT
    // ═══════════════════════════════════════════
    @Nested
    @DisplayName("buildInsightSystemPrompt()")
    class InsightPromptTests {

        @Test
        @DisplayName("insight system prompt requires JSON-only response")
        void insightSystem_RequiresJsonOnly() {
            String prompt = promptBuilder.buildInsightSystemPrompt();
            assertThat(prompt).containsIgnoringCase("JSON");
            assertThat(prompt).contains("type");
            assertThat(prompt).contains("priority");
            assertThat(prompt).contains("content");
        }

        @Test
        @DisplayName("insight system prompt lists all valid types")
        void insightSystem_ContainsAllTypes() {
            String prompt = promptBuilder.buildInsightSystemPrompt();
            assertThat(prompt).contains("SPENDING");
            assertThat(prompt).contains("SAVINGS");
            assertThat(prompt).contains("TAX");
            assertThat(prompt).contains("PORTFOLIO");
            assertThat(prompt).contains("EMI");
        }

        @Test
        @DisplayName("insight user prompt combines context + generation instruction")
        void insightUser_ContainsContextAndInstruction() {
            String prompt = promptBuilder.buildInsightUserPrompt(fullCtx);
            assertThat(prompt).contains("Arjun Kumar");  // user name from context
            assertThat(prompt).containsIgnoringCase("insight");
        }
    }

    // ═══════════════════════════════════════════
    // SUGGESTED QUESTIONS
    // ═══════════════════════════════════════════
    @Nested
    @DisplayName("buildSuggestedQuestions()")
    class SuggestedQuestionsTests {

        @Test
        @DisplayName("includes 80C question when opportunity exists")
        void suggestions_Includes80cQuestion() {
            List<String> suggestions = promptBuilder.buildSuggestedQuestions(fullCtx);
            assertThat(suggestions.stream()
                .anyMatch(q -> q.toLowerCase().contains("tax")))
                .isTrue();
        }

        @Test
        @DisplayName("includes loan prepay question when EMIs exist")
        void suggestions_IncludesLoanQuestion() {
            List<String> suggestions = promptBuilder.buildSuggestedQuestions(fullCtx);
            assertThat(suggestions.stream()
                .anyMatch(q -> q.toLowerCase().contains("loan")
                            || q.toLowerCase().contains("prepay")))
                .isTrue();
        }

        @Test
        @DisplayName("returns at most 4 suggestions")
        void suggestions_MaxFour() {
            List<String> suggestions = promptBuilder.buildSuggestedQuestions(fullCtx);
            assertThat(suggestions.size()).isLessThanOrEqualTo(4);
        }

        @Test
        @DisplayName("returns suggestions even for empty context")
        void suggestions_EmptyContext_StillReturns() {
            List<String> suggestions = promptBuilder.buildSuggestedQuestions(emptyCtx);
            assertThat(suggestions).isNotEmpty();
        }
    }

    // ═══════════════════════════════════════════
    // USER CONTEXT RECORD
    // ═══════════════════════════════════════════
    @Nested
    @DisplayName("UserContext record")
    class UserContextTests {

        @Test
        @DisplayName("has80cOpportunity returns true when limit not fully used")
        void has80c_True_WhenNotFull() {
            assertThat(fullCtx.has80cOpportunity()).isTrue();
        }

        @Test
        @DisplayName("get80cRemaining returns correct amount")
        void get80c_ReturnsCorrectRemaining() {
            // 1,50,000 - 80,000 = 70,000
            assertThat(fullCtx.get80cRemaining()).isEqualByComparingTo("70000");
        }

        @Test
        @DisplayName("UserContext.empty() returns valid object with zero financials")
        void emptyContext_ValidZeroState() {
            UserContext ctx = UserContext.empty("Test User");
            assertThat(ctx.monthlyIncome()).isEqualByComparingTo("0");
            assertThat(ctx.portfolioXirr()).isEqualByComparingTo("0");
            assertThat(ctx.pendingEmis()).isEmpty();
            assertThat(ctx.topCategories()).isEmpty();
            assertThat(ctx.has80cOpportunity()).isTrue(); // 0 used of 1.5L
        }
    }
}
