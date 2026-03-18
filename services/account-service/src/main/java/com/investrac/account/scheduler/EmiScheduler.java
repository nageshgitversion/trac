package com.investrac.account.scheduler;

import com.investrac.account.entity.VirtualAccount;
import com.investrac.account.outbox.AccountOutboxService;
import com.investrac.account.repository.VirtualAccountRepository;
import com.investrac.common.events.AccountEmiDueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * EMI Due Date Scheduler
 *
 * Runs every morning at 08:00 IST.
 * Finds all LOAN and RD accounts whose emi_day matches today.
 * Publishes AccountEmiDueEvent via Outbox → consumed by:
 *   - notification-service (sends reminder push/email to user)
 *   - wallet-service (updates committed amount, optional auto-debit)
 *
 * Cron: "0 0 8 * * *" = every day at 08:00:00
 *
 * Also runs a 3-day-ahead alert so users get advance warning.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EmiScheduler {

    private final VirtualAccountRepository accountRepository;
    private final AccountOutboxService     outboxService;

    /**
     * Daily at 08:00 — fire EMI-due events for today's due date.
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void processEmiDueToday() {
        int today = LocalDate.now().getDayOfMonth();
        log.info("EMI Scheduler running for day={}", today);

        List<VirtualAccount> dueAccounts = accountRepository
            .findEmiDueAccounts(today, LocalDate.now());

        if (dueAccounts.isEmpty()) {
            log.info("No EMI due today (day={})", today);
            return;
        }

        log.info("Found {} accounts with EMI due on day {}", dueAccounts.size(), today);

        dueAccounts.forEach(account -> {
            try {
                publishEmiDueEvent(account, 0);
                log.info("EMI event published for accountId={} userId={} amount={}",
                    account.getId(), account.getUserId(), account.getEmiAmount());
            } catch (Exception e) {
                log.error("Failed to publish EMI event for accountId={}: {}",
                    account.getId(), e.getMessage(), e);
                // Don't rethrow — process remaining accounts even if one fails
            }
        });

        log.info("EMI Scheduler complete — {} events queued", dueAccounts.size());
    }

    /**
     * Daily at 07:00 — fire 3-day-ahead EMI alerts so users have advance warning.
     */
    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void processEmiDueIn3Days() {
        LocalDate threeDaysAhead = LocalDate.now().plusDays(3);
        int dueDay = threeDaysAhead.getDayOfMonth();
        log.info("3-day EMI advance alert for day={}", dueDay);

        List<VirtualAccount> upcomingAccounts = accountRepository
            .findEmiDueAccounts(dueDay, LocalDate.now());

        upcomingAccounts.forEach(account -> {
            try {
                publishEmiDueEvent(account, 3);
            } catch (Exception e) {
                log.error("Failed to publish 3-day EMI alert for accountId={}: {}",
                    account.getId(), e.getMessage(), e);
            }
        });

        log.info("3-day EMI advance alerts queued: {}", upcomingAccounts.size());
    }

    /**
     * Daily at 09:00 — maturity alerts for FD/RD maturing in next 7 days.
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void processMaturityAlerts() {
        LocalDate today   = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);

        List<VirtualAccount> maturingAccounts = accountRepository
            .findMaturingBetween(today, in7Days);

        if (maturingAccounts.isEmpty()) return;

        log.info("Found {} accounts maturing within 7 days", maturingAccounts.size());

        maturingAccounts.forEach(account -> {
            long daysLeft = java.time.temporal.ChronoUnit.DAYS
                .between(today, account.getMaturityDate());

            log.info("Maturity alert: accountId={} userId={} matures in {} days",
                account.getId(), account.getUserId(), daysLeft);

            // Publish EMI event reused as maturity alert
            // notification-service will differentiate based on account type
            publishEmiDueEvent(account, (int) daysLeft);
        });
    }

    // ── Helper ──
    private void publishEmiDueEvent(VirtualAccount account, int daysUntilDue) {
        outboxService.publish(
            AccountEmiDueEvent.TOPIC,
            new AccountEmiDueEvent(
                account.getId(),
                account.getUserId(),
                account.getName(),
                account.getType().name().toLowerCase(),
                account.getEmiAmount() != null ? account.getEmiAmount() : BigDecimal.ZERO,
                daysUntilDue == 0 ? LocalDate.now()
                    : LocalDate.now().plusDays(daysUntilDue),
                daysUntilDue,
                BigDecimal.ZERO   // wallet balance filled by notification-service via wallet API
            )
        );
    }
}
