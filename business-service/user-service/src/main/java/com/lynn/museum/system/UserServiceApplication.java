package com.lynn.museum.system;

import com.lynn.museum.common.feign.GlobalFeignConfiguration;
import com.lynn.museum.common.web.annotation.EnableGlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 博物馆用户管理服务启动类
 * 
 * @author lynn
 * @since 2024-01-01
 */
@SpringBootApplication(scanBasePackages = {
    "com.lynn.museum.system",
    "com.lynn.museum.common"
})
@EnableDiscoveryClient
@EnableFeignClients(
    basePackages = {"com.lynn.museum.api"},
    defaultConfiguration = GlobalFeignConfiguration.class
)
@EnableGlobalExceptionHandler
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}