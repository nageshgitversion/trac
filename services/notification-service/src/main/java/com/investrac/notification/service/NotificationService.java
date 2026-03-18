package com.investrac.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investrac.notification.entity.Notification;
import com.investrac.notification.entity.NotificationPreference;
import com.investrac.notification.repository.NotificationPreferenceRepository;
import com.investrac.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

/**
 * Core notification orchestration service.
 *
 * Responsibilities:
 *  1. Save notification to DB (always — audit trail)
 *  2. Check user preferences before sending push/email
 *  3. Dispatch to FcmPushService or EmailService
 *  4. Update sent status and failure reasons
 *
 * All delivery is @Async — Kafka consumer threads are never blocked.
 * Notification delivery failure does NOT affect the business event
 * (transaction already committed, etc.).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository           notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final FcmPushService                   fcmPushService;
    private final EmailService                     emailService;
    private final ObjectMapper                     objectMapper;

    // ── SEND (orchestrator) ──────────────────────────────
    @Async
    @Transactional
    public void send(Long userId,
                     String title,
                     String body,
                     Notification.NotificationType type,
                     Map<String, String> data) {

        // 1. Always save to DB first (audit trail regardless of delivery)
        String dataJson = null;
        if (data != null && !data.isEmpty()) {
            try {
                dataJson = objectMapper.writeValueAsString(data);
            } catch (Exception e) {
                log.warn("Could not serialize notification data: {}", e.getMessage());
            }
        }

        Notification notif = Notification.builder()
            .userId(userId)
            .title(title)
            .body(body)
            .type(type)
            .channel(Notification.NotificationChannel.PUSH)
            .dataJson(dataJson)
            .build();
        notif = notificationRepository.save(notif);

        // 2. Get user preferences (create with defaults if not exists)
        NotificationPreference prefs = getOrCreatePreferences(userId);

        // 3. Check if this type is enabled
        if (!isTypeEnabled(prefs, type)) {
            log.debug("Notification suppressed by user preference: userId={} type={}", userId, type);
            return;
        }

        // 4. Push notification
        if (prefs.isPushEnabled() && prefs.getFcmToken() != null) {
            boolean pushed = fcmPushService.sendPush(prefs.getFcmToken(), title, body, data);
            if (!pushed && prefs.getFcmToken() != null) {
                // Token might be stale — clear it
                prefs.setFcmToken(null);
                preferenceRepository.save(prefs);
            }
            notif.setSent(pushed);
            notif.setSentAt(pushed ? Instant.now() : null);
            if (!pushed) notif.setFailureReason("FCM delivery failed");
        }

        // 5. Email notification (for high-priority types)
        if (prefs.isEmailEnabled() && prefs.getEmail() != null && isEmailWorthy(type)) {
            sendEmailForType(type, prefs.getEmail(), title, body);
        }

        notificationRepository.save(notif);
        log.debug("Notification processed: userId={} type={} sent={}", userId, type, notif.isSent());
    }

    // ── READ / MARK READ ──────────────────────────────────
    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(Long userId, int page, int size) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(
            userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public boolean markRead(Long notifId, Long userId) {
        return notificationRepository.markRead(notifId, userId) > 0;
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
    }

    // ── PREFERENCES ───────────────────────────────────────
    @Transactional(readOnly = true)
    public NotificationPreference getPreferences(Long userId) {
        return getOrCreatePreferences(userId);
    }

    @Transactional
    public NotificationPreference updatePreferences(Long userId,
                                                     Boolean pushEnabled,
                                                     Boolean emailEnabled,
                                                     String fcmToken,
                                                     String email,
                                                     Boolean transactionNotif,
                                                     Boolean emiNotif,
                                                     Boolean walletAlertNotif,
                                                     Boolean portfolioNotif,
                                                     Boolean aiInsightNotif) {
        NotificationPreference prefs = getOrCreatePreferences(userId);

        if (pushEnabled       != null) prefs.setPushEnabled(pushEnabled);
        if (emailEnabled      != null) prefs.setEmailEnabled(emailEnabled);
        if (fcmToken          != null) prefs.setFcmToken(fcmToken);
        if (email             != null) prefs.setEmail(email);
        if (transactionNotif  != null) prefs.setTransactionNotif(transactionNotif);
        if (emiNotif          != null) prefs.setEmiNotif(emiNotif);
        if (walletAlertNotif  != null) prefs.setWalletAlertNotif(walletAlertNotif);
        if (portfolioNotif    != null) prefs.setPortfolioNotif(portfolioNotif);
        if (aiInsightNotif    != null) prefs.setAiInsightNotif(aiInsightNotif);

        return preferenceRepository.save(prefs);
    }

    // ── CLEANUP ───────────────────────────────────────────
    /** Runs weekly Sunday midnight — delete read notifications older than 90 days */
    @Scheduled(cron = "0 0 0 * * SUN", zone = "Asia/Kolkata")
    @Transactional
    public void cleanupOldNotifications() {
        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);
        notificationRepository.deleteOldRead(cutoff);
        log.info("Old read notifications cleaned up (older than 90 days)");
    }

    // ── PRIVATE HELPERS ───────────────────────────────────
    private NotificationPreference getOrCreatePreferences(Long userId) {
        return preferenceRepository.findByUserId(userId)
            .orElseGet(() -> {
                NotificationPreference pref = NotificationPreference.builder()
                    .userId(userId)
                    .build();
                return preferenceRepository.save(pref);
            });
    }

    private boolean isTypeEnabled(NotificationPreference prefs, Notification.NotificationType type) {
        return switch (type) {
            case TRANSACTION_COMPLETED, TRANSACTION_FAILED -> prefs.isTransactionNotif();
            case EMI_DUE                                   -> prefs.isEmiNotif();
            case WALLET_LOW_BALANCE                        -> prefs.isWalletAlertNotif();
            case PORTFOLIO_SYNCED                          -> prefs.isPortfolioNotif();
            case AI_INSIGHT                                -> prefs.isAiInsightNotif();
            case WELCOME, PASSWORD_RESET, GENERAL          -> true;
        };
    }

    private boolean isEmailWorthy(Notification.NotificationType type) {
        // Only send emails for high-priority events
        return switch (type) {
            case TRANSACTION_FAILED, EMI_DUE, WALLET_LOW_BALANCE, WELCOME, PASSWORD_RESET -> true;
            default -> false;
        };
    }

    private void sendEmailForType(Notification.NotificationType type,
                                   String email, String title, String body) {
        try {
            emailService.sendEmail(email, "[INVESTRAC] " + title, body);
        } catch (Exception e) {
            log.warn("Email send failed for type={}: {}", type, e.getMessage());
        }
    }
}
