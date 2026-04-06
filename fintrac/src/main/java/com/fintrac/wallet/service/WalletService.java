package com.fintrac.wallet.service;

import com.fintrac.common.exception.FinTracException;
import com.fintrac.wallet.dto.*;
import com.fintrac.wallet.entity.Wallet;
import com.fintrac.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletResponse getWallet(Long userId) {
        Wallet wallet = getOrCreate(userId);
        return toDto(wallet);
    }

    @Transactional
    public WalletResponse credit(Long userId, WalletOperationRequest req) {
        Wallet wallet = getOrCreate(userId);
        wallet.setBalance(wallet.getBalance().add(req.getAmount()));
        return toDto(walletRepository.save(wallet));
    }

    @Transactional
    public WalletResponse debit(Long userId, WalletOperationRequest req) {
        Wallet wallet = getOrCreate(userId);
        if (wallet.getBalance().compareTo(req.getAmount()) < 0) {
            throw new FinTracException("FT-2001", "Insufficient balance", HttpStatus.BAD_REQUEST);
        }
        wallet.setBalance(wallet.getBalance().subtract(req.getAmount()));
        return toDto(walletRepository.save(wallet));
    }

    public Wallet getOrCreate(Long userId) {
        return walletRepository.findByUserId(userId)
            .orElseGet(() -> walletRepository.save(
                Wallet.builder().userId(userId).build()));
    }

    private WalletResponse toDto(Wallet w) {
        return WalletResponse.builder()
            .id(w.getId()).userId(w.getUserId())
            .balance(w.getBalance()).currency(w.getCurrency())
            .updatedAt(w.getUpdatedAt()).build();
    }
}
