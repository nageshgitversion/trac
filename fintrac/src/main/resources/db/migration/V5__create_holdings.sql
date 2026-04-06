CREATE TABLE IF NOT EXISTS holdings (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    user_id       BIGINT         NOT NULL,
    type          VARCHAR(10)    NOT NULL,
    name          VARCHAR(200)   NOT NULL,
    symbol        VARCHAR(20)    NULL,
    units         DECIMAL(19,6)  NULL,
    buy_price     DECIMAL(19,4)  NULL,
    current_price DECIMAL(19,4)  NULL,
    note          VARCHAR(500)   NULL,
    is_active     TINYINT(1)     NOT NULL DEFAULT 1,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_holding_user (user_id),
    CONSTRAINT fk_holding_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
