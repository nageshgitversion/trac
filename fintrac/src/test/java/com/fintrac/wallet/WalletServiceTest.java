package com.fintrac.wallet;

import com.fintrac.common.exception.FinTracException;
import com.fintrac.wallet.dto.WalletOperationRequest;
import com.fintrac.wallet.dto.WalletResponse;
import com.fintrac.wallet.entity.Wallet;
import com.fintrac.wallet.repository.WalletRepository;
import com.fintrac.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = Wallet.builder()
            .id(1L)
            .userId(1L)
            .balance(BigDecimal.valueOf(1000))
            .currency("INR")
            .build();
    }

    @Test
    void getWallet_createsIfNotExists() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        WalletResponse response = walletService.getWallet(1L);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void credit_addsBalance() {
        WalletOperationRequest req = new WalletOperationRequest();
        req.setAmount(BigDecimal.valueOf(500));

        Wallet credited = Wallet.builder()
            .id(1L).userId(1L).balance(BigDecimal.valueOf(1500)).currency("INR").build();

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(credited);

        WalletResponse response = walletService.credit(1L, req);

        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1500));
    }

    @Test
    void debit_subtractsBalance() {
        WalletOperationRequest req = new WalletOperationRequest();
        req.setAmount(BigDecimal.valueOf(300));

        Wallet debited = Wallet.builder()
            .id(1L).userId(1L).balance(BigDecimal.valueOf(700)).currency("INR").build();

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(debited);

        WalletResponse response = walletService.debit(1L, req);

        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(700));
    }

    @Test
    void debit_insufficientBalance_throwsException() {
        WalletOperationRequest req = new WalletOperationRequest();
        req.setAmount(BigDecimal.valueOf(2000));

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.debit(1L, req))
            .isInstanceOf(FinTracException.class)
            .satisfies(ex -> {
                FinTracException fte = (FinTracException) ex;
                assertThat(fte.getErrorCode()).isEqualTo("FT-2001");
            });
    }
}
