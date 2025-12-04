package com.lynn.museum.info;

import com.lynn.museum.common.feign.GlobalFeignConfiguration;
import com.lynn.museum.common.web.annotation.EnableGlobalExceptionHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 博物馆信息服务启动类
 *
 * @author lynn
 * @since 2024-01-01
 */
@SpringBootApplication(scanBasePackages = {
    "com.lynn.museum.info",
    "com.lynn.museum.common"
})
@EnableDiscoveryClient
@EnableFeignClients(
    basePackages = {"com.lynn.museum.api"},
    defaultConfiguration = GlobalFeignConfiguration.class
)
@EnableTransactionManagement
@EnableGlobalExceptionHandler
@MapperScan("com.lynn.museum.info.mapper")
public class MuseumServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MuseumServiceApplication.class, args);
    }
}
