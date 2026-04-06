CREATE TABLE IF NOT EXISTS transactions (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    user_id    BIGINT         NOT NULL,
    wallet_id  BIGINT         NOT NULL,
    type       VARCHAR(10)    NOT NULL,
    category   VARCHAR(20)    NOT NULL,
    name       VARCHAR(200)   NOT NULL,
    amount     DECIMAL(19,4)  NOT NULL,
    tx_date    DATE           NOT NULL,
    note       VARCHAR(500)   NULL,
    status     VARCHAR(15)    NOT NULL DEFAULT 'COMPLETED',
    is_active  TINYINT(1)     NOT NULL DEFAULT 1,
    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_tx_user (user_id),
    INDEX idx_tx_date (tx_date),
    CONSTRAINT fk_tx_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
