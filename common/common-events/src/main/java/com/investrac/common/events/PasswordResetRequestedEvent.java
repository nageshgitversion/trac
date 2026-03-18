package com.investrac.common.events;

import java.time.Instant;

/**
 * Published by: auth-service when a user requests a password reset
 * Consumed by:  notification-service (send password reset OTP email)
 */
public record PasswordResetRequestedEvent(
    Long userId,
    String email,
    String otp,             // 6-digit OTP for password reset
    Instant requestedAt
) {
    public static final String TOPIC = "investrac.user.password-reset-requested";
}
