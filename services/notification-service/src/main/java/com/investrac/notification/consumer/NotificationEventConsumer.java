package com.investrac.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investrac.common.events.*;
import com.investrac.notification.entity.Notification.NotificationType;
import com.investrac.notification.service.EmailService;
import com.investrac.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumes ALL notification-relevant Kafka events.
 *
 * Topics consumed:
 *   investrac.transaction.completed   → "Transaction successful" push + email
 *   investrac.transaction.failed      → "Transaction failed" push + email (high priority)
 *   investrac.wallet.low-balance      → "Wallet running low" push + email
 *   investrac.account.emi-due         → "EMI due" push + email reminder
 *   investrac.portfolio.synced        → "Portfolio updated" push (off by default)
 *   investrac.user.registered         → Welcome email + OTP push
 *
 * Each consumer:
 *  1. Deserializes the event
 *  2. Calls NotificationService.send() (async — non-blocking)
 *  3. ACKs the Kafka message regardless of delivery result
 *     (notification failure must not cause Kafka retry loop)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final EmailService        emailService;
    private final ObjectMapper        objectMapper;

    // ── Transaction Completed ─────────────────────────────────
    @KafkaListener(
        topics    = TransactionCompletedEvent.TOPIC,
        groupId   = "${spring.kafka.consumer.group-id:notification-service-group}",
        concurrency = "2"
    )
    public void onTransactionCompleted(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            TransactionCompletedEvent event = objectMapper.readValue(
                record.value(), TransactionCompletedEvent.class);

            log.info("Notification: transaction completed userId={} txId={}",
                event.userId(), event.transactionId());

            // Format: "₹450.00 debited — Swiggy Order"
            String amountStr = "₹" + event.amount().toPlainString();
            notificationService.send(
                event.userId(),
                "Transaction Successful ✅",
                amountStr + " — " + event.transactionName() + " (" + event.category() + ")",
                NotificationType.TRANSACTION_COMPLETED,
                Map.of(
                    "transactionId", String.valueOf(event.transactionId()),
                    "screen",        "transactions"
                )
            );
        } catch (Exception e) {
            log.error("Error processing TransactionCompletedEvent: {}", e.getMessage(), e);
        } finally {
            ack.acknowledge();  // Always ACK — notification failure ≠ SAGA failure
        }
    }

    // ── Transaction Failed ────────────────────────────────────
    @KafkaListener(
        topics    = TransactionFailedEvent.TOPIC,
        groupId   = "${spring.kafka.consumer.group-id:notification-service-group}",
        concurrency = "2"
    )
    public void onTransactionFailed(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            TransactionFailedEvent event = objectMapper.readValue(
                record.value(), TransactionFailedEvent.class);

            log.warn("Notification: transaction failed userId={} txId={} reason={}",
                event.userId(), event.transactionId(), event.failureReason());

            String amountStr = "₹" + event.amount().toPlainString();
            notificationService.send(
                event.userId(),
                "Transaction Failed ❌",
                amountStr + " could not be processed. " +
                    (event.failureReason() != null ? event.failureReason() : "Please try again."),
                NotificationType.TRANSACTION_FAILED,
                Map.of(
                    "transactionId", String.valueOf(event.transactionId()),
                    "errorCode",     event.failureCode() != null ? event.failureCode() : "",
                    "screen",        "transactions"
                )
            );
        } catch (Exception e) {
            log.error("Error processing TransactionFailedEvent: {}", e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }

    // ── Wallet Low Balance ────────────────────────────────────
    @KafkaListener(
        topics    = WalletLowBalanceEvent.TOPIC,
        groupId   = "${spring.kafka.consumer.group-id:notification-service-group}",
        concurrency = "1"
    )
    public void onWalletLowBalance(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            WalletLowBalanceEvent event = objectMapper.readValue(
                record.value(), WalletLowBalanceEvent.class);

            log.info("Notification: low balance userId={} balance={}",
                event.userId(), event.currentBalance());

            String balanceStr = "₹" + event.currentBalance().toPlainString();
            String commitStr  = "₹" + event.upcomingCommitments().toPlainString();

            notificationService.send(
                event.userId(),
                "Wallet Running Low ⚠️",
                "Available: " + balanceStr + ". Upcoming commitments: " + commitStr +
                    ". Top up your wallet to avoid payment failures.",
                NotificationType.WALLET_LOW_BALANCE,
                Map.of(
                    "walletId", String.valueOf(event.walletId()),
                    "screen",   "wallet"
                )
            );
        } catch (Exception e) {
            log.error("Error processing WalletLowBalanceEvent: {}", e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }

    // ── EMI Due ───────────────────────────────────────────────
    @KafkaListener(
        topics    = AccountEmiDueEvent.TOPIC,
        groupId   = "${spring.kafka.consumer.group-id:notification-service-group}",
        concurrency = "2"
    )
    public void onEmiDue(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            AccountEmiDueEvent event = objectMapper.readValue(
                record.value(), AccountEmiDueEvent.class);

            log.info("Notification: EMI due userId={} account={} daysUntilDue={}",
                event.userId(), event.accountName(), event.daysUntilDue());

            String amountStr  = "₹" + event.emiAmount().toPlainString();
            String whenStr    = event.daysUntilDue() == 0
                ? "today"
                : "in " + event.daysUntilDue() + " days";

            String title = event.daysUntilDue() == 0
                ? "EMI Due Today 🔔"
                : "EMI Reminder — " + event.daysUntilDue() + " Days";

            notificationService.send(
                event.userId(),
                title,
                event.accountName() + " EMI of " + amountStr + " is due " + whenStr +
                    ". Ensure your wallet has sufficient balance.",
                NotificationType.EMI_DUE,
                Map.of(
                    "accountId",   String.valueOf(event.accountId()),
                    "accountType", event.accountType(),
                    "screen",      "accounts"
                )
            );
        } catch (Exception e) {
            log.error("Error processing AccountEmiDueEvent: {}", e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }

    // ── Portfolio Synced ──────────────────────────────────────
    @KafkaListener(
        topics    = PortfolioSyncedEvent.TOPIC,
        groupId   = "${spring.kafka.consumer.group-id:notification-service-group}",
        concurrency = "1"
    )
    public void onPortfolioSynced(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            PortfolioSyncedEvent event = objectMapper.readValue(
                record.value(), PortfolioSyncedEvent.class);

            // Only notify if portfolio changed meaningfully (>1% change)
            if (event.changePercent() == null ||
                event.changePercent().abs().compareTo(java.math.BigDecimal.ONE) < 0) {
                return;
            }

            boolean isGain = event.changePercent().compareTo(java.math.BigDecimal.ZERO) > 0;
            String direction = isGain ? "📈 +" : "📉 ";
            String pctStr    = event.changePercent().abs().setScale(2,
                java.math.RoundingMode.HALF_UP).toPlainString();

            notificationService.send(
                event.userId(),
                "Portfolio Updated " + (isGain ? "📈" : "📉"),
                direction + pctStr + "% today. " + event.syncedHoldingsCount() +
                    " holdings synced. Tap to view.",
                NotificationType.PORTFOLIO_SYNCED,
                Map.of("screen", "portfolio")
            );
        } catch (Exception e) {
            log.error("Error processing PortfolioSyncedEvent: {}", e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }

    // ── User Registered (Welcome) ─────────────────────────────
    @KafkaListener(
        topics    = UserRegisteredEvent.TOPIC,
        groupId   = "${spring.kafka.consumer.group-id:notification-service-group}",
        concurrency = "1"
    )
    public void onUserRegistered(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            UserRegisteredEvent event = objectMapper.readValue(
                record.value(), UserRegisteredEvent.class);

            log.info("Notification: welcome new user userId={}", event.userId());

            // 1. Send OTP verification email directly
            emailService.sendEmail(
                event.email(),
                "[INVESTRAC] Verify Your Email",
                "Hello " + event.name() + ",\n\n" +
                "Your email verification OTP is: " + event.otp() + "\n\n" +
                "This OTP expires in 10 minutes.\n\n" +
                "Welcome to INVESTRAC!\n" +
                "Team INVESTRAC"
            );

            // 2. Send welcome push notification
            notificationService.send(
                event.userId(),
                "Welcome to INVESTRAC! 🎉",
                "Hi " + event.name() + "! Set up your wallet and start tracking your finances.",
                NotificationType.WELCOME,
                Map.of("screen", "home")
            );
        } catch (Exception e) {
            log.error("Error processing UserRegisteredEvent: {}", e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }

    // ── Password Reset Requested ──────────────────────────────
    @KafkaListener(
        topics    = PasswordResetRequestedEvent.TOPIC,
        groupId   = "${spring.kafka.consumer.group-id:notification-service-group}",
        concurrency = "1"
    )
    public void onPasswordResetRequested(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            PasswordResetRequestedEvent event = objectMapper.readValue(
                record.value(), PasswordResetRequestedEvent.class);

            log.info("Notification: password reset OTP userId={}", event.userId());

            emailService.sendEmail(
                event.email(),
                "[INVESTRAC] Password Reset OTP",
                "Your password reset OTP is: " + event.otp() + "\n\n" +
                "This OTP expires in 10 minutes.\n\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "Team INVESTRAC"
            );
        } catch (Exception e) {
            log.error("Error processing PasswordResetRequestedEvent: {}", e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }
}
