package com.axin.flashsale.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置 — OAuth2 Resource Server
 *
 * seckill-service 作为资源服务器，验证网关转发的 JWT token。
 * 网关已经做了 JWT 验证，这里做二次校验以确保安全性（纵深防御）。
 * 同时从 token 中提取 user_id 等信息供业务使用。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 活动管理接口开放（后续可加管理员鉴权）
                .requestMatchers("/seckill/activities/**").permitAll()
                // 健康检查端点开放
                .requestMatchers("/actuator/**").permitAll()
                // 秒杀相关接口需要认证
                .requestMatchers("/seckill/**").authenticated()
                .anyRequest().authenticated()
            )
            // 配置为 OAuth2 资源服务器，使用 JWT 进行令牌验证
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        // 秒杀接口不需要 CSRF（纯 API 服务）
        http.csrf(csrf -> csrf.disable());

        return http.build();
    }
}
