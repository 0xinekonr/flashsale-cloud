package com.axin.flashsale.seckill.util;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * 用户上下文工具类
 *
 * 从 Spring Security 的 SecurityContext 中提取当前认证用户的 ID。
 * auth-service 在签发 JWT 时注入了 "user_id" 自定义声明（见 auth-service SecurityConfig），
 * 网关验证 token 后将 Authentication 转发给下游服务，
 * 这里直接从 JwtAuthenticationToken 中读取 user_id，无需额外的 Feign 调用。
 */
public class UserContext {

    private UserContext() {
    }

    /**
     * 获取当前登录用户的 ID
     *
     * @return 用户 ID（Long 类型）
     * @throws IllegalStateException 如果用户未认证或 token 中缺少 user_id 声明
     */
    public static Long getUserId() {
        JwtAuthenticationToken auth = (JwtAuthenticationToken)
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();
        if (auth == null || auth.getToken() == null) {
            throw new IllegalStateException("用户未认证");
        }
        // JWT 中的数字可能被解析为 Integer 或 Long，使用 Number 统一处理
        Number userId = auth.getToken().getClaim("user_id");
        if (userId == null) {
            throw new IllegalStateException("JWT token 中缺少 user_id 声明");
        }
        return userId.longValue();
    }
}
