package com.investrac.account.service;

import com.investrac.account.dto.request.CreateAccountRequest;
import com.investrac.account.dto.request.UpdateAccountRequest;
import com.investrac.account.dto.response.AccountSummaryResponse;
import com.investrac.account.dto.response.MaturityCalculationResponse;
import com.investrac.account.dto.response.VirtualAccountResponse;
import com.investrac.account.entity.VirtualAccount;
import com.investrac.account.entity.VirtualAccount.AccountType;
import com.investrac.account.exception.AccountException;
import com.investrac.account.mapper.AccountMapper;
import com.investrac.account.repository.VirtualAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    @Mock VirtualAccountRepository accountRepository;
    @Mock MaturityCalculator       maturityCalculator;
    @Mock AccountMapper            mapper;

    @InjectMocks
    AccountService accountService;

    private static final Long USER_ID = 100L;

    // ═══════════════════════════════════════════
    // CREATE ACCOUNT
    // ═══════════════════════════════════════════
    @Nested
    @DisplayName("createAccount()")
    class CreateAccountTests {

        @Test
        @DisplayName("savings account created without maturity calculation")
        void create_SavingsAccount_NoMaturityCalc() {
            CreateAccountRequest req = new CreateAccountRequest();
            req.setType(AccountType.SAVINGS);
            req.setName("Emergency Fund");
            req.setBalance(new BigDecimal("100000.00"));
            req.setGoalAmount(new BigDecimal("400000.00"));

            VirtualAccount saved = VirtualAccount.builder()
                .id(1L).userId(USER_ID).type(AccountType.SAVINGS)
                .name("Emergency Fund").balance(new BigDecimal("100000.00"))
                .build();

            when(accountRepository.save(any())).thenReturn(saved);
            when(mapper.toResponse(any())).thenReturn(VirtualAccountResponse.builder()
                .id(1L).type("SAVINGS").name("Emergency Fund").build());

            VirtualAccountResponse result = accountService.createAccount(USER_ID, req);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getType()).isEqualTo("SAVINGS");

            // MaturityCalculator must NOT be called for SAVINGS
            verify(maturityCalculator, never()).calculate(any(), any(), any(), any(), any());
            verify(accountRepository).save(any(VirtualAccount.class));
        }

        @Test
        @DisplayName("FD account auto-calculates maturity on creation")
        void create_FdAccount_AutoCalcMaturity() {
            CreateAccountRequest req = new CreateAccountRequest();
            req.setType(AccountType.FD);
            req.setName("SBI FD 7.2%");
            req.setBalance(new BigDecimal("500000.00"));
            req.setInterestRate(new BigDecimal("7.2"));
            req.setStartDate(LocalDate.of(2026, 1, 1));
            req.setMaturityDate(LocalDate.of(2028, 1, 1));

            MaturityCalculationResponse calcResult = MaturityCalculationResponse.builder()
                .principal(new BigDecimal("500000.00"))
                .interestEarned(new BigDecimal("72000.00"))
                .maturityAmount(new BigDecimal("572000.00"))
                .tenureMonths(24).tenureYears(2.0)
                .calculationMethod("SIMPLE_INTEREST")
                .build();

            when(maturityCalculator.calculate(
                eq(AccountType.FD), any(), any(), any(), any()))
                .thenReturn(calcResult);

            ArgumentCaptor<VirtualAccount> captor = ArgumentCaptor.forClass(VirtualAccount.class);
            VirtualAccount saved = VirtualAccount.builder()
                .id(2L).type(AccountType.FD).maturityAmount(new BigDecimal("572000.00")).build();
            when(accountRepository.save(captor.capture())).thenReturn(saved);
            when(mapper.toResponse(any())).thenReturn(
                VirtualAccountResponse.builder().id(2L).type("FD")
                    .maturityAmount(new BigDecimal("572000.00")).build());

            accountService.createAccount(USER_ID, req);

            // Maturity amount must be set on the entity before saving
            VirtualAccount entitySaved = captor.getValue();
            assertThat(entitySaved.getMaturityAmount())
                .isEqualByComparingTo("572000.00");
            verify(maturityCalculator).calculate(eq(AccountType.FD), any(), any(), any(), any());
        }

        @Test
        @DisplayName("RD account uses emiAmount (not balance) for maturity calculation")
        void create_RdAccount_UsesEmiAmountForCalc() {
            CreateAccountRequest req = new CreateAccountRequest();
            req.setType(AccountType.RD);
            req.setName("Monthly RD");
            req.setBalance(new BigDecimal("10000.00"));
            req.setEmiAmount(new BigDecimal("10000.00"));
            req.setInterestRate(new BigDecimal("6.8"));
            req.setStartDate(LocalDate.of(2026, 1, 1));
            req.setMaturityDate(LocalDate.of(2029, 1, 1));

            when(maturityCalculator.calculate(any(), any(), any(), any(), any()))
                .thenReturn(MaturityCalculationResponse.builder()
                    .maturityAmount(new BigDecimal("408000.00")).build());
            when(accountRepository.save(any())).thenReturn(
                VirtualAccount.builder().id(3L).type(AccountType.RD).build());
            when(mapper.toResponse(any())).thenReturn(
                VirtualAccountResponse.builder().id(3L).type("RD").build());

            accountService.createAccount(USER_ID, req);

            // Verify it used emiAmount (10000) NOT balance for RD calculation
            ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
            verify(maturityCalculator).calculate(
                eq(AccountType.RD),
                amountCaptor.capture(),   // should be emiAmount
                any(), any(), any()
            );
            assertThat(amountCaptor.getValue()).isEqualByComparingTo("10000.00");
        }

        @Test
        @DisplayName("LOAN account created without maturity calculation")
        void create_LoanAccount_NoMaturityCalc() {
            CreateAccountRequest req = new CreateAccountRequest();
            req.setType(AccountType.LOAN);
            req.setName("HDFC Home Loan");
            req.setBalance(new BigDecimal("4200000.00"));
            req.setEmiAmount(new BigDecimal("35200.00"));
            req.setEmiDay(17);
            req.setInterestRate(new BigDecimal("8.6"));

            when(accountRepository.save(any())).thenReturn(
                VirtualAccount.builder().id(5L).type(AccountType.LOAN).build());
            when(mapper.toResponse(any())).thenReturn(
                VirtualAccountResponse.builder().id(5L).type("LOAN").build());

            accountService.createAccount(USER_ID, req);

            verify(maturityCalculator, never()).calculate(any(), any(), any(), any(), any());
        }
    }

    // ═══════════════════════════════════════════
    // GET + UPDATE + DELETE
    // ═══════════════════════════════════════════
    @Nested
    @DisplayName("getById(), updateAccount(), deleteAccount()")
    class CrudTests {

        VirtualAccount mockAccount;

        @BeforeEach
        void setUp() {
            mockAccount = VirtualAccount.builder()
                .id(1L).userId(USER_ID).type(AccountType.SAVINGS)
                .name("Emergency Fund").balance(new BigDecimal("100000"))
                .active(true).build();
        }

        @Test
        @DisplayName("getById returns account for correct userId")
        void getById_Found() {
            when(accountRepository.findByIdAndUserId(1L, USER_ID))
                .thenReturn(Optional.of(mockAccount));
            when(mapper.toResponse(mockAccount)).thenReturn(
                VirtualAccountResponse.builder().id(1L).name("Emergency Fund").build());

            VirtualAccountResponse result = accountService.getById(1L, USER_ID);
            assertThat(result.getName()).isEqualTo("Emergency Fund");
        }

        @Test
        @DisplayName("getById throws NOT_FOUND when account doesn't belong to userId")
        void getById_WrongUser_Throws() {
            when(accountRepository.findByIdAndUserId(1L, 999L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getById(1L, 999L))
                .isInstanceOf(AccountException.class)
                .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("updateAccount: name and balance updated")
        void update_NameAndBalance() {
            UpdateAccountRequest req = new UpdateAccountRequest();
            req.setName("Emergency Fund Updated");
            req.setBalance(new BigDecimal("150000.00"));

            when(accountRepository.findByIdAndUserId(1L, USER_ID))
                .thenReturn(Optional.of(mockAccount));
            when(accountRepository.save(any())).thenReturn(mockAccount);
            when(mapper.toResponse(any())).thenReturn(
                VirtualAccountResponse.builder().name("Emergency Fund Updated").build());

            VirtualAccountResponse result = accountService.updateAccount(1L, USER_ID, req);
            assertThat(result.getName()).isEqualTo("Emergency Fund Updated");
            verify(accountRepository).save(mockAccount);
        }

        @Test
        @DisplayName("deleteAccount soft-deactivates the account")
        void delete_SoftDeactivates() {
            when(accountRepository.deactivate(1L, USER_ID)).thenReturn(1);

            assertThatCode(() -> accountService.deleteAccount(1L, USER_ID))
                .doesNotThrowAnyException();

            verify(accountRepository).deactivate(1L, USER_ID);
        }

        @Test
        @DisplayName("deleteAccount throws NOT_FOUND when account doesn't exist")
        void delete_NotFound_Throws() {
            when(accountRepository.deactivate(99L, USER_ID)).thenReturn(0);

            assertThatThrownBy(() -> accountService.deleteAccount(99L, USER_ID))
                .isInstanceOf(AccountException.class);
        }
    }

    // ═══════════════════════════════════════════
    // MATURITY CALCULATION
    // ═══════════════════════════════════════════
    @Nested
    @DisplayName("calculateMaturity()")
    class MaturityTests {

        @Test
        @DisplayName("FD maturity calculation returns correct values")
        void fd_MaturityCalc_CorrectValues() {
            VirtualAccount fd = VirtualAccount.builder()
                .id(1L).userId(USER_ID).type(AccountType.FD)
                .balance(new BigDecimal("500000")).interestRate(new BigDecimal("7.2"))
                .startDate(LocalDate.of(2025, 9, 14))
                .maturityDate(LocalDate.of(2027, 3, 14))
                .active(true).build();

            MaturityCalculationResponse expected = MaturityCalculationResponse.builder()
                .principal(new BigDecimal("500000.00"))
                .interestEarned(new BigDecimal("57000.00"))
                .maturityAmount(new BigDecimal("557000.00"))
                .tenureMonths(18).tenureYears(1.5)
                .calculationMethod("SIMPLE_INTEREST")
                .build();

            when(accountRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(fd));
            when(maturityCalculator.calculate(eq(AccountType.FD), any(), any(), any(), any()))
                .thenReturn(expected);

            MaturityCalculationResponse result = accountService.calculateMaturity(1L, USER_ID);

            assertThat(result.getMaturityAmount()).isEqualByComparingTo("557000.00");
            assertThat(result.getTenureMonths()).isEqualTo(18);
            assertThat(result.getCalculationMethod()).isEqualTo("SIMPLE_INTEREST");
        }

        @Test
        @DisplayName("calculateMaturity throws for SAVINGS account type")
        void savings_MaturityCalc_Throws() {
            VirtualAccount savings = VirtualAccount.builder()
                .id(1L).userId(USER_ID).type(AccountType.SAVINGS)
                .active(true).build();

            when(accountRepository.findByIdAndUserId(1L, USER_ID))
                .thenReturn(Optional.of(savings));

            assertThatThrownBy(() -> accountService.calculateMaturity(1L, USER_ID))
                .isInstanceOf(AccountException.class)
                .hasMessageContaining("FD and RD");
        }

        @Test
        @DisplayName("calculateMaturity throws when dates missing")
        void fd_NoDatesMissing_Throws() {
            VirtualAccount fd = VirtualAccount.builder()
                .id(1L).userId(USER_ID).type(AccountType.FD)
                .balance(new BigDecimal("500000"))
                .startDate(null).maturityDate(null)  // dates missing
                .active(true).build();

            when(accountRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(fd));

            assertThatThrownBy(() -> accountService.calculateMaturity(1L, USER_ID))
                .isInstanceOf(AccountException.class)
                .hasMessageContaining("dates are required");
        }
    }
}
