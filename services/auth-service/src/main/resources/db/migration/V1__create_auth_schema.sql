-- ═══════════════════════════════════════════════════════════════
-- INVESTRAC — Auth Service Database Schema
-- Migration: V1__create_auth_schema.sql
-- Database:  investrac_auth
-- ═══════════════════════════════════════════════════════════════

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ── Users ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    name                VARCHAR(100)    NOT NULL,
    email               VARCHAR(150)    NOT NULL,
    phone               VARCHAR(15)     NULL,
    password_hash       VARCHAR(255)    NOT NULL,
    is_active           TINYINT(1)      NOT NULL DEFAULT 1,
    is_email_verified   TINYINT(1)      NOT NULL DEFAULT 0,
    login_attempts      INT             NOT NULL DEFAULT 0,
    locked_until        DATETIME        NULL,
    last_login_at       TIMESTAMP       NULL,
    last_login_ip       VARCHAR(45)     NULL,
    risk_profile        VARCHAR(20)     NOT NULL DEFAULT 'MODERATE',
    tax_regime          VARCHAR(10)     NOT NULL DEFAULT 'NEW',
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_phone (phone),
    INDEX idx_users_active (is_active),
    INDEX idx_users_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ── Refresh Tokens ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    token           VARCHAR(512)    NOT NULL,
    device_info     VARCHAR(500)    NULL,
    ip_address      VARCHAR(45)     NULL,
    expires_at      TIMESTAMP       NOT NULL,
    is_revoked      TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_token (token),
    INDEX idx_refresh_user (user_id),
    INDEX idx_refresh_expires (expires_at),
    INDEX idx_refresh_revoked (is_revoked),
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ── OTP Verifications ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS otp_verifications (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    email       VARCHAR(150)    NOT NULL,
    otp_hash    VARCHAR(255)    NOT NULL,
    purpose     VARCHAR(20)     NOT NULL,
    expires_at  DATETIME        NOT NULL,
    is_used     TINYINT(1)      NOT NULL DEFAULT 0,
    attempts    INT             NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_otp_email_purpose (email, purpose),
    INDEX idx_otp_expires (expires_at),
    INDEX idx_otp_used (is_used)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ── Audit Logs ─────────────────────────────────────────────────
-- Immutable — no UPDATE allowed via application code
CREATE TABLE IF NOT EXISTS audit_logs (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NULL,
    email           VARCHAR(150)    NULL,
    action          VARCHAR(30)     NOT NULL,
    ip_address      VARCHAR(45)     NULL,
    user_agent      VARCHAR(500)    NULL,
    success         TINYINT(1)      NOT NULL DEFAULT 1,
    failure_reason  VARCHAR(500)    NULL,
    extra_info      VARCHAR(1000)   NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_created (created_at),
    INDEX idx_audit_success (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ── Outbox Events ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS outbox_events (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    topic           VARCHAR(100)    NOT NULL,
    payload         TEXT            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    published_at    TIMESTAMP       NULL,
    retry_count     INT             NOT NULL DEFAULT 0,
    last_error      VARCHAR(500)    NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_outbox_status (status),
    INDEX idx_outbox_created (created_at),
    INDEX idx_outbox_retry (retry_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
