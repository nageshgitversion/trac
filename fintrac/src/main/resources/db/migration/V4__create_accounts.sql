CREATE TABLE IF NOT EXISTS accounts (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    user_id        BIGINT         NOT NULL,
    type           VARCHAR(10)    NOT NULL,
    name           VARCHAR(200)   NOT NULL,
    principal      DECIMAL(19,4)  NOT NULL,
    interest_rate  DECIMAL(6,4)   NULL,
    tenure_months  INT            NULL,
    start_date     DATE           NULL,
    maturity_date  DATE           NULL,
    status         VARCHAR(10)    NOT NULL DEFAULT 'ACTIVE',
    note           VARCHAR(500)   NULL,
    is_active      TINYINT(1)     NOT NULL DEFAULT 1,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_acc_user (user_id),
    CONSTRAINT fk_acc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
