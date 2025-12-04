package com.lynn.museum.common.feign;

import com.lynn.museum.common.auth.InternalServiceAuthUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * 全局FeignClient配置类
 * 
 * 功能：
 * 1. 为所有FeignClient请求自动添加内部服务认证Headers
 *    - X-Internal-Call: true
 *    - X-Service-ID: 当前服务名 
 *    - X-Service-Token: 生成的服务Token
 * 
 * 2. 统一处理Feign调用的错误响应（通过GlobalFeignErrorDecoder）
 *    - 自动解析远程服务的业务错误
 *    - 将HTTP错误转换为BizException
 *    - 避免每个Service方法手动处理FeignException
 * 
 * 特点：
 * - 全局生效，所有使用@FeignClient的接口都会自动应用这些配置
 * - 零配置，服务只需添加@EnableFeignClients即可
 * - 统一管理，避免重复配置
 * - 支持开关控制，可通过配置禁用
 * - 条件加载：只有当Feign客户端存在时才启用（避免非Feign服务启动失败）
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Configuration
@ConditionalOnClass({RequestInterceptor.class, ErrorDecoder.class})
public class GlobalFeignConfiguration {
    
    @Value("${spring.application.name:unknown-service}")
    private String currentServiceName;
    
    @Value("${museum.feign.internal-auth.enabled:true}")
    private boolean internalAuthEnabled;
    
    /**
     * 全局FeignClient内部认证拦截器
     * 
     * 使用@Order确保优先级，避免与其他拦截器冲突
     */
    @Bean
    @Order(0)
    public RequestInterceptor globalInternalServiceFeignInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                log.info("【认证检查】GlobalFeign拦截器被调用: URL={}, Service={}", template.url(), currentServiceName);
                
                if (!internalAuthEnabled) {
                    log.warn("【认证检查】内部服务认证已禁用，跳过Header添加: URL={}", template.url());
                    return;
                }
                
                try {
                    // 为所有FeignClient请求添加内部调用Headers
                    Map<String, String> headers = InternalServiceAuthUtil
                        .createFeignInternalHeaders(currentServiceName);
                    
                    log.info("【认证检查】准备添加内部认证Headers: Service={}, Headers={}", 
                        currentServiceName, headers.keySet());
                    
                    headers.forEach((key, value) -> {
                        template.header(key, value);
                        log.debug("【认证检查】添加Header: {}={}", key, key.toLowerCase().contains("token") ? "***" + value.substring(Math.max(0, value.length()-4)) : value);
                    });
                    
                    log.info("【认证检查】内部认证Headers添加完成: Service={}, URL={}", 
                        currentServiceName, template.url());
                        
                } catch (Exception e) {
                    log.error("【认证检查】添加内部认证Headers失败: Service={}, URL={}, Error={}", 
                        currentServiceName, template.url(), e.getMessage(), e);
                    // 不抛出异常，避免影响正常的服务调用
                }
            }
        };
    }
    
    /**
     * 全局FeignClient错误解码器
     * 
     * 自动将远程服务的HTTP错误响应转换为业务异常
     * 所有@FeignClient接口都会自动使用此解码器
     */
    @Bean
    public ErrorDecoder feignErrorDecoder() {
        log.info("【Feign配置】注册全局错误解码器: GlobalFeignErrorDecoder");
        return new GlobalFeignErrorDecoder();
    }
}
