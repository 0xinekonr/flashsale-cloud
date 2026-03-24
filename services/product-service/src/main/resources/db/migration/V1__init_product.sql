-- Product SPU 表
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '商品描述',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Product SKU 表
CREATE TABLE IF NOT EXISTS product_sku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT 'SPU ID',
    sku_name VARCHAR(100) NOT NULL COMMENT 'SKU名称',
    price DECIMAL(12,2) NOT NULL COMMENT '价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    attributes JSON DEFAULT NULL COMMENT 'SKU属性(颜色/尺寸等)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id),
    INDEX idx_status (status),
    CONSTRAINT uk_product_sku UNIQUE (product_id, sku_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
