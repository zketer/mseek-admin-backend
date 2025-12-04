package com.lynn.museum.info.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 高德地图API配置
 *
 * @author lynn
 * @since 2025-09-26
 */
@Data
@Component
@ConfigurationProperties(prefix = "museum.info.amap")
public class AmapConfig {

    /**
     * 高德地图Web服务API Key
     */
    private String key;

    /**
     * 逆地理编码API地址
     */
    private String geocodeUrl;

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 5000;

    /**
     * 查询半径（米）
     */
    private Integer radius = 1000;
}
