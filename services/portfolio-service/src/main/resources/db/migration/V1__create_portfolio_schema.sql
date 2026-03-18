SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS holdings (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    type            VARCHAR(30)     NOT NULL,
    name            VARCHAR(200)    NOT NULL,
    symbol          VARCHAR(50)     NULL,
    units           DECIMAL(14,4)   NOT NULL DEFAULT 0.0000,
    buy_price       DECIMAL(14,4)   NOT NULL DEFAULT 0.0000,
    current_price   DECIMAL(14,4)   NOT NULL DEFAULT 0.0000,
    invested        DECIMAL(15,2)   NOT NULL,
    current_value   DECIMAL(15,2)   NOT NULL,
    xirr            DECIMAL(6,2)    NOT NULL DEFAULT 0.00,
    sip_amount      DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    is_updatable    TINYINT(1)      NOT NULL DEFAULT 0,
    last_synced     TIMESTAMP       NULL,
    note            VARCHAR(500)    NULL,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_holding_user (user_id),
    INDEX idx_holding_type (type),
    INDEX idx_holding_symbol (symbol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS price_history (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    holding_id      BIGINT          NOT NULL,
    price           DECIMAL(14,4)   NOT NULL,
    recorded_at     DATE            NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_price_holding_date (holding_id, recorded_at),
    CONSTRAINT fk_ph_holding FOREIGN KEY (holding_id) REFERENCES holdings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
