package com.lynn.museum.common.web.config;

import com.lynn.museum.common.web.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Web异常处理自动配置类
 * 自动导入全局异常处理器
 *
 * @author lynn
 * @since 2024-01-01
 */
@Configuration
@Import(GlobalExceptionHandler.class)
public class WebExceptionAutoConfiguration {
    // 配置类可以为空，主要用于导入GlobalExceptionHandler
}
