package com.axin.flashsale.product.controller;

import com.axin.flashsale.common.response.PageResult;
import com.axin.flashsale.common.response.Result;
import com.axin.flashsale.product.entity.Product;
import com.axin.flashsale.product.entity.ProductSku;
import com.axin.flashsale.product.service.ProductService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public Result<Product> create(@RequestBody Product product) {
        Product created = productService.create(product);
        return Result.success(created);
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        Product product = productService.getDetailById(id);
        if (product == null) {
            return Result.fail(404, "商品不存在");
        }
        return Result.success(product);
    }

    @GetMapping
    public PageResult<Product> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> productPage = productService.list(page, size);
        return PageResult.success(
                productPage.getRecords(),
                productPage.getTotal(),
                (int) productPage.getCurrent(),
                (int) productPage.getSize()
        );
    }

    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        Product updated = productService.update(id, product);
        if (updated == null) {
            return Result.fail(404, "商品不存在");
        }
        return Result.success(updated);
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = productService.updateStatus(id, status);
        return Result.success(success);
    }

    @GetMapping("/{productId}/skus")
    public Result<List<ProductSku>> listSkus(@PathVariable Long productId) {
        List<ProductSku> skus = productService.listSkus(productId);
        return Result.success(skus);
    }

    @GetMapping("/{productId}/skus/{skuId}")
    public Result<ProductSku> getSku(@PathVariable Long productId, @PathVariable Long skuId) {
        ProductSku sku = productService.getSku(productId, skuId);
        if (sku == null) {
            return Result.fail(404, "SKU不存在");
        }
        return Result.success(sku);
    }

    @PostMapping("/{productId}/skus")
    public Result<ProductSku> createSku(@PathVariable Long productId, @RequestBody ProductSku sku) {
        ProductSku created = productService.createSku(productId, sku);
        return Result.success(created);
    }
}
