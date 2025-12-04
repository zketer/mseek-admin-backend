package com.lynn.museum.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 博物馆微服务网关启动类
 * 
 * Spring Cloud Gateway 是基于 Spring 5、Spring Boot 2 和 Project Reactor 的响应式网关
 * 它提供了一种简单而有效的方式来对 API 进行路由，以及提供一些强大的过滤器功能
 * 
 * 核心概念：
 * 1. Route（路由）：网关的基本构建块，由ID、目标URI、一组断言和一组过滤器定义
 * 2. Predicate（断言）：匹配HTTP请求中的任何内容，如请求头或参数
 * 3. Filter（过滤器）：可以在发送下游请求之前或之后修改请求和响应
 * 
 * 企业级网关特性：
 * - 服务发现集成：自动发现注册中心的服务
 * - 负载均衡：内置Ribbon/LoadBalancer支持
 * - 限流熔断：集成Sentinel/Hystrix
 * - 安全认证：统一鉴权和授权
 * - 监控告警：集成Actuator和Micrometer
 * 
 * @author lynn
 * @version 1.0
 * @since 2024
 */

/**
 * @author lynn
 * @EnableDiscoveryClient 启用服务发现客户端
 * 该注解会自动注册当前服务到服务注册中心（如Nacos、Eureka等）
 * 同时也能发现其他已注册的服务，实现服务间的自动路由
 */
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayServiceApplication {

    /**
     * 应用程序入口点
     * 
     * Spring Boot应用启动流程：
     * 1. 创建SpringApplication实例
     * 2. 准备Environment环境
     * 3. 创建ApplicationContext应用上下文
     * 4. 刷新上下文，加载所有Bean
     * 5. 启动内嵌Web服务器（Netty for WebFlux）
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 启动Spring Boot应用
        // Gateway基于WebFlux，使用Netty作为默认服务器
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

}
