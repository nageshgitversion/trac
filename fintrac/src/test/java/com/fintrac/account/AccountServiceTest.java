package com.fintrac.account;

import com.fintrac.account.dto.*;
import com.fintrac.account.entity.*;
import com.fintrac.account.repository.AccountRepository;
import com.fintrac.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createFdAccount_autoCalculatesMaturityDate() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setType(AccountType.FD);
        req.setName("HDFC FD");
        req.setPrincipal(BigDecimal.valueOf(100000));
        req.setInterestRate(BigDecimal.valueOf(7.0));
        req.setTenureMonths(12);
        req.setStartDate(LocalDate.of(2024, 1, 1));

        Account savedAccount = Account.builder()
            .id(1L).userId(1L).type(AccountType.FD).name("HDFC FD")
            .principal(BigDecimal.valueOf(100000))
            .interestRate(BigDecimal.valueOf(7.0))
            .tenureMonths(12)
            .startDate(LocalDate.of(2024, 1, 1))
            .maturityDate(LocalDate.of(2025, 1, 1))
            .status(AccountStatus.ACTIVE)
            .createdAt(Instant.now())
            .build();

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        AccountResponse response = accountService.create(1L, req);

        assertThat(response).isNotNull();
        assertThat(response.getMaturityDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(response.getType()).isEqualTo(AccountType.FD);
    }

    @Test
    void createLoanAccount_setsActiveStatus() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setType(AccountType.LOAN);
        req.setName("Home Loan");
        req.setPrincipal(BigDecimal.valueOf(2000000));
        req.setInterestRate(BigDecimal.valueOf(8.5));
        req.setTenureMonths(240);

        Account savedAccount = Account.builder()
            .id(2L).userId(1L).type(AccountType.LOAN).name("Home Loan")
            .principal(BigDecimal.valueOf(2000000))
            .interestRate(BigDecimal.valueOf(8.5))
            .tenureMonths(240)
            .startDate(LocalDate.now())
            .maturityDate(LocalDate.now().plusMonths(240))
            .status(AccountStatus.ACTIVE)
            .createdAt(Instant.now())
            .build();

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        AccountResponse response = accountService.create(1L, req);

        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo(AccountType.LOAN);
        assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void projection_fdAccount_calculatesMaturityAmount() {
        Account fdAccount = Account.builder()
            .id(1L).userId(1L).type(AccountType.FD).name("HDFC FD")
            .principal(BigDecimal.valueOf(100000))
            .interestRate(BigDecimal.valueOf(7.0))
            .tenureMonths(12)
            .startDate(LocalDate.of(2024, 1, 1))
            .maturityDate(LocalDate.of(2025, 1, 1))
            .status(AccountStatus.ACTIVE)
            .active(true)
            .build();

        when(accountRepository.findByIdAndUserIdAndActiveTrue(1L, 1L)).thenReturn(Optional.of(fdAccount));

        AccountProjectionResponse projection = accountService.projection(1L, 1L);

        assertThat(projection).isNotNull();
        assertThat(projection.getMaturityAmount()).isGreaterThan(BigDecimal.valueOf(100000));
        assertThat(projection.getTotalInterest()).isGreaterThan(BigDecimal.ZERO);
        assertThat(projection.getMonthlyEmi()).isNull();
    }

    @Test
    void projection_loanAccount_calculatesEmi() {
        Account loanAccount = Account.builder()
            .id(2L).userId(1L).type(AccountType.LOAN).name("Car Loan")
            .principal(BigDecimal.valueOf(500000))
            .interestRate(BigDecimal.valueOf(9.0))
            .tenureMonths(60)
            .startDate(LocalDate.now())
            .status(AccountStatus.ACTIVE)
            .active(true)
            .build();

        when(accountRepository.findByIdAndUserIdAndActiveTrue(2L, 1L)).thenReturn(Optional.of(loanAccount));

        AccountProjectionResponse projection = accountService.projection(1L, 2L);

        assertThat(projection).isNotNull();
        assertThat(projection.getMonthlyEmi()).isNotNull();
        assertThat(projection.getMonthlyEmi()).isGreaterThan(BigDecimal.ZERO);
        assertThat(projection.getTotalInterest()).isGreaterThan(BigDecimal.ZERO);
    }
}
