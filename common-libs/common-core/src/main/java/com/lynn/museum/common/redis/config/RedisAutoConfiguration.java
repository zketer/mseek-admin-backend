package com.lynn.museum.common.redis.config;

import com.lynn.museum.common.redis.utils.RedisUtils;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 自动配置类
 * 
 * @author lynn
 * @since 2024-11-18
 */
@Slf4j
@Configuration
@ConditionalOnClass(RedisTemplate.class)
public class RedisAutoConfiguration {

    /**
     * Redis模板配置
     */
    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("[common-redis] 初始化通用Redis模板");
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 设置序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        
        // Key序列化
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Value序列化
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();
        
        log.info("[common-redis] 通用RedisTemplate配置完成");
        return template;
    }

    /**
     * Redis工具类配置
     */
    @Bean
    @ConditionalOnMissingBean(RedisUtils.class)
    public RedisUtils redisUtils(RedisTemplate<String, Object> redisTemplate) {
        log.info("[common-redis] 初始化通用Redis工具类");
        return new RedisUtils(redisTemplate);
    }
    
    /**
     * Lettuce 客户端配置优化
     * 解决长时间空闲后连接超时问题
     */
    @Bean
    @ConditionalOnMissingBean(LettuceClientConfigurationBuilderCustomizer.class)
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer() {
        return clientConfigurationBuilder -> {
            // Socket 配置
            SocketOptions socketOptions = SocketOptions.builder()
                    .connectTimeout(Duration.ofSeconds(5))  // 连接超时
                    .keepAlive(true)  // 启用 TCP KeepAlive
                    .build();
            
            // 客户端选项配置
            ClientOptions clientOptions = ClientOptions.builder()
                    .socketOptions(socketOptions)
                    .autoReconnect(true)  // 自动重连
                    .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)  // 断开时拒绝命令
                    .timeoutOptions(TimeoutOptions.enabled(Duration.ofSeconds(10)))  // 命令超时
                    .build();
            
            clientConfigurationBuilder
                    .clientOptions(clientOptions)
                    // 命令超时时间
                    .commandTimeout(Duration.ofSeconds(10));
            
            log.info("[common-redis] Lettuce客户端配置优化完成：启用自动重连、KeepAlive和连接验证");
        };
    }
}
