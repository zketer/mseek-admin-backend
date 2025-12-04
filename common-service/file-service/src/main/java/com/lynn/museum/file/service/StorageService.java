package com.lynn.museum.file.service;

import java.io.InputStream;
import java.util.List;

/**
 * 存储服务接口
 * 提供统一的文件存储接口，支持不同的存储实现（MinIO、OSS等）
 * 
 * @author Lynn
 * @since 2024-01-01
 */
public interface StorageService {

    /**
     * 上传文件（使用默认存储桶）
     * 
     * @param objectName 文件名称
     * @param inputStream 文件流
     * @param contentType 文件类型
     * @param fileSize 文件大小
     * @throws Exception 上传异常
     */
    void putObject(String objectName, InputStream inputStream, String contentType, long fileSize) throws Exception;

    /**
     * 上传文件（指定存储桶）
     * 
     * @param bucketName 存储桶名称
     * @param objectName 文件名称
     * @param inputStream 文件流
     * @param contentType 文件类型
     * @param fileSize 文件大小
     * @throws Exception 上传异常
     */
    void putObject(String bucketName, String objectName, InputStream inputStream, 
                   String contentType, long fileSize) throws Exception;

    /**
     * 下载文件（使用默认存储桶）
     * 
     * @param objectName 文件名称
     * @return 文件流
     * @throws Exception 下载异常
     */
    InputStream getObject(String objectName) throws Exception;

    /**
     * 下载文件（指定存储桶）
     * 
     * @param bucketName 存储桶名称
     * @param objectName 文件名称
     * @return 文件流
     * @throws Exception 下载异常
     */
    InputStream getObject(String bucketName, String objectName) throws Exception;

    /**
     * 删除文件（使用默认存储桶）
     * 
     * @param objectName 文件名称
     * @throws Exception 删除异常
     */
    void removeObject(String objectName) throws Exception;

    /**
     * 删除文件（指定存储桶）
     * 
     * @param bucketName 存储桶名称
     * @param objectName 文件名称
     * @throws Exception 删除异常
     */
    void removeObject(String bucketName, String objectName) throws Exception;

    /**
     * 获取文件访问URL（使用默认存储桶）
     * 
     * @param objectName 文件名称
     * @param expiry 过期时间（秒）
     * @return 访问URL
     * @throws Exception URL生成异常
     */
    String getPresignedObjectUrl(String objectName, int expiry) throws Exception;

    /**
     * 获取文件访问URL（指定存储桶）
     * 
     * @param bucketName 存储桶名称
     * @param objectName 文件名称
     * @param expiry 过期时间（秒）
     * @return 访问URL
     * @throws Exception URL生成异常
     */
    String getPresignedObjectUrl(String bucketName, String objectName, int expiry) throws Exception;

    /**
     * 获取文件强制下载URL（使用默认存储桶）
     * 返回的URL会强制浏览器下载而不是预览
     * 
     * @param objectName 文件名称
     * @param originalFileName 原始文件名（用于下载时的文件名）
     * @param expiry 过期时间（秒）
     * @return 下载URL
     * @throws Exception URL生成异常
     */
    String getPresignedDownloadUrl(String objectName, String originalFileName, int expiry) throws Exception;

    /**
     * 获取文件强制下载URL（指定存储桶）
     * 返回的URL会强制浏览器下载而不是预览
     * 
     * @param bucketName 存储桶名称
     * @param objectName 文件名称
     * @param originalFileName 原始文件名（用于下载时的文件名）
     * @param expiry 过期时间（秒）
     * @return 下载URL
     * @throws Exception URL生成异常
     */
    String getPresignedDownloadUrl(String bucketName, String objectName, String originalFileName, int expiry) throws Exception;

    /**
     * 检查文件是否存在（使用默认存储桶）
     * 
     * @param objectName 文件名称
     * @return 是否存在
     * @throws Exception 检查异常
     */
    boolean doesObjectExist(String objectName) throws Exception;

    /**
     * 检查文件是否存在（指定存储桶）
     * 
     * @param bucketName 存储桶名称
     * @param objectName 文件名称
     * @return 是否存在
     * @throws Exception 检查异常
     */
    boolean doesObjectExist(String bucketName, String objectName) throws Exception;

    /**
     * 确保默认存储桶存在，如果不存在则创建
     * 
     * @throws Exception 创建异常
     */
    void ensureBucketExists() throws Exception;

    /**
     * 确保指定存储桶存在，如果不存在则创建
     * 
     * @param bucketName 存储桶名称
     * @throws Exception 创建异常
     */
    void ensureBucketExists(String bucketName) throws Exception;

    /**
     * 批量上传文件（使用默认存储桶）
     * 
     * @param files 文件信息列表，包含文件名、流、类型等
     * @return 上传结果列表
     * @throws Exception 上传异常
     */
    List<String> putObjects(List<FileUploadInfo> files) throws Exception;

    /**
     * 批量上传文件（指定存储桶）
     * 
     * @param bucketName 存储桶名称
     * @param files 文件信息列表
     * @return 上传结果列表
     * @throws Exception 上传异常
     */
    List<String> putObjects(String bucketName, List<FileUploadInfo> files) throws Exception;

    /**
     * 批量删除文件（使用默认存储桶）
     * 
     * @param objectNames 文件名列表
     * @throws Exception 删除异常
     */
    void removeObjects(List<String> objectNames) throws Exception;

    /**
     * 批量删除文件（指定存储桶）
     * 
     * @param bucketName 存储桶名称
     * @param objectNames 文件名列表
     * @throws Exception 删除异常
     */
    void removeObjects(String bucketName, List<String> objectNames) throws Exception;

    /**
     * 批量检查文件是否存在（使用默认存储桶）
     * 
     * @param objectNames 文件名列表
     * @return 存在性结果列表，与输入列表对应
     * @throws Exception 检查异常
     */
    List<Boolean> doesObjectsExist(List<String> objectNames) throws Exception;

    /**
     * 批量检查文件是否存在（指定存储桶）
     * 
     * @param bucketName 存储桶名称
     * @param objectNames 文件名列表
     * @return 存在性结果列表，与输入列表对应
     * @throws Exception 检查异常
     */
    List<Boolean> doesObjectsExist(String bucketName, List<String> objectNames) throws Exception;

    /**
     * 获取存储类型
     * 
     * @return 存储类型
     */
    String getStorageType();
    
    /**
     * 检查存储服务是否可用
     * 
     * @return 存储服务是否可用
     */
    boolean isAvailable();
    
    /**
     * 复制文件（使用默认存储桶）
     * 
     * @param sourceObjectName 源文件名
     * @param targetObjectName 目标文件名
     * @throws Exception 复制异常
     */
    void copyObject(String sourceObjectName, String targetObjectName) throws Exception;
    
    /**
     * 复制文件（指定存储桶）
     * 
     * @param sourceBucketName 源存储桶名称
     * @param sourceObjectName 源文件名
     * @param targetBucketName 目标存储桶名称
     * @param targetObjectName 目标文件名
     * @throws Exception 复制异常
     */
    void copyObject(String sourceBucketName, String sourceObjectName, 
                   String targetBucketName, String targetObjectName) throws Exception;

    /**
     * 文件上传信息
     */
    class FileUploadInfo {
        private String objectName;
        private java.io.InputStream inputStream;
        private String contentType;
        private long fileSize;

        public FileUploadInfo(String objectName, java.io.InputStream inputStream, String contentType, long fileSize) {
            this.objectName = objectName;
            this.inputStream = inputStream;
            this.contentType = contentType;
            this.fileSize = fileSize;
        }

        // Getters
        public String getObjectName() { return objectName; }
        public java.io.InputStream getInputStream() { return inputStream; }
        public String getContentType() { return contentType; }
        public long getFileSize() { return fileSize; }

        // Setters
        public void setObjectName(String objectName) { this.objectName = objectName; }
        public void setInputStream(java.io.InputStream inputStream) { this.inputStream = inputStream; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    }
}
