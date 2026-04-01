-- Outbox 消息表（用于可靠消息投递）
CREATE TABLE IF NOT EXISTS outbox_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    aggregate_type VARCHAR(64) NOT NULL COMMENT '聚合类型(PAYMENT)',
    aggregate_id BIGINT NOT NULL COMMENT '聚合ID(支付流水ID)',
    event_type VARCHAR(64) NOT NULL COMMENT '事件类型(PAYMENT_SUCCESS)',
    payload TEXT NOT NULL COMMENT '消息载荷(JSON)',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING/SENT/FAILED)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    sent_time DATETIME DEFAULT NULL COMMENT '发送时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_status_retry (status, retry_count),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Outbox消息表';