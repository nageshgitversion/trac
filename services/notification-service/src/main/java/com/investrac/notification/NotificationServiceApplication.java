package com.investrac.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * INVESTRAC Notification Service
 *
 * Kafka consumers for all notification-relevant events:
 *   - Transaction completed/failed → push + email
 *   - Wallet low balance → push + email
 *   - EMI due reminders → push + email
 *   - Portfolio synced → push (>1% change only)
 *   - User registered → OTP email + welcome push
 *
 * Delivery:
 *   - Push: Firebase Cloud Messaging (FCM)
 *   - Email: AWS SES via JavaMailSender
 *
 * @EnableAsync — all push/email sends are non-blocking
 * @EnableScheduling — weekly cleanup of old notifications
 *
 * Port: 8088
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
