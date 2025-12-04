package com.lynn.museum.common.web.annotation;

import com.lynn.museum.common.web.config.WebExceptionAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用全局异常处理器的注解
 * 在需要使用全局异常处理的服务主类上添加此注解
 *
 * @author lynn
 * @since 2024-01-01
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(WebExceptionAutoConfiguration.class)
public @interface EnableGlobalExceptionHandler {
}
