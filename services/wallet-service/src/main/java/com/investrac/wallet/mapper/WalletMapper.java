package com.investrac.wallet.mapper;

import com.investrac.wallet.dto.EnvelopeResponse;
import com.investrac.wallet.dto.WalletResponse;
import com.investrac.wallet.entity.Wallet;
import com.investrac.wallet.entity.WalletEnvelope;
import com.investrac.wallet.repository.WalletEnvelopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WalletMapper {

    private final WalletEnvelopeRepository envelopeRepository;

    public WalletResponse toResponse(Wallet wallet) {
        List<EnvelopeResponse> envelopes = envelopeRepository
            .findByWalletId(wallet.getId()).stream()
            .map(this::toEnvelopeResponse)
            .collect(Collectors.toList());

        return WalletResponse.builder()
            .id(wallet.getId())
            .userId(wallet.getUserId())
            .month(wallet.getMonth())
            .income(wallet.getIncome())
            .topup(wallet.getTopup())
            .balance(wallet.getBalance())
            .committed(wallet.getCommitted())
            .freeToSpend(wallet.getFreeToSpend())
            .usedPercent(wallet.getUsedPercent())
            .active(wallet.isActive())
            .envelopes(envelopes)
            .createdAt(wallet.getCreatedAt())
            .build();
    }

    public EnvelopeResponse toEnvelopeResponse(WalletEnvelope e) {
        BigDecimal remaining = e.getRemaining();
        int usedPct = 0;
        if (e.getBudget().compareTo(BigDecimal.ZERO) > 0) {
            usedPct = e.getSpent()
                .multiply(BigDecimal.valueOf(100))
                .divide(e.getBudget(), 0, RoundingMode.HALF_UP)
                .intValue();
        }
        return EnvelopeResponse.builder()
            .id(e.getId())
            .envelopeKey(e.getEnvelopeKey())
            .categoryName(e.getCategoryName())
            .icon(e.getIcon())
            .budget(e.getBudget())
            .spent(e.getSpent())
            .remaining(remaining)
            .overBudget(e.isOverBudget())
            .usedPercent(Math.min(usedPct, 100))
            .build();
    }
}
