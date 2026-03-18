package com.investrac.notification.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Firebase Cloud Messaging service for push notifications.
 *
 * Requires: Firebase Admin SDK initialized via FirebaseConfig.
 * FCM token stored in notification_preferences.fcm_token.
 *
 * Android: token persists until app reinstall or user clears data.
 * iOS:     token may rotate — frontend must update via PUT /api/notifications/preferences.
 *
 * Error handling:
 *   - UNREGISTERED token → mark token as invalid in DB
 *   - SENDER_ID_MISMATCH → config error, log and alert ops
 *   - Other errors → log warning, continue (notification not critical path)
 *
 * SECURITY: Never log notification body content (may contain financial data).
 */
@Service
@Slf4j
public class FcmPushService {

    /**
     * Send a push notification to a specific device token.
     *
     * @param fcmToken Device FCM registration token
     * @param title    Notification title (shown in system tray)
     * @param body     Notification body text
     * @param data     Extra key-value pairs for deep linking in Angular app
     * @return true if sent successfully, false on any failure
     */
    public boolean sendPush(String fcmToken, String title, String body, Map<String, String> data) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("FCM push skipped — no token registered");
            return false;
        }

        // SECURITY: log title only, never body (financial data)
        log.debug("Sending FCM push: title='{}'", title);

        try {
            Message.Builder messageBuilder = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .setAndroidConfig(AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                        .setChannelId("investrac_alerts")
                        .setIcon("ic_notification")
                        .setColor("#4F46E5")  // INVESTRAC indigo
                        .build())
                    .build())
                .setApnsConfig(ApnsConfig.builder()
                    .setAps(Aps.builder()
                        .setAlert(ApsAlert.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                        .setSound("default")
                        .setBadge(1)
                        .build())
                    .build());

            // Add data payload for deep-link routing
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String messageId = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("FCM push sent successfully: messageId={}", messageId);
            return true;

        } catch (FirebaseMessagingException e) {
            handleFcmError(e, fcmToken);
            return false;
        } catch (Exception e) {
            log.error("FCM push unexpected error: {}", e.getMessage());
            return false;
        }
    }

    private void handleFcmError(FirebaseMessagingException e, String fcmToken) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();
        if (errorCode == MessagingErrorCode.UNREGISTERED) {
            // Token is no longer valid — caller should clear it from DB
            log.warn("FCM token unregistered (app uninstalled or token expired): token_prefix={}",
                fcmToken.length() > 10 ? fcmToken.substring(0, 10) + "..." : "short");
        } else if (errorCode == MessagingErrorCode.SENDER_ID_MISMATCH) {
            log.error("FCM SENDER_ID_MISMATCH — check Firebase project configuration");
        } else {
            log.warn("FCM send failed: errorCode={} message={}", errorCode, e.getMessage());
        }
    }
}
