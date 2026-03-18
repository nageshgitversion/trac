package com.investrac.transaction.service;

import com.investrac.common.events.TransactionCreatedEvent;
import com.investrac.common.events.TransactionCompletedEvent;
import com.investrac.common.events.TransactionFailedEvent;
import com.investrac.common.response.PagedResponse;
import com.investrac.transaction.dto.*;
import com.investrac.transaction.entity.Transaction;
import com.investrac.transaction.entity.Transaction.*;
import com.investrac.transaction.exception.TransactionException;
import com.investrac.transaction.mapper.TransactionMapper;
import com.investrac.transaction.outbox.TransactionOutboxService;
import com.investrac.transaction.repository.TransactionRepository;
import com.investrac.common.dto.ErrorCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core transaction business logic.
 *
 * createTransaction:
 *  1. Saves transaction as PENDING
 *  2. Publishes TransactionCreatedEvent via Outbox (SAGA Step 1)
 *  3. wallet-service consumes → debits → publishes WalletDebitedEvent
 *  4. WalletDebitedEventConsumer updates status to COMPLETED or FAILED
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository   transactionRepository;
    private final TransactionOutboxService outboxService;
    private final TransactionMapper        mapper;

    // ══════════════════════════════════════════
    // CREATE — SAGA Step 1
    // ══════════════════════════════════════════
    @Transactional
    public TransactionResponse createTransaction(Long userId, CreateTransactionRequest req) {
        log.info("Creating transaction for userId={} type={} amount={}", userId, req.getType(), req.getAmount());

        String sagaId = UUID.randomUUID().toString();

        Transaction tx = Transaction.builder()
            .userId(userId)
            .walletId(req.getWalletId())
            .accountId(req.getAccountId())
            .type(req.getType())
            .category(req.getCategory())
            .name(req.getName().trim())
            .amount(req.getAmount())
            .envelopeKey(req.getEnvelopeKey())
            .txDate(req.getTxDate())
            .note(req.getNote())
            .source(req.getSource() != null ? req.getSource() : TransactionSource.MANUAL)
            .status(TransactionStatus.PENDING)
            .sagaId(sagaId)
            .build();

        tx = transactionRepository.save(tx);

        // If wallet is linked — trigger SAGA (wallet debit/credit)
        if (req.getWalletId() != null) {
            outboxService.publish(TransactionCreatedEvent.TOPIC,
                new TransactionCreatedEvent(
                    sagaId,
                    tx.getId(),
                    userId,
                    req.getWalletId(),
                    req.getAmount(),
                    req.getType().name().toLowerCase(),
                    req.getCategory(),
                    req.getEnvelopeKey(),
                    req.getName(),
                    req.getTxDate(),
                    tx.getSource().name().toLowerCase()
                )
            );
            log.info("SAGA initiated: sagaId={} transactionId={}", sagaId, tx.getId());
        } else {
            // No wallet linked — mark completed immediately
            tx.markCompleted();
            transactionRepository.save(tx);
        }

        return mapper.toResponse(tx);
    }

    // ══════════════════════════════════════════
    // GET — Paged + Filtered
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getTransactions(Long userId, TransactionFilterRequest filter) {
        Sort sort = Sort.by(
            "asc".equalsIgnoreCase(filter.getSortDir())
                ? Sort.Direction.ASC : Sort.Direction.DESC,
            filter.getSortBy()
        );
        PageRequest pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<Transaction> page = transactionRepository.findFiltered(
            userId,
            filter.getType(),
            filter.getCategory(),
            filter.getFrom(),
            filter.getTo(),
            filter.getSearch() != null && !filter.getSearch().isBlank() ? filter.getSearch() : null,
            pageable
        );

        Page<TransactionResponse> responsePage = page.map(mapper::toResponse);
        return PagedResponse.of(responsePage);
    }

    // ══════════════════════════════════════════
    // GET ONE
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public TransactionResponse getById(Long userId, Long txId) {
        Transaction tx = transactionRepository
            .findByIdAndUserIdAndDeletedFalse(txId, userId)
            .orElseThrow(() -> new TransactionException(
                ErrorCodes.TRANSACTION_NOT_FOUND,
                "Transaction not found", HttpStatus.NOT_FOUND));
        return mapper.toResponse(tx);
    }

    // ══════════════════════════════════════════
    // GET RECENT (home screen)
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecent(Long userId, int limit) {
        return transactionRepository
            .findRecentByUserId(userId, PageRequest.of(0, limit))
            .stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════
    // UPDATE
    // ══════════════════════════════════════════
    @Transactional
    public TransactionResponse updateTransaction(Long userId, Long txId, UpdateTransactionRequest req) {
        Transaction tx = transactionRepository
            .findByIdAndUserIdAndDeletedFalse(txId, userId)
            .orElseThrow(() -> new TransactionException(
                ErrorCodes.TRANSACTION_NOT_FOUND,
                "Transaction not found", HttpStatus.NOT_FOUND));

        if (tx.getStatus() == TransactionStatus.PENDING) {
            throw new TransactionException(ErrorCodes.TRANSACTION_FAILED,
                "Cannot update a transaction while SAGA is in progress", HttpStatus.CONFLICT);
        }

        BigDecimal oldAmount   = tx.getAmount();
        boolean    amountChanged = !oldAmount.equals(req.getAmount());

        // If wallet-linked and amount changed — compensate: reverse old, apply new via SAGA
        if (tx.getWalletId() != null && amountChanged) {
            // Reverse the old transaction effect
            BigDecimal reversalAmount = tx.isDebit() ? oldAmount.negate() : oldAmount;
            String compensatingSagaId = UUID.randomUUID().toString();

            outboxService.publish(TransactionCreatedEvent.TOPIC,
                new TransactionCreatedEvent(
                    compensatingSagaId,
                    tx.getId(),
                    userId,
                    tx.getWalletId(),
                    reversalAmount.abs(),
                    tx.isDebit() ? "income" : "expense",  // Reverse direction
                    "COMPENSATION",
                    tx.getEnvelopeKey(),
                    "Compensation for update: " + tx.getName(),
                    LocalDate.now(),
                    "manual"
                )
            );
        }

        // Apply updates
        tx.setCategory(req.getCategory());
        tx.setName(req.getName().trim());
        tx.setAmount(req.getAmount());
        tx.setTxDate(req.getTxDate());
        tx.setNote(req.getNote());
        tx.setEnvelopeKey(req.getEnvelopeKey());

        // If amount changed and wallet-linked — re-trigger SAGA for new amount
        if (tx.getWalletId() != null && amountChanged) {
            tx.setStatus(TransactionStatus.PENDING);
            String newSagaId = UUID.randomUUID().toString();
            tx.setSagaId(newSagaId);
            outboxService.publish(TransactionCreatedEvent.TOPIC,
                new TransactionCreatedEvent(
                    newSagaId, tx.getId(), userId, tx.getWalletId(),
                    req.getAmount(), tx.getType().name().toLowerCase(),
                    req.getCategory(), req.getEnvelopeKey(),
                    req.getName(), req.getTxDate(), "manual"
                )
            );
        }

        tx = transactionRepository.save(tx);
        log.info("Transaction updated: id={} userId={}", txId, userId);
        return mapper.toResponse(tx);
    }

    // ══════════════════════════════════════════
    // DELETE (soft delete + compensate wallet)
    // ══════════════════════════════════════════
    @Transactional
    public void deleteTransaction(Long userId, Long txId) {
        Transaction tx = transactionRepository
            .findByIdAndUserIdAndDeletedFalse(txId, userId)
            .orElseThrow(() -> new TransactionException(
                ErrorCodes.TRANSACTION_NOT_FOUND,
                "Transaction not found", HttpStatus.NOT_FOUND));

        if (tx.getStatus() == TransactionStatus.PENDING) {
            throw new TransactionException(ErrorCodes.TRANSACTION_FAILED,
                "Cannot delete a transaction while SAGA is in progress", HttpStatus.CONFLICT);
        }

        // If wallet-linked and COMPLETED — compensate wallet (reverse the effect)
        if (tx.getWalletId() != null && tx.getStatus() == TransactionStatus.COMPLETED) {
            String compensatingSagaId = UUID.randomUUID().toString();
            outboxService.publish(TransactionCreatedEvent.TOPIC,
                new TransactionCreatedEvent(
                    compensatingSagaId, tx.getId(), userId, tx.getWalletId(),
                    tx.getAmount(),
                    tx.isDebit() ? "income" : "expense",   // Reverse direction
                    "COMPENSATION", tx.getEnvelopeKey(),
                    "Delete compensation: " + tx.getName(),
                    LocalDate.now(), "manual"
                )
            );
            log.info("Wallet compensation published for deleted tx id={}", txId);
        }

        tx.softDelete();
        transactionRepository.save(tx);
        log.info("Transaction soft-deleted: id={} userId={}", txId, userId);
    }

    // ══════════════════════════════════════════
    // MONTHLY SUMMARY
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public MonthSummaryResponse getMonthSummary(Long userId, int year, int month) {
        BigDecimal income     = orZero(transactionRepository.sumByTypeAndMonth(userId, TransactionType.INCOME,     year, month));
        BigDecimal expense    = orZero(transactionRepository.sumByTypeAndMonth(userId, TransactionType.EXPENSE,    year, month));
        BigDecimal investment = orZero(transactionRepository.sumByTypeAndMonth(userId, TransactionType.INVESTMENT, year, month));
        BigDecimal savings    = orZero(transactionRepository.sumByTypeAndMonth(userId, TransactionType.SAVINGS,    year, month));

        BigDecimal netSavings     = income.subtract(expense);
        int        savingsRate    = income.compareTo(BigDecimal.ZERO) > 0
            ? netSavings.multiply(BigDecimal.valueOf(100))
                         .divide(income, 0, RoundingMode.HALF_UP)
                         .intValue()
            : 0;

        // Category breakdown
        List<Object[]> breakdown = transactionRepository.categoryBreakdown(userId, year, month);
        List<MonthSummaryResponse.CategoryBreakdown> categories = breakdown.stream()
            .map(row -> {
                BigDecimal catAmount = (BigDecimal) row[1];
                int pct = expense.compareTo(BigDecimal.ZERO) > 0
                    ? catAmount.multiply(BigDecimal.valueOf(100))
                               .divide(expense, 0, RoundingMode.HALF_UP)
                               .intValue()
                    : 0;
                return MonthSummaryResponse.CategoryBreakdown.builder()
                    .category((String) row[0])
                    .amount(catAmount)
                    .percentOfTotal(pct)
                    .build();
            })
            .collect(Collectors.toList());

        String monthLabel = Month.of(month)
            .getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;

        return MonthSummaryResponse.builder()
            .year(year)
            .month(month)
            .monthLabel(monthLabel)
            .totalIncome(income)
            .totalExpense(expense)
            .totalInvestment(investment)
            .totalSavings(savings)
            .netSavings(netSavings)
            .savingsRatePercent(Math.max(0, savingsRate))
            .expenseBreakdown(categories)
            .build();
    }

    // ── SAGA Status Update (called by WalletDebitedEventConsumer) ──
    @Transactional
    public void markCompleted(String sagaId) {
        int updated = transactionRepository.updateStatusBySagaId(sagaId, TransactionStatus.COMPLETED);
        if (updated == 0) {
            log.warn("No transaction found for sagaId={} during COMPLETED update", sagaId);
        } else {
            log.info("Transaction COMPLETED for sagaId={}", sagaId);
        }
    }

    @Transactional
    public void markFailed(String sagaId, String reason) {
        int updated = transactionRepository.updateStatusAndReasonBySagaId(
            sagaId, TransactionStatus.FAILED, reason);
        if (updated == 0) {
            log.warn("No transaction found for sagaId={} during FAILED update", sagaId);
        } else {
            log.warn("Transaction FAILED for sagaId={}: {}", sagaId, reason);
        }
    }

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
