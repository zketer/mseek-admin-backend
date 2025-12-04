package com.lynn.museum.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient配置
 * 用于Gateway调用其他微服务（避免Feign的循环依赖问题）
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Configuration
public class WebClientConfig {
    
    /**
     * WebClient Builder
     * 用于创建WebClient实例
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
