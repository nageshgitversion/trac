package com.investrac.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Email notification service.
 *
 * Backend: JavaMailSender configured for AWS SES SMTP.
 * All sends are @Async — never blocks the Kafka consumer thread.
 *
 * AWS SES setup (production):
 *   SMTP host: email-smtp.ap-south-1.amazonaws.com (Mumbai region)
 *   Port: 587 (STARTTLS)
 *   Credentials: IAM user with SES SendEmail permission
 *
 * Alternative: SendGrid — replace spring.mail config with SendGrid SMTP.
 *
 * SECURITY: Never log email content (financial details in body).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:no-reply@investrac.in}")
    private String fromAddress;

    @Value("${spring.mail.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send a simple plain-text email asynchronously.
     */
    @Async
    public void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.debug("Email disabled in config — skipping to={}", to);
            return;
        }

        // SECURITY: log recipient and subject only, never body
        log.debug("Sending email: to={} subject='{}'", to, subject);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent: to={} subject='{}'", to, subject);
        } catch (MailException e) {
            log.error("Email send failed: to={} subject='{}' error={}", to, subject, e.getMessage());
        }
    }

    /**
     * Send an HTML email for richer formatting (EMI reminders, welcome emails).
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        if (!emailEnabled) return;

        log.debug("Sending HTML email: to={} subject='{}'", to, subject);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("HTML email sent: to={}", to);
        } catch (Exception e) {
            log.error("HTML email failed: to={} error={}", to, e.getMessage());
        }
    }

    // ── Template helpers ──

    public String buildTransactionEmail(String userName, String txName, String amount, boolean success) {
        String status = success ? "successful" : "failed";
        return String.format("""
            Dear %s,

            Your transaction %s (%s) was %s.

            Log in to INVESTRAC to view the details.

            Best regards,
            INVESTRAC Team

            ─────────────────────────────────────
            This is an automated message. Please do not reply.
            To unsubscribe from transaction alerts, update your preferences in the app.
            """, userName, txName, amount, status);
    }

    public String buildEmiReminderEmail(String userName, String accountName,
                                         String amount, int daysUntilDue) {
        String when = daysUntilDue == 0 ? "today" : "in " + daysUntilDue + " days";
        return String.format("""
            Dear %s,

            Reminder: Your EMI for %s (%s) is due %s.

            Please ensure sufficient balance in your linked account.

            Best regards,
            INVESTRAC Team
            """, userName, accountName, amount, when);
    }

    public String buildWelcomeEmail(String userName) {
        return String.format("""
            Welcome to INVESTRAC, %s!

            Your intelligent investment tracker is ready.

            Next steps:
            1. Set up your monthly wallet
            2. Add your portfolio holdings
            3. Connect your virtual accounts (FD, RD, Loans)
            4. Chat with your AI financial advisor

            Start now: https://app.investrac.in

            Best regards,
            The INVESTRAC Team
            """, userName);
    }
}
