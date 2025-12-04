package com.lynn.museum.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;

/**
 * 网关Knife4j自动发现配置
 * 
 * 功能说明：
 * 1. 启用Knife4j自动发现模式
 * 2. 通过网关统一访问所有服务的API文档
 * 3. 自动发现注册到网关的微服务并聚合其OpenAPI文档
 * 
 * 重要说明：
 * - 使用discover自动发现模式时，不需要手动配置GroupedOpenApi
 * - 所有微服务必须配置springdoc.api-docs.enabled=true
 * - 微服务必须引入knife4j-openapi3-jakarta-spring-boot-starter依赖
 * 
 * 访问方式：
 * - Knife4j文档：http://localhost:8000/doc.html
 * - Swagger UI：http://localhost:8000/swagger-ui.html
 * - OpenAPI JSON：http://localhost:8000/v3/api-docs
 * 
 * @author lynn
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Configuration
@EnableKnife4j
public class SwaggerConfig {


    
    /**
     * 使用Knife4j自动发现模式时，不需要手动配置GroupedOpenApi
     * 
     * 自动发现模式的工作原理：
     * 1. Knife4j会自动扫描注册到网关的微服务
     * 2. 通过服务名称自动构建文档聚合路径
     * 3. 从各微服务的/v3/api-docs端点获取OpenAPI文档
     * 4. 在网关层面进行文档聚合展示
     * 
     * 配置要求：
     * - application.yml中配置knife4j.gateway.strategy=discover
     * - application.yml中配置knife4j.gateway.discover.enabled=true
     * - 各微服务必须启用springdoc.api-docs.enabled=true
     */
    
    // 注释掉手动配置，改为使用自动发现模式
    // 如果需要自定义分组，可以在各微服务中配置springdoc.group-configs
}