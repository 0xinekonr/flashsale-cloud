CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY COMMENT 'Snowflake ID',
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    count INT NOT NULL DEFAULT 1,
    total_amount DECIMAL(12,2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-待支付 1-已支付 2-已发货 3-已取消',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_status (user_id, status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
