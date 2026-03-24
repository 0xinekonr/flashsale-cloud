-- 为订单表添加 sku_id 字段
ALTER TABLE orders ADD COLUMN sku_id BIGINT DEFAULT NULL COMMENT 'SKU ID' AFTER product_id;

-- 添加索引
CREATE INDEX idx_sku_id ON orders(sku_id);
