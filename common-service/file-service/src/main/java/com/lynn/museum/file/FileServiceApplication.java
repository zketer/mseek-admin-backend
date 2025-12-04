package com.lynn.museum.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 博物馆文件服务启动类
 * 
 * 注意：file-service 不使用 Feign 客户端，因此排除 feign 包的扫描
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(
    basePackages = {
        "com.lynn.museum.file",
        "com.lynn.museum.common"
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.lynn\\.museum\\.common\\.feign\\..*"
    )
)
public class FileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }
}
