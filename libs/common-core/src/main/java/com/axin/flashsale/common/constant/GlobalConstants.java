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
        String SECKILL_EXCHANGE = "seckill.topic.exchange";
        String SECKILL_ORDER_QUEUE = "seckill.order.queue";
        String SECKILL_ROUTING_KEY = "seckill.order.create";
    }

}
