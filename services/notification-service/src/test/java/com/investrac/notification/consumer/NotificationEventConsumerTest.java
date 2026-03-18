package com.investrac.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.investrac.common.events.*;
import com.investrac.notification.entity.Notification.NotificationType;
import com.investrac.notification.service.EmailService;
import com.investrac.notification.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventConsumer Tests")
class NotificationEventConsumerTest {

    @Mock NotificationService notificationService;
    @Mock EmailService        emailService;
    @Mock Acknowledgment      ack;

    @InjectMocks
    NotificationEventConsumer consumer;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // Inject objectMapper via reflection since it's @InjectMocks
        try {
            var field = NotificationEventConsumer.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(consumer, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ── Transaction Completed ──────────────────────────────────

    @Test
    @DisplayName("onTransactionCompleted: sends push with correct amount and name")
    void onTransactionCompleted_SendsCorrectPush() throws Exception {
        TransactionCompletedEvent event = new TransactionCompletedEvent(
            "saga-1", 10L, 100L,
            new BigDecimal("450.00"), "Swiggy Order", "Food & Dining",
            Instant.now()
        );

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
            TransactionCompletedEvent.TOPIC, 0, 0L, null,
            objectMapper.writeValueAsString(event));

        consumer.onTransactionCompleted(record, ack);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).send(
            eq(100L),
            eq("Transaction Successful ✅"),
            bodyCaptor.capture(),
            eq(NotificationType.TRANSACTION_COMPLETED),
            anyMap()
        );

        assertThat(bodyCaptor.getValue()).contains("450.00");
        assertThat(bodyCaptor.getValue()).contains("Swiggy Order");
        verify(ack).acknowledge();   // Always ACK
    }

    @Test
    @DisplayName("onTransactionCompleted: ACKs even when notification service throws")
    void onTransactionCompleted_AlwaysAcks_EvenOnError() throws Exception {
        TransactionCompletedEvent event = new TransactionCompletedEvent(
            "saga-2", 11L, 101L, new BigDecimal("100"), "Test", "Other", Instant.now()
        );
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
            TransactionCompletedEvent.TOPIC, 0, 0L, null,
            objectMapper.writeValueAsString(event));

        doThrow(new RuntimeException("Push service down"))
            .when(notificationService).send(any(), any(), any(), any(), any());

        consumer.onTransactionCompleted(record, ack);

        // Must still ACK — notification failure ≠ SAGA failure
        verify(ack).acknowledge();
    }

    // ── Transaction Failed ─────────────────────────────────────

    @Test
    @DisplayName("onTransactionFailed: sends push with failure reason")
    void onTransactionFailed_SendsFailureNotification() throws Exception {
        TransactionFailedEvent event = new TransactionFailedEvent(
            "saga-3", 12L, 100L,
            new BigDecimal("5000.00"), "Insufficient wallet balance",
            "WLTH-2003", Instant.now()
        );

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
            TransactionFailedEvent.TOPIC, 0, 0L, null,
            objectMapper.writeValueAsString(event));

        consumer.onTransactionFailed(record, ack);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).send(
            eq(100L),
            eq("Transaction Failed ❌"),
            bodyCaptor.capture(),
            eq(NotificationType.TRANSACTION_FAILED),
            anyMap()
        );

        assertThat(bodyCaptor.getValue()).contains("Insufficient wallet balance");
        verify(ack).acknowledge();
    }

    // ── Wallet Low Balance ─────────────────────────────────────

    @Test
    @DisplayName("onWalletLowBalance: sends push with balance and commitments")
    void onWalletLowBalance_SendsAlert() throws Exception {
        WalletLowBalanceEvent event = new WalletLowBalanceEvent(
            100L, 1L,
            new BigDecimal("8500.00"),
            new BigDecimal("11500.00"),
            new BigDecimal("53600.00")
        );

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
            WalletLowBalanceEvent.TOPIC, 0, 0L, null,
            objectMapper.writeValueAsString(event));

        consumer.onWalletLowBalance(record, ack);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).send(
            eq(100L),
            eq("Wallet Running Low ⚠️"),
            bodyCaptor.capture(),
            eq(NotificationType.WALLET_LOW_BALANCE),
            anyMap()
        );

        assertThat(bodyCaptor.getValue()).contains("8500.00");
        assertThat(bodyCaptor.getValue()).contains("53600.00");
        verify(ack).acknowledge();
    }

    // ── EMI Due ────────────────────────────────────────────────

    @Test
    @DisplayName("onEmiDue: title says 'today' when daysUntilDue=0")
    void onEmiDue_Today_TitleSaysToday() throws Exception {
        AccountEmiDueEvent event = new AccountEmiDueEvent(
            5L, 100L, "HDFC Home Loan", "loan",
            new BigDecimal("35200.00"), LocalDate.now(), 0,
            new BigDecimal("51400.00")
        );

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
            AccountEmiDueEvent.TOPIC, 0, 0L, null,
            objectMapper.writeValueAsString(event));

        consumer.onEmiDue(record, ack);

        verify(notificationService).send(
            eq(100L),
            eq("EMI Due Today 🔔"),
            contains("today"),
            eq(NotificationType.EMI_DUE),
            anyMap()
        );
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("onEmiDue: title shows days remaining when daysUntilDue=3")
    void onEmiDue_3Days_TitleShowsDays() throws Exception {
        AccountEmiDueEvent event = new AccountEmiDueEvent(
            6L, 100L, "Car Loan ICICI", "loan",
            new BigDecimal("8400.00"), LocalDate.now().plusDays(3), 3,
            new BigDecimal("51400.00")
        );

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
            AccountEmiDueEvent.TOPIC, 0, 0L, null,
            objectMapper.writeValueAsString(event));

        consumer.onEmiDue(record, ack);

        verify(notificationService).send(
            eq(100L),
            eq("EMI Reminder — 3 Days"),
            contains("in 3 days"),
            eq(NotificationType.EMI_DUE),
            anyMap()
        );
        verify(ack).acknowledge();
    }

    // ── Portfolio Synced ───────────────────────────────────────

    @Test
    @DisplayName("onPortfolioSynced: skips notification when change < 1%")
    void onPortfolioSynced_SmallChange_Skipped() throws Exception {
        PortfolioSyncedEvent event = new PortfolioSyncedEvent(
            100L, 4, new BigDecimal("1860000"), new BigDecimal("1858000"),
            new BigDecimal("0.11"),   // 0.11% — below threshold
            Instant.now()
        );

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
            PortfolioSyncedEvent.TOPIC, 0, 0L, null,
            objectMapper.writeValueAsString(event));

        consumer.onPortfolioSynced(record, ack);

        verify(notificationService, never()).send(any(), any(), any(), any(), any());
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("onPortfolioSynced: sends notification when change >= 1%")
    void onPortfolioSynced_LargeChange_SendsNotification() throws Exception {
        PortfolioSyncedEvent event = new PortfolioSyncedEvent(
            100L, 4, new BigDecimal("1879000"), new BigDecimal("1860000"),
            new BigDecimal("1.02"),   // 1.02% — above threshold
            Instant.now()
        );

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
            PortfolioSyncedEvent.TOPIC, 0, 0L, null,
            objectMapper.writeValueAsString(event));

        consumer.onPortfolioSynced(record, ack);

        verify(notificationService).send(
            eq(100L), contains("Portfolio"), any(), eq(NotificationType.PORTFOLIO_SYNCED), anyMap()
        );
        verify(ack).acknowledge();
    }

    // ── User Registered (Welcome) ──────────────────────────────

    @Test
    @DisplayName("onUserRegistered: sends OTP email and welcome push")
    void onUserRegistered_SendsOtpEmailAndWelcomePush() throws Exception {
        UserRegisteredEvent event = new UserRegisteredEvent(
            100L, "arjun@investrac.in", "Arjun Kumar", "123456", Instant.now()
        );

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
            UserRegisteredEvent.TOPIC, 0, 0L, null,
            objectMapper.writeValueAsString(event));

        consumer.onUserRegistered(record, ack);

        // OTP email must be sent
        verify(emailService).sendEmail(
            eq("arjun@investrac.in"),
            eq("[INVESTRAC] Verify Your Email"),
            contains("123456")
        );

        // Welcome push must be sent
        verify(notificationService).send(
            eq(100L), eq("Welcome to INVESTRAC! 🎉"), contains("Arjun"),
            eq(NotificationType.WELCOME), anyMap()
        );

        verify(ack).acknowledge();
    }
}
