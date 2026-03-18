package com.investrac.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Extended user profile managed by user-service.
 * Core identity (email/password) lives in auth-service.
 * This service owns: personal details, financial profile, KYC, preferences.
 *
 * userId = same ID as auth-service users.id (no FK across services — by design).
 */
@Entity
@Table(name = "user_profiles",
    indexes = @Index(name = "idx_profile_user", columnList = "user_id", unique = true))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 15)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    // ── Financial Profile ──
    @Column(name = "monthly_income", precision = 15)
    private Long monthlyIncome;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "risk_profile", length = 20)
    private RiskProfile riskProfile = RiskProfile.MODERATE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "tax_regime", length = 10)
    private TaxRegime taxRegime = TaxRegime.NEW;

    @Column(name = "retirement_age")
    private Integer retirementAge;

    @Column(name = "financial_goal", length = 200)
    private String financialGoal;

    // ── KYC — encrypted at rest with AES-256 ──
    // SECURITY: These fields are encrypted via @Convert before storing.
    // Never log these values. Never return in API responses.
    @Column(name = "pan_encrypted", length = 500)
    private String panEncrypted;       // PAN: ABCDE1234F

    @Column(name = "aadhaar_last4", length = 4)
    private String aadhaarLast4;       // Last 4 digits only — never store full Aadhaar

    @Builder.Default
    @Column(name = "kyc_verified")
    private boolean kycVerified = false;

    // ── App Preferences ──
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "language", length = 10)
    private Language language = Language.EN;

    @Builder.Default
    @Column(name = "currency", length = 5)
    private String currency = "INR";

    @Builder.Default
    @Column(name = "theme", length = 10)
    private String theme = "light";

    @Builder.Default
    @Column(name = "biometric_enabled")
    private boolean biometricEnabled = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── Computed ──
    public Integer getAge() {
        if (dateOfBirth == null) return null;
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    public boolean hasPan() {
        return panEncrypted != null && !panEncrypted.isBlank();
    }

    // ── Enums ──
    public enum RiskProfile { CONSERVATIVE, MODERATE, AGGRESSIVE }
    public enum TaxRegime   { OLD, NEW }
    public enum Language    { EN, HI, TE, TA, KN, MR }
}
