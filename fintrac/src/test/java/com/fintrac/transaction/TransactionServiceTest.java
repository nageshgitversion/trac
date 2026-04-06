package com.fintrac.transaction;

import com.fintrac.common.exception.FinTracException;
import com.fintrac.transaction.dto.*;
import com.fintrac.transaction.entity.*;
import com.fintrac.transaction.repository.TransactionRepository;
import com.fintrac.transaction.service.TransactionService;
import com.fintrac.wallet.entity.Wallet;
import com.fintrac.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository txRepository;
    @Mock
    private WalletService walletService;

    @InjectMocks
    private TransactionService transactionService;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = Wallet.builder()
            .id(1L)
            .userId(1L)
            .balance(BigDecimal.valueOf(5000))
            .currency("INR")
            .build();
    }

    @Test
    void create_creditTransaction_addsToWallet() {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setType(TransactionType.CREDIT);
        req.setCategory(TransactionCategory.SALARY);
        req.setName("Monthly Salary");
        req.setAmount(BigDecimal.valueOf(10000));
        req.setTxDate(LocalDate.now());

        Transaction savedTx = Transaction.builder()
            .id(1L).userId(1L).walletId(1L)
            .type(TransactionType.CREDIT).category(TransactionCategory.SALARY)
            .name("Monthly Salary").amount(BigDecimal.valueOf(10000))
            .txDate(LocalDate.now()).status(TransactionStatus.COMPLETED)
            .build();

        when(walletService.getOrCreate(1L)).thenReturn(wallet);
        when(txRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse response = transactionService.create(1L, req);

        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo(TransactionType.CREDIT);
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(15000));
    }

    @Test
    void create_debitTransaction_subtractsFromWallet() {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setType(TransactionType.DEBIT);
        req.setCategory(TransactionCategory.FOOD);
        req.setName("Groceries");
        req.setAmount(BigDecimal.valueOf(500));
        req.setTxDate(LocalDate.now());

        Transaction savedTx = Transaction.builder()
            .id(2L).userId(1L).walletId(1L)
            .type(TransactionType.DEBIT).category(TransactionCategory.FOOD)
            .name("Groceries").amount(BigDecimal.valueOf(500))
            .txDate(LocalDate.now()).status(TransactionStatus.COMPLETED)
            .build();

        when(walletService.getOrCreate(1L)).thenReturn(wallet);
        when(txRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse response = transactionService.create(1L, req);

        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo(TransactionType.DEBIT);
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(4500));
    }

    @Test
    void create_debitWithInsufficientBalance_throwsException() {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setType(TransactionType.DEBIT);
        req.setCategory(TransactionCategory.SHOPPING);
        req.setName("Laptop");
        req.setAmount(BigDecimal.valueOf(100000));
        req.setTxDate(LocalDate.now());

        when(walletService.getOrCreate(1L)).thenReturn(wallet);

        assertThatThrownBy(() -> transactionService.create(1L, req))
            .isInstanceOf(FinTracException.class)
            .satisfies(ex -> {
                FinTracException fte = (FinTracException) ex;
                assertThat(fte.getErrorCode()).isEqualTo("FT-3001");
            });
    }

    @Test
    void summary_returnsCorrectTotals() {
        when(txRepository.sumByTypeAndMonth(1L, TransactionType.CREDIT, 2024, 1))
            .thenReturn(BigDecimal.valueOf(10000));
        when(txRepository.sumByTypeAndMonth(1L, TransactionType.DEBIT, 2024, 1))
            .thenReturn(BigDecimal.valueOf(3000));

        TransactionSummaryResponse summary = transactionService.summary(1L, 2024, 1);

        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(summary.getTotalExpense()).isEqualByComparingTo(BigDecimal.valueOf(3000));
        assertThat(summary.getNetSavings()).isEqualByComparingTo(BigDecimal.valueOf(7000));
        assertThat(summary.getYear()).isEqualTo(2024);
        assertThat(summary.getMonth()).isEqualTo(1);
    }
}
