SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS virtual_accounts (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    type            VARCHAR(20)     NOT NULL,   -- savings | fd | rd | loan
    name            VARCHAR(100)    NOT NULL,
    balance         DECIMAL(15,2)   NOT NULL,
    interest_rate   DECIMAL(5,2)    NOT NULL DEFAULT 0.00,
    bank_name       VARCHAR(100)    NULL,
    start_date      DATE            NULL,
    maturity_date   DATE            NULL,
    maturity_amount DECIMAL(15,2)   NULL,
    emi_amount      DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    emi_day         TINYINT         NULL,
    linked_acc_id   BIGINT          NULL,
    goal_amount     DECIMAL(15,2)   NULL,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_acc_user (user_id),
    INDEX idx_acc_type (type),
    INDEX idx_acc_emi_day (emi_day)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS outbox_events (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    topic       VARCHAR(100) NOT NULL,
    payload     TEXT        NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT         NOT NULL DEFAULT 0,
    last_error  VARCHAR(500) NULL,
    published_at TIMESTAMP  NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_outbox_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
