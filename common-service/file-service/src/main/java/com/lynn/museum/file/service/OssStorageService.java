package com.lynn.museum.file.service;

/**
 * 阿里云OSS存储服务接口
 * 继承通用存储服务接口，提供OSS特定的存储功能
 * 
 * @author Lynn
 * @since 2024-01-01
 */
public interface OssStorageService extends StorageService {
    
    /**
     * 获取OSS客户端状态
     * 
     * @return 客户端是否可用
     */
    boolean isAvailable();
    
    /**
     * 设置存储桶的跨域规则
     * 
     * @param bucketName 存储桶名称
     * @throws Exception 设置异常
     */
    void setBucketCors(String bucketName) throws Exception;
    
    /**
     * 获取存储桶的区域信息
     * 
     * @param bucketName 存储桶名称
     * @return 区域信息
     * @throws Exception 获取异常
     */
    String getBucketLocation(String bucketName) throws Exception;
}
