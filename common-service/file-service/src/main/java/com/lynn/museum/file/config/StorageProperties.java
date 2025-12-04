package com.lynn.museum.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 存储配置属性
 * 支持MinIO和OSS两种存储模式
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * 存储类型：minio 或 oss
     */
    private String type = "minio";

    /**
     * 默认存储桶名称
     */
    private String bucketName = "museum";

    /**
     * 文件URL过期时间（秒），默认7天
     */
    private long urlExpiry = 7 * 24 * 60 * 60;

    /**
     * 最大文件大小（字节），默认100MB
     */
    private long maxFileSize = 100L * 1024 * 1024;

    /**
     * 允许的文件类型
     */
    private String[] allowedContentTypes = {
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "video/mp4", "video/webm", "video/avi",
        "application/pdf", "text/plain"
    };

    /**
     * 文件路径前缀配置
     */
    private PathConfig path = new PathConfig();

    /**
     * MinIO配置
     */
    private MinioConfig minio = new MinioConfig();

    /**
     * OSS配置
     */
    private OssConfig oss = new OssConfig();

    @Data
    public static class PathConfig {
        /**
         * 头像文件路径前缀
         */
        private String avatar = "avatar";

        /**
         * 博物馆图片路径前缀
         */
        private String museum = "museum";

        /**
         * 公告图片路径前缀
         */
        private String announcement = "announcement";

        /**
         * 横幅图片路径前缀
         */
        private String banner = "banner";

        /**
         * 推荐内容图片路径前缀
         */
        private String recommendation = "recommendation";

        /**
         * 用户打卡照片路径前缀
         */
        private String checkin = "checkin";

        /**
         * 临时文件路径前缀
         */
        private String temp = "temp";

        /**
         * 获取展览路径前缀
         */
        private String exhibition = "exhibition";

        /**
         * 获取app版本
         */
        private String appVersion = "appVersion";
    }

    @Data
    public static class MinioConfig {
        /**
         * MinIO服务地址
         */
        private String endpoint = "http://localhost:9000";

        /**
         * 访问密钥
         */
        private String accessKey = "minioadmin";

        /**
         * 秘密密钥
         */
        private String secretKey = "minioadmin";
    }

    @Data
    public static class OssConfig {
        /**
         * 阿里云OSS端点
         */
        private String endpoint = "https://oss-cn-beijing.aliyuncs.com";

        /**
         * 访问密钥ID
         */
        private String accessKeyId;

        /**
         * 访问密钥Secret
         */
        private String accessKeySecret;

//        /**
//         * OSS区域
//         */
//        private String region = "cn-beijing";
    }
}
