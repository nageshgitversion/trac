package com.investrac.common.events;

import java.time.Instant;

/**
 * Published by: auth-service after successful registration
 * Consumed by:  notification-service (send welcome email + OTP)
 */
public record UserRegisteredEvent(
    Long userId,
    String email,
    String name,
    String otp,                 // 6-digit OTP for email verification
    Instant registeredAt
) {
    public static final String TOPIC = "investrac.user.registered";
}
