package com.axin.flashsale.order.listener;

import com.axin.flashsale.common.dto.SeckillMessage;
import com.axin.flashsale.order.client.InventoryClient;
import com.axin.flashsale.order.config.RabbitConfig;
import com.axin.flashsale.order.entity.Order;
import com.axin.flashsale.order.mapper.OrderMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SeckillOrderListener {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private InventoryClient inventoryClient;

    @RabbitListener(queues = RabbitConfig.SECKILL_ORDER_QUEUE)
    public void createSeckillOrder(SeckillMessage message) {
//        System.out.println("开始异步创建秒杀订单：" + message);

        // 1. 幂等性/防重检查 (DB层面兜底)
        // 实际上 Redis 已经挡了一层，这里可以再查库确认（可选），或者依赖数据库唯一索引
        // 假设我们在订单表加了唯一索引 uk_user_product，这里 insert 会报错忽略即可

        // 2. 组装订单对象
        Order order = new Order();
        order.setUserId(message.getUserId());
        order.setProductId(message.getProductId());
        order.setCount(1);  // 秒杀通常限购1件
        order.setTotalAmount(message.getSeckillPrice());
        order.setStatus("NEW");
        order.setCreateTime(LocalDateTime.now());

        // 3. 写入数据库（创建订单）
        try {
            orderMapper.insert(order);

            // === 4. 同步扣减 MySQL 库存（异步削峰后执行）===
            // 这里调用 M3 写好的 Feign 接口：UPDATE inventory SET ...
            // 注意：因为已经是“已支付”或“秒杀成功”状态，这里实际上是在同步数据
            inventoryClient.lockStock(message.getProductId(), 1);
//            System.out.println("秒杀订单落库成功 + 库存同步完成： " + order.getId());
        } catch (Exception e) {
            // 可能是重复消费导致的唯一索引冲突
            System.err.println("订单落库失败" + e.getMessage());
        }
    }
}
