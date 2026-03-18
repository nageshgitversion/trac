package com.investrac.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investrac.ai.client.ClaudeApiClient;
import com.investrac.ai.client.ClaudeApiClient.ClaudeApiException;
import com.investrac.ai.dto.request.ChatRequest;
import com.investrac.ai.dto.response.ChatResponse;
import com.investrac.ai.dto.response.InsightResponse;
import com.investrac.ai.dto.response.InsightSummaryResponse;
import com.investrac.ai.entity.AiChatHistory;
import com.investrac.ai.entity.AiInsight;
import com.investrac.ai.entity.AiInsight.InsightType;
import com.investrac.ai.prompt.PromptBuilder;
import com.investrac.ai.prompt.UserContext;
import com.investrac.ai.repository.AiChatHistoryRepository;
import com.investrac.ai.repository.AiInsightRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiService Unit Tests")
class AiServiceTest {

    @Mock AiInsightRepository    insightRepository;
    @Mock AiChatHistoryRepository chatRepository;
    @Mock ClaudeApiClient        claudeClient;
    @Mock PromptBuilder          promptBuilder;
    @Mock UserContextService     userContextService;
    @Spy  ObjectMapper           objectMapper = new ObjectMapper();

    @InjectMocks AiService aiService;

    private static final Long USER_ID = 100L;
    private UserContext mockCtx;

    @BeforeEach
    void setUp() {
        mockCtx = new UserContext(
            "Arjun Kumar", "NEW", "MODERATE",
            new BigDecimal("115000"), new BigDecimal("51400"),
            new BigDecimal("63600"), new BigDecimal("51400"),
            new BigDecimal("1860000"), new BigDecimal("1508000"),
            new BigDecimal("352000"), new BigDecimal("23.34"),
            new BigDecimal("16.20"),
            new BigDecimal("38420"), new BigDecimal("42.00"),
            Map.of("Food & Dining", new BigDecimal("8420"),
                   "Shopping",     new BigDecimal("12300")),
            List.of(new UserContext.PendingEmi("HDFC Home Loan",
                new BigDecimal("35200"), 17, "loan")),
            new BigDecimal("123800"), new BigDecimal("150000")
        );
    }

    // ═══════════════════════════════════════════
    // CHAT
    // ═══════════════════════════════════════════
    @Nested
    @DisplayName("chat()")
    class ChatTests {

        @Test
        @DisplayName("success: returns Claude answer + suggested questions")
        void chat_Success_ReturnsAnswerAndSuggestions() {
            ChatRequest req = new ChatRequest();
            req.setQuestion("Should I increase my SIP?");

            when(userContextService.buildContext(USER_ID)).thenReturn(mockCtx);
            when(promptBuilder.buildChatSystemPrompt(mockCtx)).thenReturn("system prompt");
            when(promptBuilder.buildSuggestedQuestions(mockCtx))
                .thenReturn(List.of("How do I save tax?", "Should I prepay loan?"));
            when(chatRepository.findRecentByUserId(eq(USER_ID), any(Pageable.class)))
                .thenReturn(List.of());
            when(claudeClient.chat(any(), any(), eq(1024)))
                .thenReturn("Based on your 42% savings rate and 16.2% XIRR portfolio, " +
                            "increasing SIP by ₹5,000/month is advisable.");
            when(chatRepository.save(any())).thenReturn(null);

            ChatResponse result = aiService.chat(USER_ID, req);

            assertThat(result.getAnswer()).contains("savings rate");
            assertThat(result.getSuggestedQuestions()).hasSize(2);
            assertThat(result.getProcessingTimeMs()).isGreaterThanOrEqualTo(0);
            // Chat history must be saved for both question and answer
            verify(chatRepository, times(2)).save(any(AiChatHistory.class));
        }

        @Test
        @DisplayName("Claude API failure returns graceful fallback — no exception thrown")
        void chat_ClaudeApiFailure_ReturnsFallback() {
            ChatRequest req = new ChatRequest();
            req.setQuestion("What is my portfolio XIRR?");

            when(userContextService.buildContext(USER_ID)).thenReturn(mockCtx);
            when(promptBuilder.buildChatSystemPrompt(any())).thenReturn("system");
            when(promptBuilder.buildSuggestedQuestions(any())).thenReturn(List.of());
            when(chatRepository.findRecentByUserId(any(), any())).thenReturn(List.of());
            when(claudeClient.chat(any(), any(), anyInt()))
                .thenThrow(new ClaudeApiException("Rate limited"));
            when(chatRepository.save(any())).thenReturn(null);

            // Must NOT throw — returns fallback message
            ChatResponse result = aiService.chat(USER_ID, req);

            assertThat(result.getAnswer()).isNotBlank();
            assertThat(result.getAnswer()).contains("unavailable");
        }

        @Test
        @DisplayName("conversation history from request is used (not DB)")
        void chat_UsesRequestHistory_WhenProvided() {
            ChatRequest req = new ChatRequest();
            req.setQuestion("What about my home loan?");
            req.setConversationHistory(List.of(
                buildMsg("user",      "How is my portfolio?"),
                buildMsg("assistant", "Your portfolio XIRR is 16.2%.")
            ));

            when(userContextService.buildContext(USER_ID)).thenReturn(mockCtx);
            when(promptBuilder.buildChatSystemPrompt(any())).thenReturn("system");
            when(promptBuilder.buildSuggestedQuestions(any())).thenReturn(List.of());
            when(claudeClient.chat(any(), any(), anyInt())).thenReturn("Your EMI is ₹35,200");
            when(chatRepository.save(any())).thenReturn(null);

            aiService.chat(USER_ID, req);

            // Should NOT query DB for history since request provided it
            verify(chatRepository, never()).findRecentByUserId(any(), any());
        }
    }

    // ═══════════════════════════════════════════
    // INSIGHT GENERATION
    // ═══════════════════════════════════════════
    @Nested
    @DisplayName("generateInsights()")
    class InsightGenerationTests {

        @Test
        @DisplayName("parses valid JSON array from Claude and saves insights")
        void generate_ValidJson_SavesInsights() {
            String claudeJson = """
                [
                  {"type":"TAX","priority":1,"content":"Invest ₹26,200 in ELSS to save ₹7,874 in tax."},
                  {"type":"PORTFOLIO","priority":2,"content":"Your XIRR is 16.2% — excellent performance."},
                  {"type":"SPENDING","priority":3,"content":"Shopping up 23% vs last month."}
                ]
                """;

            when(insightRepository.hasInsightGeneratedSince(eq(USER_ID), any())).thenReturn(false);
            when(userContextService.buildContext(USER_ID)).thenReturn(mockCtx);
            when(promptBuilder.buildInsightSystemPrompt()).thenReturn("system");
            when(promptBuilder.buildInsightUserPrompt(mockCtx)).thenReturn("user prompt");
            when(claudeClient.generateInsights(any(), any())).thenReturn(claudeJson);

            ArgumentCaptor<List<AiInsight>> captor = ArgumentCaptor.forClass(List.class);
            when(insightRepository.saveAll(captor.capture())).thenAnswer(i -> i.getArgument(0));

            aiService.generateInsights(USER_ID);

            List<AiInsight> saved = captor.getValue();
            assertThat(saved).hasSize(3);
            assertThat(saved.get(0).getType()).isEqualTo(InsightType.TAX);
            assertThat(saved.get(0).getPriority()).isEqualTo(1);
            assertThat(saved.get(0).getContent()).contains("ELSS");
            assertThat(saved.get(1).getType()).isEqualTo(InsightType.PORTFOLIO);
        }

        @Test
        @DisplayName("already generated today — skips generation, returns existing")
        void generate_AlreadyDoneToday_SkipsAndReturnsExisting() {
            when(insightRepository.hasInsightGeneratedSince(eq(USER_ID), any())).thenReturn(true);
            // Return mock unread insights
            AiInsight existing = AiInsight.builder().id(1L).userId(USER_ID)
                .content("Old insight").type(InsightType.TAX).priority(2).build();
            when(insightRepository.findByUserIdAndReadFalseOrderByPriorityAscGeneratedAtDesc(USER_ID))
                .thenReturn(List.of(existing));
            when(insightRepository.countByUserIdAndReadFalse(USER_ID)).thenReturn(1L);

            List<InsightResponse> result = aiService.generateInsights(USER_ID);

            // Claude should NOT be called
            verify(claudeClient, never()).generateInsights(any(), any());
            // Should return the existing insights
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Claude API failure returns fallback insights based on context")
        void generate_ClaudeFailure_ReturnsFallbackInsights() {
            when(insightRepository.hasInsightGeneratedSince(eq(USER_ID), any())).thenReturn(false);
            when(userContextService.buildContext(USER_ID)).thenReturn(mockCtx);
            when(promptBuilder.buildInsightSystemPrompt()).thenReturn("system");
            when(promptBuilder.buildInsightUserPrompt(any())).thenReturn("prompt");
            when(claudeClient.generateInsights(any(), any()))
                .thenThrow(new ClaudeApiException("Timeout"));

            List<InsightResponse> result = aiService.generateInsights(USER_ID);

            // Must return fallback insights, never empty on failure
            assertThat(result).isNotEmpty();
            // Should mention 80C since context has remaining allowance
            assertThat(result.stream().anyMatch(i -> i.getContent().contains("80C")))
                .isTrue();
        }

        @Test
        @DisplayName("malformed JSON from Claude returns empty list gracefully")
        void generate_MalformedJson_ReturnsEmpty() {
            when(insightRepository.hasInsightGeneratedSince(eq(USER_ID), any())).thenReturn(false);
            when(userContextService.buildContext(USER_ID)).thenReturn(mockCtx);
            when(promptBuilder.buildInsightSystemPrompt()).thenReturn("system");
            when(promptBuilder.buildInsightUserPrompt(any())).thenReturn("prompt");
            when(claudeClient.generateInsights(any(), any()))
                .thenReturn("Sorry, I cannot generate insights right now."); // non-JSON response

            when(insightRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

            List<InsightResponse> result = aiService.generateInsights(USER_ID);

            // Must not throw — returns empty
            assertThat(result).isEmpty();
        }
    }

    // ═══════════════════════════════════════════
    // GET INSIGHTS
    // ═══════════════════════════════════════════
    @Nested
    @DisplayName("getInsights()")
    class GetInsightTests {

        @Test
        @DisplayName("returns unread count + insight list")
        void getInsights_ReturnsUnreadCountAndList() {
            AiInsight insight = AiInsight.builder()
                .id(1L).userId(USER_ID).type(InsightType.TAX).priority(1)
                .content("Use ₹26,200 80C limit.").read(false).build();

            when(insightRepository.findByUserIdAndReadFalseOrderByPriorityAscGeneratedAtDesc(USER_ID))
                .thenReturn(List.of(insight));
            when(insightRepository.countByUserIdAndReadFalse(USER_ID)).thenReturn(1L);

            InsightSummaryResponse result = aiService.getInsights(USER_ID);

            assertThat(result.getUnreadCount()).isEqualTo(1L);
            assertThat(result.getInsights()).hasSize(1);
            assertThat(result.getInsights().get(0).getType()).isEqualTo("TAX");
            assertThat(result.getInsights().get(0).getPriority()).isEqualTo(1);
        }
    }

    // ═══════════════════════════════════════════
    // MARK AS READ
    // ═══════════════════════════════════════════
    @Test
    @DisplayName("markInsightRead updates the insight")
    void markInsightRead_CallsRepository() {
        when(insightRepository.markRead(1L, USER_ID)).thenReturn(1);
        assertThatCode(() -> aiService.markInsightRead(1L, USER_ID))
            .doesNotThrowAnyException();
        verify(insightRepository).markRead(1L, USER_ID);
    }

    // Helper
    private ChatRequest.ConversationMessage buildMsg(String role, String content) {
        var m = new ChatRequest.ConversationMessage();
        m.setRole(role); m.setContent(content);
        return m;
    }
}
