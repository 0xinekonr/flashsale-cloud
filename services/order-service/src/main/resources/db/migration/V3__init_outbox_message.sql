-- Outbox 消息表（可靠消息投递）
CREATE TABLE IF NOT EXISTS outbox_message (
    id BIGINT PRIMARY KEY COMMENT '消息ID',
    aggregate_type VARCHAR(50) NOT NULL COMMENT '聚合类型(ORDER/PAYMENT)',
    aggregate_id BIGINT NOT NULL COMMENT '聚合ID',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型(ORDER_CREATED/ORDER_CANCELLED)',
    payload TEXT NOT NULL COMMENT '消息载荷(JSON)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态:PENDING/SENT/FAILED',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    sent_time DATETIME DEFAULT NULL COMMENT '发送时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_aggregate (aggregate_type, aggregate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;