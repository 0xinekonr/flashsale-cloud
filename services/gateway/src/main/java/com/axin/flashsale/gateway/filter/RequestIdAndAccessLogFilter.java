package com.axin.flashsale.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
public class RequestIdAndAccessLogFilter implements GlobalFilter, Ordered {
    public static final Logger log = LoggerFactory.getLogger(RequestIdAndAccessLogFilter.class);

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startNanos = System.nanoTime();

        // 1. 获取或生成 Request-Id
        ServerHttpRequest request = exchange.getRequest();
        final String requestId = Optional.ofNullable(request.getHeaders().getFirst(REQUEST_ID_HEADER))
                .filter(s -> !s.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString().replace("-", ""));

        // 2. 透传到下游（Request Header）和 回传给前端（Response Header）
        // 注意：mutate() 操作是不可变的，必须重新赋值给 chain.filter
        ServerHttpRequest mutatedRequest = request.mutate().header(REQUEST_ID_HEADER, requestId).build();
        exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);

        // 日志所需的基本信息
        String method = request.getMethod().name();
        String path = request.getURI().getRawPath();

        // 3. 执行过滤器链
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signalType -> {
                    // 4. 请求结束时打印日志（无论成功失败）
                    long costMs = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
                    var status = exchange.getResponse().getStatusCode();

                    // routeId 需要从 exchange attribute 里取，某些场景可能为 null（比如没匹配到路由）
                    Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                    String routeId = (route != null) ? route.getId() : "-";

                    log.info("ACCESS requestId={} {} {} status={} costMs={} route={}",
                            requestId, method, path, status, costMs, routeId);
                });
    }

    @Override
    public int getOrder() {
        return -1000;
    }
}
