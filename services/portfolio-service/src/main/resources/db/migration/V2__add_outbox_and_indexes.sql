-- ═══════════════════════════════════════════════════════════════
-- INVESTRAC — Portfolio Service
-- Migration: V2__add_outbox_and_indexes.sql
-- Adds outbox_events table and additional performance indexes
-- ═══════════════════════════════════════════════════════════════

SET NAMES utf8mb4;

-- ── Outbox Events (Outbox Pattern for Kafka reliability) ──
CREATE TABLE IF NOT EXISTS outbox_events (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    topic           VARCHAR(100)    NOT NULL,
    payload         TEXT            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    retry_count     INT             NOT NULL DEFAULT 0,
    last_error      VARCHAR(500)    NULL,
    published_at    TIMESTAMP       NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_pf_outbox_status  (status),
    INDEX idx_pf_outbox_created (created_at),
    INDEX idx_pf_outbox_retry   (retry_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ── Additional indexes for common query patterns ──

-- Portfolio value history query: userId + date range
ALTER TABLE holdings
    ADD INDEX IF NOT EXISTS idx_holding_user_active (user_id, is_active);

-- Price history range queries: holdingId + date range
ALTER TABLE price_history
    ADD INDEX IF NOT EXISTS idx_ph_holding_date_range (holding_id, recorded_at DESC);

-- Updatable holdings scheduler query
ALTER TABLE holdings
    ADD INDEX IF NOT EXISTS idx_holding_updatable_active (is_updatable, is_active);
