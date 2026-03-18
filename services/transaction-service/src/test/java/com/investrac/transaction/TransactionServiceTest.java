package com.investrac.transaction;

import com.investrac.transaction.dto.CreateTransactionRequest;
import com.investrac.transaction.dto.MonthSummaryResponse;
import com.investrac.transaction.dto.TransactionResponse;
import com.investrac.transaction.entity.Transaction;
import com.investrac.transaction.entity.Transaction.*;
import com.investrac.transaction.exception.TransactionException;
import com.investrac.transaction.mapper.TransactionMapper;
import com.investrac.transaction.outbox.TransactionOutboxService;
import com.investrac.transaction.repository.TransactionRepository;
import com.investrac.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

    @Mock TransactionRepository    transactionRepository;
    @Mock TransactionOutboxService outboxService;
    @Mock TransactionMapper        mapper;

    @InjectMocks
    TransactionService transactionService;

    private Transaction mockTx;
    private TransactionResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockTx = Transaction.builder()
            .id(1L)
            .userId(100L)
            .walletId(10L)
            .type(TransactionType.EXPENSE)
            .category("Food & Dining")
            .name("Swiggy Order")
            .amount(new BigDecimal("450.00"))
            .envelopeKey("food")
            .txDate(LocalDate.now())
            .status(TransactionStatus.PENDING)
            .sagaId("test-saga-id")
            .build();

        mockResponse = TransactionResponse.builder()
            .id(1L).userId(100L).amount(new BigDecimal("450.00"))
            .status(TransactionStatus.PENDING)
            .build();
    }

    // ── CREATE ──────────────────────────────────────────────

    @Test
    @DisplayName("createTransaction: success with wallet link — triggers SAGA")
    void createTransaction_WithWallet_TriggersSaga() {
        CreateTransactionRequest req = buildCreateRequest(
            TransactionType.EXPENSE, "Food & Dining", "Swiggy", new BigDecimal("450"), 10L);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTx);
        when(mapper.toResponse(any())).thenReturn(mockResponse);
        doNothing().when(outboxService).publish(any(), any());

        TransactionResponse result = transactionService.createTransaction(100L, req);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.PENDING);
        verify(outboxService).publish(eq("investrac.transaction.created"), any());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("createTransaction: success without wallet — marks COMPLETED immediately")
    void createTransaction_NoWallet_CompletedImmediately() {
        mockTx.setWalletId(null);
        mockTx.setStatus(TransactionStatus.COMPLETED);
        mockResponse = TransactionResponse.builder().id(1L)
            .status(TransactionStatus.COMPLETED).build();

        CreateTransactionRequest req = buildCreateRequest(
            TransactionType.EXPENSE, "Food & Dining", "Cash purchase", new BigDecimal("200"), null);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTx);
        when(mapper.toResponse(any())).thenReturn(mockResponse);

        TransactionResponse result = transactionService.createTransaction(100L, req);

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        // No Kafka event published — no wallet involved
        verify(outboxService, never()).publish(any(), any());
    }

    @Test
    @DisplayName("createTransaction: income type — SAGA should credit wallet")
    void createTransaction_Income_SagaCredit() {
        CreateTransactionRequest req = buildCreateRequest(
            TransactionType.INCOME, "Income", "Salary TCS", new BigDecimal("115000"), 10L);

        when(transactionRepository.save(any())).thenReturn(mockTx);
        when(mapper.toResponse(any())).thenReturn(mockResponse);
        doNothing().when(outboxService).publish(any(), any());

        transactionService.createTransaction(100L, req);

        verify(outboxService).publish(eq("investrac.transaction.created"), any());
    }

    // ── GET ──────────────────────────────────────────────────

    @Test
    @DisplayName("getById: success — returns transaction for correct user")
    void getById_Success() {
        when(transactionRepository.findByIdAndUserIdAndDeletedFalse(1L, 100L))
            .thenReturn(Optional.of(mockTx));
        when(mapper.toResponse(mockTx)).thenReturn(mockResponse);

        TransactionResponse result = transactionService.getById(100L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getById: throws NOT_FOUND if transaction belongs to different user")
    void getById_NotFound_ThrowsException() {
        when(transactionRepository.findByIdAndUserIdAndDeletedFalse(1L, 999L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getById(999L, 1L))
            .isInstanceOf(TransactionException.class)
            .hasMessageContaining("Transaction not found");
    }

    // ── DELETE ───────────────────────────────────────────────

    @Test
    @DisplayName("deleteTransaction: soft deletes and compensates wallet")
    void deleteTransaction_SoftDeleteAndCompensate() {
        mockTx.setStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findByIdAndUserIdAndDeletedFalse(1L, 100L))
            .thenReturn(Optional.of(mockTx));
        when(transactionRepository.save(any())).thenReturn(mockTx);
        doNothing().when(outboxService).publish(any(), any());

        transactionService.deleteTransaction(100L, 1L);

        verify(transactionRepository).save(argThat(t -> t.isDeleted()));
        // Compensation event published
        verify(outboxService).publish(eq("investrac.transaction.created"), any());
    }

    @Test
    @DisplayName("deleteTransaction: throws if transaction is PENDING (SAGA in progress)")
    void deleteTransaction_PendingStatus_ThrowsException() {
        mockTx.setStatus(TransactionStatus.PENDING);
        when(transactionRepository.findByIdAndUserIdAndDeletedFalse(1L, 100L))
            .thenReturn(Optional.of(mockTx));

        assertThatThrownBy(() -> transactionService.deleteTransaction(100L, 1L))
            .isInstanceOf(TransactionException.class)
            .hasMessageContaining("SAGA is in progress");
    }

    // ── SAGA STATUS ──────────────────────────────────────────

    @Test
    @DisplayName("markCompleted: updates transaction status via sagaId")
    void markCompleted_UpdatesStatus() {
        when(transactionRepository.updateStatusBySagaId("saga-123", TransactionStatus.COMPLETED))
            .thenReturn(1);

        transactionService.markCompleted("saga-123");

        verify(transactionRepository).updateStatusBySagaId("saga-123", TransactionStatus.COMPLETED);
    }

    @Test
    @DisplayName("markFailed: updates status and reason via sagaId")
    void markFailed_UpdatesStatusAndReason() {
        when(transactionRepository.updateStatusAndReasonBySagaId(
            "saga-456", TransactionStatus.FAILED, "Insufficient balance")).thenReturn(1);

        transactionService.markFailed("saga-456", "Insufficient balance");

        verify(transactionRepository).updateStatusAndReasonBySagaId(
            "saga-456", TransactionStatus.FAILED, "Insufficient balance");
    }

    // ── MONTHLY SUMMARY ──────────────────────────────────────

    @Test
    @DisplayName("getMonthSummary: calculates savings rate correctly")
    void getMonthSummary_CalculatesSavingsRate() {
        when(transactionRepository.sumByTypeAndMonth(100L, TransactionType.INCOME,     2026, 3))
            .thenReturn(new BigDecimal("115000"));
        when(transactionRepository.sumByTypeAndMonth(100L, TransactionType.EXPENSE,    2026, 3))
            .thenReturn(new BigDecimal("38420"));
        when(transactionRepository.sumByTypeAndMonth(100L, TransactionType.INVESTMENT, 2026, 3))
            .thenReturn(new BigDecimal("15000"));
        when(transactionRepository.sumByTypeAndMonth(100L, TransactionType.SAVINGS,    2026, 3))
            .thenReturn(new BigDecimal("10000"));
        when(transactionRepository.categoryBreakdown(100L, 2026, 3))
            .thenReturn(List.of());

        MonthSummaryResponse summary = transactionService.getMonthSummary(100L, 2026, 3);

        assertThat(summary.getTotalIncome()).isEqualByComparingTo("115000");
        assertThat(summary.getTotalExpense()).isEqualByComparingTo("38420");
        assertThat(summary.getNetSavings()).isEqualByComparingTo("76580");
        assertThat(summary.getSavingsRatePercent()).isEqualTo(66);   // 76580/115000*100
        assertThat(summary.getMonthLabel()).isEqualTo("March 2026");
    }

    // ── Helper ───────────────────────────────────────────────

    private CreateTransactionRequest buildCreateRequest(
            TransactionType type, String category, String name,
            BigDecimal amount, Long walletId) {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setType(type);
        req.setCategory(category);
        req.setName(name);
        req.setAmount(amount);
        req.setTxDate(LocalDate.now());
        req.setWalletId(walletId);
        req.setEnvelopeKey("food");
        return req;
    }
}
