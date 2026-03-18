SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS wallets (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    month       VARCHAR(7)      NOT NULL,
    income      DECIMAL(15,2)   NOT NULL,
    topup       DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    balance     DECIMAL(15,2)   NOT NULL,
    committed   DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    is_active   TINYINT(1)      NOT NULL DEFAULT 1,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wallet_user_month (user_id, month),
    INDEX idx_wallet_user_active (user_id, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS wallet_envelopes (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    wallet_id       BIGINT          NOT NULL,
    envelope_key    VARCHAR(30)     NOT NULL,
    category_name   VARCHAR(50)     NOT NULL,
    icon            VARCHAR(10)     NULL,
    budget          DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    spent           DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    PRIMARY KEY (id),
    UNIQUE KEY uk_envelope_wallet_key (wallet_id, envelope_key),
    CONSTRAINT fk_envelope_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS saga_processed_events (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    saga_id         VARCHAR(36)     NOT NULL,
    event_type      VARCHAR(50)     NULL,
    processed_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_saga_id (saga_id),
    INDEX idx_saga_processed_at (processed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
    INDEX idx_outbox_status (status),
    INDEX idx_outbox_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
