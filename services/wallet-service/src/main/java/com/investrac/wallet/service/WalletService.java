package com.investrac.wallet.service;

import com.investrac.common.dto.ErrorCodes;
import com.investrac.common.events.WalletDebitedEvent;
import com.investrac.common.events.WalletLowBalanceEvent;
import com.investrac.wallet.dto.*;
import com.investrac.wallet.entity.Wallet;
import com.investrac.wallet.entity.WalletEnvelope;
import com.investrac.wallet.exception.WalletException;
import com.investrac.wallet.mapper.WalletMapper;
import com.investrac.wallet.outbox.WalletOutboxService;
import com.investrac.wallet.repository.WalletEnvelopeRepository;
import com.investrac.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository         walletRepository;
    private final WalletEnvelopeRepository envelopeRepository;
    private final WalletMapper             walletMapper;
    private final WalletOutboxService      outboxService;

    // Low balance threshold: 20% of income
    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("0.20");

    // ══════════════════════════════════════════
    // CREATE WALLET
    // ══════════════════════════════════════════
    @Transactional
    public WalletResponse createWallet(Long userId, CreateWalletRequest request) {
        log.info("Creating wallet for userId={} month={}", userId, request.getMonth());

        if (walletRepository.existsByUserIdAndMonth(userId, request.getMonth())) {
            throw new WalletException(ErrorCodes.WALLET_ALREADY_EXISTS,
                "Wallet already exists for " + request.getMonth(), HttpStatus.CONFLICT);
        }

        // Deactivate previous month wallet if exists
        walletRepository.findByUserIdAndActiveTrue(userId)
            .ifPresent(prev -> {
                prev.setActive(false);
                walletRepository.save(prev);
            });

        BigDecimal totalFunds = request.getIncome().add(
            request.getTopup() != null ? request.getTopup() : BigDecimal.ZERO
        );

        Wallet wallet = Wallet.builder()
            .userId(userId)
            .month(request.getMonth())
            .income(request.getIncome())
            .topup(request.getTopup() != null ? request.getTopup() : BigDecimal.ZERO)
            .balance(totalFunds)
            .committed(BigDecimal.ZERO)
            .active(true)
            .build();

        wallet = walletRepository.save(wallet);

        // Save envelope budgets
        if (request.getEnvelopes() != null && !request.getEnvelopes().isEmpty()) {
            saveEnvelopes(wallet, request.getEnvelopes());
        }

        log.info("Wallet created id={} balance={} for userId={}", wallet.getId(), wallet.getBalance(), userId);
        return walletMapper.toResponse(wallet);
    }

    // ══════════════════════════════════════════
    // GET CURRENT WALLET
    // ══════════════════════════════════════════
    @Transactional(readOnly = true)
    public WalletResponse getCurrentWallet(Long userId) {
        String currentMonth = YearMonth.now().toString();
        Wallet wallet = walletRepository.findByUserIdAndMonthAndActiveTrue(userId, currentMonth)
            .orElseThrow(() -> new WalletException(ErrorCodes.WALLET_NOT_FOUND,
                "No active wallet found for " + currentMonth + ". Please setup your wallet.",
                HttpStatus.NOT_FOUND));
        return walletMapper.toResponse(wallet);
    }

    // ══════════════════════════════════════════
    // TOP-UP
    // ══════════════════════════════════════════
    @Transactional
    public WalletResponse topUp(Long userId, TopUpRequest request) {
        String currentMonth = YearMonth.now().toString();
        Wallet wallet = getActiveWallet(userId, currentMonth);

        walletRepository.topUp(wallet.getId(), request.getAmount());
        log.info("Wallet topped up by ₹{} for userId={}", request.getAmount(), userId);

        return walletMapper.toResponse(walletRepository.findById(wallet.getId()).orElseThrow());
    }

    // ══════════════════════════════════════════
    // DEBIT — called by SAGA consumer
    // ══════════════════════════════════════════
    @Transactional
    public WalletDebitedEvent debit(String sagaId, Long transactionId, Long userId,
                                    Long walletId, BigDecimal amount, String envelopeKey) {
        log.info("SAGA debit: sagaId={} userId={} amount={}", sagaId, userId, amount);

        Wallet wallet = walletRepository.findById(walletId)
            .orElse(null);

        if (wallet == null || !wallet.getUserId().equals(userId)) {
            return WalletDebitedEvent.failure(sagaId, transactionId, userId, walletId,
                amount, ErrorCodes.WALLET_NOT_FOUND, "Wallet not found");
        }

        if (!wallet.hasSufficientBalance(amount)) {
            log.warn("Insufficient balance for userId={}: balance={} required={}",
                userId, wallet.getBalance(), amount);
            return WalletDebitedEvent.failure(sagaId, transactionId, userId, walletId,
                amount, ErrorCodes.INSUFFICIENT_BALANCE,
                "Insufficient wallet balance. Available: ₹" + wallet.getBalance());
        }

        // Atomic debit using optimistic lock
        int updated = walletRepository.debitBalance(walletId, amount);
        if (updated == 0) {
            return WalletDebitedEvent.failure(sagaId, transactionId, userId, walletId,
                amount, ErrorCodes.INSUFFICIENT_BALANCE, "Balance check failed (concurrent update)");
        }

        // Debit envelope if specified
        if (envelopeKey != null && !envelopeKey.isBlank()) {
            envelopeRepository.addSpending(walletId, envelopeKey, amount);
        }

        // Check if balance is now low — publish alert via outbox
        Wallet updated2 = walletRepository.findById(walletId).orElseThrow();
        BigDecimal threshold = wallet.getIncome().multiply(LOW_BALANCE_THRESHOLD);
        if (updated2.getBalance().compareTo(threshold) < 0) {
            log.warn("Low balance alert for userId={}: balance={}", userId, updated2.getBalance());
            outboxService.publish(WalletLowBalanceEvent.TOPIC,
                new WalletLowBalanceEvent(userId, walletId, updated2.getBalance(),
                    threshold, wallet.getCommitted()));
        }

        log.info("SAGA debit SUCCESS: sagaId={} newBalance={}", sagaId, updated2.getBalance());
        return WalletDebitedEvent.success(sagaId, transactionId, userId, walletId, amount);
    }

    // ══════════════════════════════════════════
    // CREDIT — for income transactions
    // ══════════════════════════════════════════
    @Transactional
    public void credit(Long userId, Long walletId, BigDecimal amount) {
        walletRepository.creditBalance(walletId, amount);
        log.info("Wallet credited ₹{} for userId={}", amount, userId);
    }

    // ══════════════════════════════════════════
    // UPDATE COMMITTED (from account-service EMI scheduler)
    // ══════════════════════════════════════════
    @Transactional
    public void updateCommitted(Long userId, BigDecimal totalCommitted) {
        String currentMonth = YearMonth.now().toString();
        walletRepository.findByUserIdAndMonthAndActiveTrue(userId, currentMonth)
            .ifPresent(w -> {
                w.setCommitted(totalCommitted);
                walletRepository.save(w);
            });
    }

    // ══════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════
    private Wallet getActiveWallet(Long userId, String month) {
        return walletRepository.findByUserIdAndMonthAndActiveTrue(userId, month)
            .orElseThrow(() -> new WalletException(ErrorCodes.WALLET_NOT_FOUND,
                "No active wallet found. Please setup wallet first.", HttpStatus.NOT_FOUND));
    }

    private void saveEnvelopes(Wallet wallet, Map<String, BigDecimal> envelopeMap) {
        Map<String, String[]> envelopeMeta = Map.of(
            "food",      new String[]{"Food & Dining",   "🍕"},
            "groceries", new String[]{"Groceries",        "🛒"},
            "transport", new String[]{"Transport",        "🚗"},
            "shopping",  new String[]{"Shopping",         "🛍️"},
            "ent",       new String[]{"Entertainment",    "🎬"},
            "health",    new String[]{"Healthcare",       "💊"},
            "other",     new String[]{"Others",           "📦"}
        );

        List<WalletEnvelope> envelopes = envelopeMap.entrySet().stream()
            .filter(e -> e.getValue() != null && e.getValue().compareTo(BigDecimal.ZERO) > 0)
            .map(e -> {
                String[] meta = envelopeMeta.getOrDefault(e.getKey(),
                    new String[]{e.getKey(), "📦"});
                return WalletEnvelope.builder()
                    .wallet(wallet)
                    .envelopeKey(e.getKey())
                    .categoryName(meta[0])
                    .icon(meta[1])
                    .budget(e.getValue())
                    .spent(BigDecimal.ZERO)
                    .build();
            })
            .collect(Collectors.toList());

        envelopeRepository.saveAll(envelopes);
    }
}
