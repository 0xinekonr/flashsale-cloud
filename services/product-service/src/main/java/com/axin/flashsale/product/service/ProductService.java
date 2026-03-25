package com.axin.flashsale.product.service;

import com.axin.flashsale.product.entity.Product;
import com.axin.flashsale.product.entity.ProductSku;
import com.axin.flashsale.product.mapper.ProductMapper;
import com.axin.flashsale.product.mapper.ProductSkuMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductSkuMapper skuMapper;

    public Product getById(Long id) {
        return productMapper.selectById(id);
    }

    public Product getDetailById(Long id) {
        Product product = productMapper.selectById(id);
        if (product != null) {
            List<ProductSku> skus = skuMapper.selectList(
                    new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, id));
            // 可以在 Product 实体中添加 skus 字段，或返回 DTO
        }
        return product;
    }

    public Page<Product> list(int page, int size) {
        return productMapper.selectPage(new Page<>(page, size), null);
    }

    @Transactional
    public Product create(Product product) {
        product.setStatus(1);
        productMapper.insert(product);
        log.info("商品创建成功, productId={}", product.getId());
        return product;
    }

    @Transactional
    public Product update(Long id, Product product) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            return null;
        }
        product.setId(id);
        productMapper.updateById(product);
        return product;
    }

    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return false;
        }
        product.setStatus(status);
        productMapper.updateById(product);
        log.info("商品状态更新, productId={}, status={}", id, status);
        return true;
    }

    // SKU 相关方法
    public List<ProductSku> listSkus(Long productId) {
        return skuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId)
                        .eq(ProductSku::getStatus, 1));
    }

    public ProductSku getSku(Long productId, Long skuId) {
        ProductSku sku = skuMapper.selectById(skuId);
        if (sku != null && sku.getProductId().equals(productId)) {
            return sku;
        }
        return null;
    }

    @Transactional
    public ProductSku createSku(Long productId, ProductSku sku) {
        sku.setProductId(productId);
        sku.setStatus(1);
        skuMapper.insert(sku);
        log.info("SKU创建成功, skuId={}, productId={}", sku.getId(), productId);
        return sku;
    }
}
