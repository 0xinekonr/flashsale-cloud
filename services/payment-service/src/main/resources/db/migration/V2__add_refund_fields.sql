-- 添加退款相关字段
ALTER TABLE payment ADD COLUMN refund_amount DECIMAL(12,2) DEFAULT NULL COMMENT '已退款金额';
ALTER TABLE payment ADD COLUMN refund_time DATETIME DEFAULT NULL COMMENT '退款时间';
ALTER TABLE payment ADD INDEX idx_status (status);
ALTER TABLE payment ADD INDEX idx_order_id (order_id);
