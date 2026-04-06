package com.fintrac.transaction.service;

import com.fintrac.common.exception.FinTracException;
import com.fintrac.transaction.dto.*;
import com.fintrac.transaction.entity.*;
import com.fintrac.transaction.repository.TransactionRepository;
import com.fintrac.wallet.entity.Wallet;
import com.fintrac.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository txRepository;
    private final WalletService walletService;

    @Transactional
    public TransactionResponse create(Long userId, CreateTransactionRequest req) {
        Wallet wallet = walletService.getOrCreate(userId);

        if (req.getType() == TransactionType.DEBIT) {
            if (wallet.getBalance().compareTo(req.getAmount()) < 0)
                throw new FinTracException("FT-3001", "Insufficient wallet balance", HttpStatus.BAD_REQUEST);
            wallet.setBalance(wallet.getBalance().subtract(req.getAmount()));
        } else {
            wallet.setBalance(wallet.getBalance().add(req.getAmount()));
        }

        Transaction tx = Transaction.builder()
            .userId(userId)
            .walletId(wallet.getId())
            .type(req.getType())
            .category(req.getCategory())
            .name(req.getName())
            .amount(req.getAmount())
            .txDate(req.getTxDate())
            .note(req.getNote())
            .build();

        return toDto(txRepository.save(tx));
    }

    public Page<TransactionResponse> list(Long userId, TransactionType type,
            TransactionCategory category, LocalDate from, LocalDate to, Pageable pageable) {
        return txRepository.findFiltered(userId, type, category, from, to, pageable).map(this::toDto);
    }

    @Transactional
    public TransactionResponse update(Long userId, Long id, UpdateTransactionRequest req) {
        Transaction tx = txRepository.findByIdAndUserIdAndActiveTrue(id, userId)
            .orElseThrow(() -> new FinTracException("FT-3002", "Transaction not found", HttpStatus.NOT_FOUND));
        if (req.getName() != null) tx.setName(req.getName());
        if (req.getCategory() != null) tx.setCategory(req.getCategory());
        if (req.getNote() != null) tx.setNote(req.getNote());
        return toDto(txRepository.save(tx));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Transaction tx = txRepository.findByIdAndUserIdAndActiveTrue(id, userId)
            .orElseThrow(() -> new FinTracException("FT-3002", "Transaction not found", HttpStatus.NOT_FOUND));
        tx.setActive(false);
        txRepository.save(tx);
    }

    public TransactionSummaryResponse summary(Long userId, int year, int month) {
        BigDecimal income = txRepository.sumByTypeAndMonth(userId, TransactionType.CREDIT, year, month);
        BigDecimal expense = txRepository.sumByTypeAndMonth(userId, TransactionType.DEBIT, year, month);
        return TransactionSummaryResponse.builder()
            .totalIncome(income).totalExpense(expense)
            .netSavings(income.subtract(expense))
            .year(year).month(month).build();
    }

    private TransactionResponse toDto(Transaction t) {
        return TransactionResponse.builder()
            .id(t.getId()).userId(t.getUserId()).type(t.getType())
            .category(t.getCategory()).name(t.getName()).amount(t.getAmount())
            .txDate(t.getTxDate()).note(t.getNote()).status(t.getStatus())
            .createdAt(t.getCreatedAt()).build();
    }
}
