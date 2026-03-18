-- ═══════════════════════════════════════════════════════
-- INVESTRAC — User Service Schema
-- Migration: V1__create_user_schema.sql
-- Database:  investrac_user
-- ═══════════════════════════════════════════════════════
SET NAMES utf8mb4;

-- ── User Profiles ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_profiles (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    user_id             BIGINT          NOT NULL,
    name                VARCHAR(100)    NOT NULL,
    email               VARCHAR(150)    NOT NULL,
    phone               VARCHAR(15)     NULL,
    date_of_birth       DATE            NULL,
    profile_photo_url   VARCHAR(500)    NULL,

    -- Financial profile
    monthly_income      BIGINT          NULL,
    risk_profile        VARCHAR(20)     NOT NULL DEFAULT 'MODERATE',
    tax_regime          VARCHAR(10)     NOT NULL DEFAULT 'NEW',
    retirement_age      INT             NULL,
    financial_goal      VARCHAR(200)    NULL,

    -- KYC — PAN stored encrypted, only last 4 Aadhaar digits
    pan_encrypted       VARCHAR(500)    NULL,       -- AES-256-GCM encrypted
    aadhaar_last4       VARCHAR(4)      NULL,
    kyc_verified        TINYINT(1)      NOT NULL DEFAULT 0,

    -- Preferences
    language            VARCHAR(10)     NOT NULL DEFAULT 'EN',
    currency            VARCHAR(5)      NOT NULL DEFAULT 'INR',
    theme               VARCHAR(10)     NOT NULL DEFAULT 'light',
    biometric_enabled   TINYINT(1)      NOT NULL DEFAULT 0,

    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_profile_user  (user_id),
    UNIQUE KEY uk_profile_email (email),
    INDEX idx_profile_created   (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ── User Preferences ───────────────────────────────────
CREATE TABLE IF NOT EXISTS user_preferences (
    id                          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id                     BIGINT      NOT NULL,
    show_balance_on_home        TINYINT(1)  NOT NULL DEFAULT 1,
    show_portfolio_on_home      TINYINT(1)  NOT NULL DEFAULT 1,
    ai_insights_enabled         TINYINT(1)  NOT NULL DEFAULT 1,
    ai_language                 VARCHAR(10) NOT NULL DEFAULT 'en',
    auto_lock_minutes           INT         NOT NULL DEFAULT 5,
    show_amounts_in_lakhs       TINYINT(1)  NOT NULL DEFAULT 1,
    wallet_budget_alert_percent INT         NOT NULL DEFAULT 80,
    updated_at                  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_pref_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
