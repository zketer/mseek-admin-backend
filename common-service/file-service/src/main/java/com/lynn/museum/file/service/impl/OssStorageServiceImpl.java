package com.lynn.museum.file.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.ResponseHeaderOverrides;
import com.lynn.museum.file.config.StorageProperties;
import com.lynn.museum.file.service.OssStorageService;
import com.lynn.museum.file.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 阿里云OSS存储服务实现
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "oss")
public class OssStorageServiceImpl implements StorageService {

    private final StorageProperties storageProperties;
    private OSS ossClient;

    public OssStorageServiceImpl(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @PostConstruct
    public void init() {
        log.info("初始化阿里云OSS客户端...");
        
        StorageProperties.OssConfig ossConfig = storageProperties.getOss();
        
        // 创建OSS客户端
        this.ossClient = new OSSClientBuilder()
            .build(ossConfig.getEndpoint(), ossConfig.getAccessKeyId(), ossConfig.getAccessKeySecret());
        
        log.info("OSS客户端初始化成功，端点: {}", ossConfig.getEndpoint());
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS客户端已关闭");
        }
    }

    @Override
    public void putObject(String objectName, InputStream inputStream, String contentType, long fileSize) throws Exception {
        putObject(storageProperties.getBucketName(), objectName, inputStream, contentType, fileSize);
    }

    @Override
    public void putObject(String bucketName, String objectName, InputStream inputStream, 
                         String contentType, long fileSize) throws Exception {
        log.debug("OSS上传文件: bucket={}, object={}, size={}", bucketName, objectName, fileSize);
        
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
        
        // 设置对象元数据
        if (contentType != null || fileSize > 0) {
            ObjectMetadata metadata = new ObjectMetadata();
            
            // 设置Content-Type
            if (contentType != null) {
                metadata.setContentType(contentType);
                log.debug("设置Content-Type: {}", contentType);
            }
            
            // 设置Content-Length
            if (fileSize > 0) {
                metadata.setContentLength(fileSize);
                log.debug("设置Content-Length: {}", fileSize);
            }
            
            // 将元数据设置到请求中
            putObjectRequest.setMetadata(metadata);
        }
        
        ossClient.putObject(putObjectRequest);
        
        log.info("OSS文件上传成功: {}", objectName);
    }

    @Override
    public InputStream getObject(String objectName) throws Exception {
        return getObject(storageProperties.getBucketName(), objectName);
    }

    @Override
    public InputStream getObject(String bucketName, String objectName) throws Exception {
        log.debug("OSS下载文件: bucket={}, object={}", bucketName, objectName);
        
        return ossClient.getObject(bucketName, objectName).getObjectContent();
    }

    @Override
    public void removeObject(String bucketName, String objectName) throws Exception {
        log.debug("OSS删除文件: bucket={}, object={}", bucketName, objectName);
        
        ossClient.deleteObject(bucketName, objectName);
        
        log.info("OSS文件删除成功: {}", objectName);
    }

    @Override
    public String getPresignedObjectUrl(String bucketName, String objectName, int expiry) throws Exception {
        log.debug("OSS获取预签名URL: bucket={}, object={}, expiry={}", bucketName, objectName, expiry);
        
        // 设置过期时间
        Date expiration = new Date(System.currentTimeMillis() + expiry * 1000L);
        
        // 生成预签名URL
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName);
        request.setExpiration(expiration);
        
        URL url = ossClient.generatePresignedUrl(request);
        return url.toString();
    }

    @Override
    public String getPresignedDownloadUrl(String objectName, String originalFileName, int expiry) throws Exception {
        return getPresignedDownloadUrl(storageProperties.getBucketName(), objectName, originalFileName, expiry);
    }

    @Override
    public String getPresignedDownloadUrl(String bucketName, String objectName, String originalFileName, int expiry) throws Exception {
        log.debug("OSS获取强制下载URL: bucket={}, object={}, originalFileName={}, expiry={}", 
                  bucketName, objectName, originalFileName, expiry);
        
        // 设置过期时间
        Date expiration = new Date(System.currentTimeMillis() + expiry * 1000L);
        
        // 生成预签名URL并设置响应头
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName);
        request.setExpiration(expiration);
        
        // 对文件名进行URL编码，支持中文和特殊字符
        String encodedFileName;
        encodedFileName = java.net.URLEncoder.encode(originalFileName, StandardCharsets.UTF_8)
                // 将+号替换为%20，符合RFC标准
                .replaceAll("\\+", "%20");

        // 设置响应头，强制下载
        ResponseHeaderOverrides responseHeaders = new ResponseHeaderOverrides();
        responseHeaders.setContentDisposition("attachment; filename=\"" + encodedFileName + "\"");
        request.setResponseHeaders(responseHeaders);
        
        URL url = ossClient.generatePresignedUrl(request);
        return url.toString();
    }

    @Override
    public boolean doesObjectExist(String bucketName, String objectName) throws Exception {
        log.debug("OSS检查文件存在: bucket={}, object={}", bucketName, objectName);
        
        try {
            return ossClient.doesObjectExist(bucketName, objectName);
        } catch (Exception e) {
            log.warn("OSS检查文件存在时发生异常: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void ensureBucketExists(String bucketName) throws Exception {
        log.debug("OSS检查存储桶: {}", bucketName);
        
        try {
            // 检查bucket名称是否合规
            validateBucketName(bucketName);
            
            // 检查bucket是否存在
            boolean exists = ossClient.doesBucketExist(bucketName);
            log.debug("OSS存储桶检查结果: {} = {}", bucketName, exists);
            
            if (!exists) {
                log.info("OSS存储桶不存在，开始创建: {}", bucketName);
                ossClient.createBucket(bucketName);
                log.info("✅ OSS创建存储桶成功: {}", bucketName);
                
                // 验证创建是否成功
                if (ossClient.doesBucketExist(bucketName)) {
                    log.info("✅ OSS存储桶创建验证成功: {}", bucketName);
                } else {
                    throw new Exception("OSS存储桶创建后验证失败: " + bucketName);
                }
            } else {
                log.debug("✅ OSS存储桶已存在: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("❌ OSS存储桶操作失败: bucket={}, 错误={}", bucketName, e.getMessage(), e);
            throw new Exception("OSS存储桶操作失败: " + bucketName + ", 原因: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证bucket名称是否符合OSS规范
     */
    private void validateBucketName(String bucketName) throws Exception {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new Exception("bucket名称不能为空");
        }
        
        bucketName = bucketName.trim().toLowerCase();
        
        // 长度检查
        if (bucketName.length() < 3 || bucketName.length() > 63) {
            throw new Exception("bucket名称长度必须在3-63字符之间: " + bucketName);
        }
        
        // 字符检查
        if (!bucketName.matches("^[a-z0-9][a-z0-9\\-]*[a-z0-9]$") && bucketName.length() > 1) {
            throw new Exception("bucket名称只能包含小写字母、数字、短横线，且不能以短横线开头或结尾: " + bucketName);
        }
        
        if (bucketName.length() == 1 && !bucketName.matches("^[a-z0-9]$")) {
            throw new Exception("单字符bucket名称只能是小写字母或数字: " + bucketName);
        }
        
        log.debug("✅ OSS bucket名称验证通过: {}", bucketName);
    }

    @Override
    public String getStorageType() {
        return "oss";
    }

    @Override
    public boolean isAvailable() {
        try {
            // 通过列举存储桶来检查OSS客户端是否可用
            ossClient.listBuckets();
            return true;
        } catch (Exception e) {
            log.warn("OSS客户端不可用: {}", e.getMessage());
            return false;
        }
    }

//    @Override
//    public void setBucketCors(String bucketName) throws Exception {
//        log.debug("设置OSS存储桶跨域规则: bucket={}", bucketName);
//
//        // 简化实现，在实际使用时可以根据需要添加具体的CORS配置
//        log.info("OSS存储桶跨域规则设置功能已预留，bucket: {}", bucketName);
//    }

//    @Override
//    public String getBucketLocation(String bucketName) throws Exception {
//        log.debug("获取OSS存储桶区域: bucket={}", bucketName);
//
//        String location = ossClient.getBucketLocation(bucketName);
//
//        log.debug("OSS存储桶区域: bucket={}, location={}", bucketName, location);
//        return location;
//    }

    // 添加默认bucket的方法重载
    @Override
    public void removeObject(String objectName) throws Exception {
        removeObject(storageProperties.getBucketName(), objectName);
    }

    @Override
    public String getPresignedObjectUrl(String objectName, int expiry) throws Exception {
        return getPresignedObjectUrl(storageProperties.getBucketName(), objectName, expiry);
    }

    @Override
    public boolean doesObjectExist(String objectName) throws Exception {
        return doesObjectExist(storageProperties.getBucketName(), objectName);
    }

    @Override
    public void ensureBucketExists() throws Exception {
        ensureBucketExists(storageProperties.getBucketName());
    }

    // 批量操作方法
    @Override
    public List<String> putObjects(List<StorageService.FileUploadInfo> files) throws Exception {
        return putObjects(storageProperties.getBucketName(), files);
    }

    @Override
    public List<String> putObjects(String bucketName, List<StorageService.FileUploadInfo> files) throws Exception {
        log.debug("OSS批量上传文件: bucket={}, count={}", bucketName, files.size());
        
        List<String> results = new ArrayList<>();
        for (StorageService.FileUploadInfo file : files) {
            try {
                putObject(bucketName, file.getObjectName(), file.getInputStream(), 
                         file.getContentType(), file.getFileSize());
                results.add(file.getObjectName());
                log.debug("OSS批量上传成功: {}", file.getObjectName());
            } catch (Exception e) {
                log.error("OSS批量上传失败: {}, 错误: {}", file.getObjectName(), e.getMessage());
                throw new Exception("批量上传失败，文件: " + file.getObjectName() + ", 错误: " + e.getMessage(), e);
            }
        }
        
        log.info("OSS批量上传完成: bucket={}, 成功数量={}", bucketName, results.size());
        return results;
    }

    @Override
    public void removeObjects(List<String> objectNames) throws Exception {
        removeObjects(storageProperties.getBucketName(), objectNames);
    }

    @Override
    public void removeObjects(String bucketName, List<String> objectNames) throws Exception {
        log.debug("OSS批量删除文件: bucket={}, count={}", bucketName, objectNames.size());
        
        for (String objectName : objectNames) {
            try {
                removeObject(bucketName, objectName);
                log.debug("OSS批量删除成功: {}", objectName);
            } catch (Exception e) {
                log.error("OSS批量删除失败: {}, 错误: {}", objectName, e.getMessage());
                throw new Exception("批量删除失败，文件: " + objectName + ", 错误: " + e.getMessage(), e);
            }
        }
        
        log.info("OSS批量删除完成: bucket={}, 删除数量={}", bucketName, objectNames.size());
    }

    @Override
    public List<Boolean> doesObjectsExist(List<String> objectNames) throws Exception {
        return doesObjectsExist(storageProperties.getBucketName(), objectNames);
    }

    @Override
    public List<Boolean> doesObjectsExist(String bucketName, List<String> objectNames) throws Exception {
        log.debug("OSS批量检查文件存在: bucket={}, count={}", bucketName, objectNames.size());
        
        List<Boolean> results = new ArrayList<>();
        for (String objectName : objectNames) {
            try {
                boolean exists = doesObjectExist(bucketName, objectName);
                results.add(exists);
                log.debug("OSS批量检查: {} = {}", objectName, exists);
            } catch (Exception e) {
                log.error("OSS批量检查失败: {}, 错误: {}", objectName, e.getMessage());
                // 出错时认为不存在
                results.add(false);
            }
        }
        
        log.info("OSS批量检查完成: bucket={}, 检查数量={}", bucketName, results.size());
        return results;
    }
    
    @Override
    public void copyObject(String sourceObjectName, String targetObjectName) throws Exception {
        copyObject(storageProperties.getBucketName(), sourceObjectName, 
                 storageProperties.getBucketName(), targetObjectName);
    }
    
    @Override
    public void copyObject(String sourceBucketName, String sourceObjectName, 
                         String targetBucketName, String targetObjectName) throws Exception {
        log.debug("OSS复制文件: sourceBucket={}, sourceObject={}, targetBucket={}, targetObject={}", 
                 sourceBucketName, sourceObjectName, targetBucketName, targetObjectName);
        
        try {
            // 使用OSS的copyObject方法复制文件
            ossClient.copyObject(sourceBucketName, sourceObjectName, targetBucketName, targetObjectName);
            
            log.info("OSS文件复制成功: {} -> {}", sourceObjectName, targetObjectName);
        } catch (Exception e) {
            log.error("OSS文件复制失败: {}, 错误: {}", sourceObjectName, e.getMessage());
            throw new Exception("文件复制失败: " + e.getMessage(), e);
        }
    }
}
