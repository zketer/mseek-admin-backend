package com.lynn.museum.file.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 存储配置类
 * 根据配置动态创建不同的存储客户端
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StorageConfig {

    private final StorageProperties storageProperties;

    /**
     * 创建MinIO客户端（仅在使用MinIO存储时）
     */
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "minio", matchIfMissing = true)
    public MinioClient minioClient() {
        StorageProperties.MinioConfig minioConfig = storageProperties.getMinio();
        
        log.info("初始化MinIO客户端，服务地址: {}", minioConfig.getEndpoint());
        
        return MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();
    }
}
