package com.axin.flashsale.order.listener;

import com.axin.flashsale.common.dto.SeckillMessage;
import com.axin.flashsale.order.client.InventoryClient;
import com.axin.flashsale.order.config.RabbitConfig;
import com.axin.flashsale.order.entity.Order;
import com.axin.flashsale.order.mapper.OrderMapper;
import org.apache.seata.spring.annotation.GlobalTransactional;
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

    /**
     * 开启分布式事务
     * name: 给这个事务起个名字
     * rollbackFor: 遇到任何异常都回滚
     */
    @GlobalTransactional(name = "seckill-create-order", rollbackFor = Exception.class)
    @RabbitListener(queues = RabbitConfig.SECKILL_ORDER_QUEUE)
    public void createSeckillOrder(SeckillMessage message) {
        System.out.println("全局事务 XID: " + org.apache.seata.core.context.RootContext.getXID());

        // 1. 组装并插入订单
        Order order = new Order();
        order.setUserId(message.getUserId());
        order.setProductId(message.getProductId());
        order.setCount(1);  // 秒杀通常限购1件
        order.setTotalAmount(message.getSeckillPrice());
        order.setStatus("NEW");
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insert(order);
        System.out.println("1. 订单已插入本地数据库，ID:  " + order.getId());

        // 2. 远程调用库存服务 (Feign 会自动把 XID 放在 Header 里传过去)
        System.out.println("2. 准备调用 Inventory Service 扣减库存...");
        Boolean lockResult = inventoryClient.lockStock(message.getProductId(), 1);
        System.out.println("3. Inventory Service 返回结果：" + lockResult);

        // 3. 模拟一个恐怖的宕机异常！（用来测试分布式回滚）
//        System.out.println("4. 模拟系统突然崩溃！");
//        int a = 1 / 0; // 这里会抛出 ArithmeticException

        System.out.println("秒杀订单落库成功 + 库存同步完成： " + order.getId());

    }
}
