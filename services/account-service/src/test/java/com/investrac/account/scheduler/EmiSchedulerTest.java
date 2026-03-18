package com.investrac.account.scheduler;

import com.investrac.account.entity.VirtualAccount;
import com.investrac.account.entity.VirtualAccount.AccountType;
import com.investrac.account.outbox.AccountOutboxService;
import com.investrac.account.repository.VirtualAccountRepository;
import com.investrac.common.events.AccountEmiDueEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmiScheduler Unit Tests")
class EmiSchedulerTest {

    @Mock VirtualAccountRepository accountRepository;
    @Mock AccountOutboxService     outboxService;

    @InjectMocks
    EmiScheduler emiScheduler;

    private VirtualAccount homeLoan;
    private VirtualAccount rdAccount;

    @BeforeEach
    void setUp() {
        int today = LocalDate.now().getDayOfMonth();

        homeLoan = VirtualAccount.builder()
            .id(1L).userId(100L).type(AccountType.LOAN)
            .name("HDFC Home Loan")
            .emiAmount(new BigDecimal("35200.00"))
            .emiDay(today)
            .active(true).build();

        rdAccount = VirtualAccount.builder()
            .id(2L).userId(200L).type(AccountType.RD)
            .name("Monthly RD SBI")
            .emiAmount(new BigDecimal("10000.00"))
            .emiDay(today)
            .active(true).build();
    }

    @Test
    @DisplayName("processEmiDueToday: publishes events for all due accounts")
    void processEmiDueToday_PublishesEventsForAllDue() {
        int today = LocalDate.now().getDayOfMonth();

        when(accountRepository.findEmiDueAccounts(eq(today), any(LocalDate.class)))
            .thenReturn(List.of(homeLoan, rdAccount));
        doNothing().when(outboxService).publish(any(), any());

        emiScheduler.processEmiDueToday();

        // Should publish one event per account
        verify(outboxService, times(2)).publish(eq(AccountEmiDueEvent.TOPIC), any());
    }

    @Test
    @DisplayName("processEmiDueToday: event has correct amount and daysUntilDue=0")
    void processEmiDueToday_EventHasCorrectData() {
        int today = LocalDate.now().getDayOfMonth();

        when(accountRepository.findEmiDueAccounts(eq(today), any(LocalDate.class)))
            .thenReturn(List.of(homeLoan));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        doNothing().when(outboxService).publish(any(), eventCaptor.capture());

        emiScheduler.processEmiDueToday();

        AccountEmiDueEvent event = (AccountEmiDueEvent) eventCaptor.getValue();
        assertThat(event.accountId()).isEqualTo(1L);
        assertThat(event.userId()).isEqualTo(100L);
        assertThat(event.emiAmount()).isEqualByComparingTo("35200.00");
        assertThat(event.daysUntilDue()).isEqualTo(0);
        assertThat(event.accountName()).isEqualTo("HDFC Home Loan");
        assertThat(event.accountType()).isEqualTo("loan");
    }

    @Test
    @DisplayName("processEmiDueToday: no events published when no accounts due")
    void processEmiDueToday_NoAccountsDue_NoEvents() {
        int today = LocalDate.now().getDayOfMonth();

        when(accountRepository.findEmiDueAccounts(eq(today), any(LocalDate.class)))
            .thenReturn(List.of());

        emiScheduler.processEmiDueToday();

        verify(outboxService, never()).publish(any(), any());
    }

    @Test
    @DisplayName("processEmiDueToday: continues processing remaining accounts if one fails")
    void processEmiDueToday_OneFailure_ContinuesOthers() {
        int today = LocalDate.now().getDayOfMonth();

        when(accountRepository.findEmiDueAccounts(eq(today), any(LocalDate.class)))
            .thenReturn(List.of(homeLoan, rdAccount));

        // First account throws, second should still be processed
        doThrow(new RuntimeException("Kafka unavailable"))
            .doNothing()
            .when(outboxService).publish(any(), any());

        // Should NOT throw — scheduler must be resilient
        assertThatCode(() -> emiScheduler.processEmiDueToday())
            .doesNotThrowAnyException();

        // Both accounts attempted
        verify(outboxService, times(2)).publish(any(), any());
    }

    @Test
    @DisplayName("processEmiDueIn3Days: publishes 3-day advance alerts")
    void processEmiIn3Days_PublishesWithCorrectDaysUntilDue() {
        LocalDate threeDaysAhead = LocalDate.now().plusDays(3);
        int dueDay = threeDaysAhead.getDayOfMonth();

        VirtualAccount upcomingLoan = VirtualAccount.builder()
            .id(3L).userId(300L).type(AccountType.LOAN)
            .name("Car Loan").emiAmount(new BigDecimal("8400.00"))
            .emiDay(dueDay).active(true).build();

        when(accountRepository.findEmiDueAccounts(eq(dueDay), any(LocalDate.class)))
            .thenReturn(List.of(upcomingLoan));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        doNothing().when(outboxService).publish(any(), captor.capture());

        emiScheduler.processEmiDueIn3Days();

        AccountEmiDueEvent event = (AccountEmiDueEvent) captor.getValue();
        assertThat(event.daysUntilDue()).isEqualTo(3);
        assertThat(event.emiAmount()).isEqualByComparingTo("8400.00");
    }
}
