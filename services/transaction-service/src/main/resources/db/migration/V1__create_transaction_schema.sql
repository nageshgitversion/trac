SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS transactions (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    wallet_id       BIGINT          NULL,
    account_id      BIGINT          NULL,
    type            VARCHAR(20)     NOT NULL,
    category        VARCHAR(50)     NOT NULL,
    name            VARCHAR(200)    NOT NULL,
    amount          DECIMAL(12,2)   NOT NULL,
    envelope_key    VARCHAR(30)     NULL,
    tx_date         DATE            NOT NULL,
    note            VARCHAR(500)    NULL,
    source          VARCHAR(20)     NOT NULL DEFAULT 'manual',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    saga_id         VARCHAR(36)     NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_tx_user_date (user_id, tx_date),
    INDEX idx_tx_user_type (user_id, type),
    INDEX idx_tx_saga (saga_id),
    INDEX idx_tx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS saga_processed_events (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    saga_id         VARCHAR(36)     NOT NULL,
    event_type      VARCHAR(50)     NULL,
    processed_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_saga_id (saga_id)
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
    INDEX idx_outbox_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
