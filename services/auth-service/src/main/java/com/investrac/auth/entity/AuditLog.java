package com.investrac.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Immutable audit trail for all auth events.
 * Every login attempt, password change, logout is recorded.
 * Required for fintech compliance.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_created", columnList = "created_at")
})
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;            // nullable — failed login may not have userId

    @Column(name = "email", length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuditAction action;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Builder.Default
    @Column(nullable = false)
    private boolean success = true;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "extra_info", length = 1000)
    private String extraInfo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum AuditAction {
        REGISTER, LOGIN, LOGIN_FAILED, LOGOUT, LOGOUT_ALL,
        REFRESH_TOKEN, PASSWORD_RESET, PASSWORD_CHANGED,
        EMAIL_VERIFIED, ACCOUNT_LOCKED, ACCOUNT_UNLOCKED
    }
}
