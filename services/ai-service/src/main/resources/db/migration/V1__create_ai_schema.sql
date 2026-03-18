-- INVESTRAC AI Service Schema
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS ai_insights (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    user_id         BIGINT      NOT NULL,
    content         TEXT        NOT NULL,
    type            VARCHAR(20) NOT NULL,
    priority        TINYINT     NOT NULL DEFAULT 3,
    is_read         TINYINT(1)  NOT NULL DEFAULT 0,
    generated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_insight_user     (user_id),
    INDEX idx_insight_unread   (user_id, is_read),
    INDEX idx_insight_generated(generated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_chat_history (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    role        VARCHAR(15) NOT NULL,
    content     TEXT        NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_chat_user   (user_id),
    INDEX idx_chat_created(user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
