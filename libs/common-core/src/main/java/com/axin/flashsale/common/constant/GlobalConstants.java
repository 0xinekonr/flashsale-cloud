package com.axin.flashsale.common.constant;

/**
 * 全局通用常量池
 * 使用 interface 定义常量是一种常见的简洁写法（或者使用 public static final class）
 */
public interface GlobalConstants {

    // ================= 系统级常量 =================
    /**
     * 全局成功状态码
     */
    Integer SUCCESS_CODE = 200;

    /**
     * 全局成功默认提示
     */
    String SUCCESS_MESSAGE = "操作成功";

    /**
     * 链路追踪 Header Name (与你 Gateway 中定义的保持绝对一致)
     */
    String TRACE_ID_HEADER = "X-Request-Id";

    // ================= Redis Key 前缀 =================
    interface RedisKey {
        String SECKILL_STOCK_PREFIX = "seckill:stock:";
        String SECKILL_USER_SET_PREFIX = "seckill:users:";
    }

    // ================= RabbitMQ 队列与交换机 =================
    interface MQ {
        String ORDER_PAY_QUEUE = "order.pay.queue";
        String SECKILL_EXCHANGE = "seckill.topic.exchange";
        String SECKILL_ORDER_QUEUE = "seckill.order.queue";
        String SECKILL_ROUTING_KEY = "seckill.order.create";

        // --- 订单事件中心 ---
        String ORDER_EVENT_EXCHANGE = "order.event.exchange";
        String ORDER_DELAY_QUEUE = "order.delay.queue";
        String ORDER_DEAD_LETTER_QUEUE = "order.dead.letter.queue";
        String ORDER_CREATE_ROUTING_KEY = "order.create";
        String ORDER_CANCEL_NOTIFY_ROUTING_KEY = "order.cancelled.notify";
        String INVENTORY_ROLLBACK_QUEUE = "inventory.stock.rollback.queue";
    }

}
