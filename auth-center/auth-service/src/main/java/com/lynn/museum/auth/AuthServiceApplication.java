package com.lynn.museum.auth;

import com.lynn.museum.common.feign.GlobalFeignConfiguration;
import com.lynn.museum.common.web.annotation.EnableGlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证服务启动类
 * 
 * @author lynn
 */
@SpringBootApplication(scanBasePackages = {
    "com.lynn.museum.auth",
    "com.lynn.museum.common"
})
@EnableDiscoveryClient
@EnableFeignClients(
    basePackages = {"com.lynn.museum.api"},
    defaultConfiguration = GlobalFeignConfiguration.class
)
@EnableGlobalExceptionHandler
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}