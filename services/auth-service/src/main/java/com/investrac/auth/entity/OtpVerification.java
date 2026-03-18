package com.investrac.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications", indexes = {
    @Index(name = "idx_otp_email_purpose", columnList = "email,purpose")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;         // BCrypt hash of 6-digit OTP

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OtpPurpose purpose;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(name = "is_used")
    private boolean used = false;

    @Builder.Default
    @Column(name = "attempts")
    private int attempts = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean hasExceededMaxAttempts() {
        return attempts >= 3;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public enum OtpPurpose {
        EMAIL_VERIFICATION,
        PASSWORD_RESET,
        LOGIN_2FA
    }
}
