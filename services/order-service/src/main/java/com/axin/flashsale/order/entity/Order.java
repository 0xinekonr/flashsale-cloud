package com.axin.flashsale.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    // 后面我们要换雪花算法，这里先用 AUTO 或者 INPUT
    // 为了简单，我们暂且假设数据库ID自增，或者手动生成ID
    @TableId(type = IdType.ASSIGN_ID)   // Mybatis Plus 内置雪花算法
    private Long id;
    private Long userId;
    private Long productId;
    private Integer count;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
