package com.investrac.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true),
    @Index(name = "idx_users_phone", columnList = "phone", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(length = 15, unique = true)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Builder.Default
    @Column(name = "is_active")
    private boolean active = true;

    @Builder.Default
    @Column(name = "is_email_verified")
    private boolean emailVerified = false;

    @Builder.Default
    @Column(name = "login_attempts")
    private int loginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "risk_profile", length = 20)
    private RiskProfile riskProfile = RiskProfile.MODERATE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "tax_regime", length = 10)
    private TaxRegime taxRegime = TaxRegime.NEW;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── Business methods ──

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public void incrementLoginAttempts() {
        this.loginAttempts++;
    }

    public void lockFor30Minutes() {
        this.lockedUntil = LocalDateTime.now().plusMinutes(30);
    }

    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.lockedUntil = null;
    }

    public enum RiskProfile { CONSERVATIVE, MODERATE, AGGRESSIVE }
    public enum TaxRegime { OLD, NEW }
}
