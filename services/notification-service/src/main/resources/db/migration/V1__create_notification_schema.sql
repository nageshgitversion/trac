-- ═══════════════════════════════════════════════════════
-- INVESTRAC — Notification Service Schema
-- Migration: V1__create_notification_schema.sql
-- Database:  investrac_notification
-- ═══════════════════════════════════════════════════════
SET NAMES utf8mb4;

-- ── Notifications ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS notifications (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    title           VARCHAR(150)    NOT NULL,
    body            TEXT            NOT NULL,
    type            VARCHAR(30)     NOT NULL,
    channel         VARCHAR(20)     NOT NULL DEFAULT 'PUSH',
    data_json       VARCHAR(500)    NULL,
    is_read         TINYINT(1)      NOT NULL DEFAULT 0,
    is_sent         TINYINT(1)      NOT NULL DEFAULT 0,
    sent_at         TIMESTAMP       NULL,
    failure_reason  VARCHAR(500)    NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_notif_user       (user_id),
    INDEX idx_notif_unread     (user_id, is_read),
    INDEX idx_notif_type       (type),
    INDEX idx_notif_created    (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ── Notification Preferences ───────────────────────────
CREATE TABLE IF NOT EXISTS notification_preferences (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    user_id             BIGINT          NOT NULL,
    fcm_token           VARCHAR(512)    NULL,
    email               VARCHAR(150)    NULL,
    push_enabled        TINYINT(1)      NOT NULL DEFAULT 1,
    email_enabled       TINYINT(1)      NOT NULL DEFAULT 1,
    transaction_notif   TINYINT(1)      NOT NULL DEFAULT 1,
    emi_notif           TINYINT(1)      NOT NULL DEFAULT 1,
    wallet_alert_notif  TINYINT(1)      NOT NULL DEFAULT 1,
    portfolio_notif     TINYINT(1)      NOT NULL DEFAULT 0,
    ai_insight_notif    TINYINT(1)      NOT NULL DEFAULT 1,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_pref_user (user_id),
    INDEX idx_pref_fcm (fcm_token(32))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
