package com.lynn.museum.file.service;

/**
 * MinIO存储服务接口
 * 继承通用存储服务接口，提供MinIO特定的存储功能
 * 
 * @author Lynn
 * @since 2024-01-01
 */
public interface MinioStorageService extends StorageService {
    
    /**
     * 获取MinIO客户端状态
     * 
     * @return 客户端是否可用
     */
    boolean isAvailable();
    
    /**
     * 设置存储桶策略
     * 
     * @param bucketName 存储桶名称
     * @param policy 策略JSON字符串
     * @throws Exception 设置异常
     */
    void setBucketPolicy(String bucketName, String policy) throws Exception;
}
