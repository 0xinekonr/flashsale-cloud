-- 幂等消费记录表
CREATE TABLE IF NOT EXISTS idempotent_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id VARCHAR(100) NOT NULL UNIQUE COMMENT '消息唯一ID',
    consumer_group VARCHAR(50) NOT NULL COMMENT '消费者组',
    status VARCHAR(20) NOT NULL DEFAULT 'CONSUMED' COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_message_id (message_id),
    INDEX idx_consumer_group (consumer_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;