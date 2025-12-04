package com.lynn.museum.file.service.impl;

import com.lynn.museum.file.config.StorageProperties;
import com.lynn.museum.file.service.MinioStorageService;
import com.lynn.museum.file.service.StorageService;
import io.minio.*;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MinIO存储服务实现
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.type", havingValue = "minio", matchIfMissing = true)
public class MinioStorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final StorageProperties storageProperties;

    @Override
    public void putObject(String objectName, InputStream inputStream, String contentType, long fileSize) throws Exception {
        putObject(storageProperties.getBucketName(), objectName, inputStream, contentType, fileSize);
    }

    @Override
    public void putObject(String bucketName, String objectName, InputStream inputStream, 
                         String contentType, long fileSize) throws Exception {
        log.debug("MinIO上传文件: bucket={}, object={}, size={}", bucketName, objectName, fileSize);
        
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream, fileSize, -1)
                .contentType(contentType)
                .build()
        );
        
        log.info("MinIO文件上传成功: {}", objectName);
    }

    @Override
    public InputStream getObject(String objectName) throws Exception {
        return getObject(storageProperties.getBucketName(), objectName);
    }

    @Override
    public InputStream getObject(String bucketName, String objectName) throws Exception {
        log.debug("MinIO下载文件: bucket={}, object={}", bucketName, objectName);
        
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build()
        );
    }

    @Override
    public void removeObject(String objectName) throws Exception {
        removeObject(storageProperties.getBucketName(), objectName);
    }

    @Override
    public void removeObject(String bucketName, String objectName) throws Exception {
        log.debug("MinIO删除文件: bucket={}, object={}", bucketName, objectName);
        
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build()
        );
        
        log.info("MinIO文件删除成功: {}", objectName);
    }

    @Override
    public String getPresignedObjectUrl(String objectName, int expiry) throws Exception {
        return getPresignedObjectUrl(storageProperties.getBucketName(), objectName, expiry);
    }

    @Override
    public String getPresignedObjectUrl(String bucketName, String objectName, int expiry) throws Exception {
        log.debug("MinIO获取预签名URL: bucket={}, object={}, expiry={}", bucketName, objectName, expiry);
        
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(expiry, TimeUnit.SECONDS)
                .build()
        );
    }

    @Override
    public String getPresignedDownloadUrl(String objectName, String originalFileName, int expiry) throws Exception {
        return getPresignedDownloadUrl(storageProperties.getBucketName(), objectName, originalFileName, expiry);
    }

    @Override
    public String getPresignedDownloadUrl(String bucketName, String objectName, String originalFileName, int expiry) throws Exception {
        log.debug("MinIO获取强制下载URL: bucket={}, object={}, originalFileName={}, expiry={}", 
                  bucketName, objectName, originalFileName, expiry);
        
        // 使用extraQueryParams添加response-content-disposition头，强制浏览器下载
        java.util.Map<String, String> extraQueryParams = new java.util.HashMap<>();
        
        // 对文件名进行URL编码，支持中文和特殊字符
        String encodedFileName;
        try {
            // 将+号替换为%20，符合RFC标准
            encodedFileName = java.net.URLEncoder.encode(originalFileName, "UTF-8")
                    .replaceAll("\\+", "%20");
        } catch (java.io.UnsupportedEncodingException e) {
            log.error("文件名编码失败: {}", originalFileName, e);
            encodedFileName = originalFileName;
        }
        
        // 设置Content-Disposition为attachment，强制下载
        extraQueryParams.put("response-content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
        
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(expiry, TimeUnit.SECONDS)
                .extraQueryParams(extraQueryParams)
                .build()
        );
    }

    @Override
    public boolean doesObjectExist(String objectName) throws Exception {
        return doesObjectExist(storageProperties.getBucketName(), objectName);
    }

    @Override
    public boolean doesObjectExist(String bucketName, String objectName) throws Exception {
        log.info("MinIO检查文件存在: bucket={}, object={}", bucketName, objectName);
        
        // 参数校验
        if (bucketName == null || bucketName.isEmpty()) {
            log.error("MinIO检查文件存在失败: 存储桶名称为空");
            return false;
        }
        
        if (objectName == null || objectName.isEmpty()) {
            log.error("MinIO检查文件存在失败: 对象名称为空");
            return false;
        }
        
        // 打印MinIO客户端配置信息
        StorageProperties.MinioConfig config = storageProperties.getMinio();
        log.info("MinIO客户端配置: endpoint={}, accessKey={}", 
                config.getEndpoint(), 
                config.getAccessKey().substring(0, Math.min(3, config.getAccessKey().length())) + "***");
        
        // 重试机制
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;
        Exception lastException = null;
        
        while (retryCount < maxRetries && !success) {
            try {
                // 检查存储桶是否存在
                boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
                );
                
                if (!bucketExists) {
                    log.warn("MinIO存储桶不存在: {}", bucketName);
                    return false;
                }
                
                // 检查对象是否存在
                StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
                );
                
                log.info("MinIO文件存在: bucket={}, object={}, size={}, etag={}, lastModified={}", 
                        bucketName, objectName, stat.size(), stat.etag(), stat.lastModified());
                return true;
            } catch (io.minio.errors.ErrorResponseException e) {
                // 如果是404错误，说明文件不存在，不需要重试
                if (e.response() != null && e.response().code() == 404) {
                    log.info("MinIO文件不存在(404): bucket={}, object={}", bucketName, objectName);
                    return false;
                }
                lastException = e;
                log.warn("MinIO检查文件存在失败(第{}\u6b21尝试): bucket={}, object={}, error={}", 
                        retryCount + 1, bucketName, objectName, e.getMessage());
            } catch (Exception e) {
                lastException = e;
                log.warn("MinIO检查文件存在异常(第{}\u6b21尝试): bucket={}, object={}, error={}", 
                        retryCount + 1, bucketName, objectName, e.getMessage());
            }
            
            retryCount++;
            if (retryCount < maxRetries) {
                // 等待一段时间后重试
                try {
                    Thread.sleep(1000 * retryCount); // 每次重试等待时间增加
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        if (lastException != null) {
            log.error("MinIO检查文件存在失败(重试{}\u6b21后): bucket={}, object={}, error={}", 
                    maxRetries, bucketName, objectName, lastException.getMessage());
        }
        
        return false;
    }

    @Override
    public void ensureBucketExists() throws Exception {
        ensureBucketExists(storageProperties.getBucketName());
    }

    @Override
    public void ensureBucketExists(String bucketName) throws Exception {
        log.debug("MinIO检查存储桶: {}", bucketName);
        
        boolean exists = minioClient.bucketExists(
            BucketExistsArgs.builder()
                .bucket(bucketName)
                .build()
        );
        
        if (!exists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
            log.info("MinIO创建存储桶成功: {}", bucketName);
        }
    }

    @Override
    public String getStorageType() {
        return "minio";
    }

    @Override
    public boolean isAvailable() {
        try {
            // 通过列举存储桶来检查MinIO客户端是否可用
            minioClient.listBuckets();
            return true;
        } catch (Exception e) {
            log.warn("MinIO客户端不可用: {}", e.getMessage());
            return false;
        }
    }

//    @Override
//    public void setBucketPolicy(String bucketName, String policy) throws Exception {
//        log.debug("设置MinIO存储桶策略: bucket={}", bucketName);
//
//        minioClient.setBucketPolicy(
//            SetBucketPolicyArgs.builder()
//                .bucket(bucketName)
//                .config(policy)
//                .build()
//        );
//
//        log.info("MinIO存储桶策略设置成功: {}", bucketName);
//    }

    @Override
    public List<String> putObjects(List<StorageService.FileUploadInfo> files) throws Exception {
        return putObjects(storageProperties.getBucketName(), files);
    }

    @Override
    public List<String> putObjects(String bucketName, List<StorageService.FileUploadInfo> files) throws Exception {
        log.debug("MinIO批量上传文件: bucket={}, count={}", bucketName, files.size());
        
        List<String> results = new ArrayList<>();
        for (StorageService.FileUploadInfo file : files) {
            try {
                putObject(bucketName, file.getObjectName(), file.getInputStream(), 
                         file.getContentType(), file.getFileSize());
                results.add(file.getObjectName());
                log.debug("MinIO批量上传成功: {}", file.getObjectName());
            } catch (Exception e) {
                log.error("MinIO批量上传失败: {}, 错误: {}", file.getObjectName(), e.getMessage());
                throw new Exception("批量上传失败，文件: " + file.getObjectName() + ", 错误: " + e.getMessage(), e);
            }
        }
        
        log.info("MinIO批量上传完成: bucket={}, 成功数量={}", bucketName, results.size());
        return results;
    }

    @Override
    public void removeObjects(List<String> objectNames) throws Exception {
        removeObjects(storageProperties.getBucketName(), objectNames);
    }

    @Override
    public void removeObjects(String bucketName, List<String> objectNames) throws Exception {
        log.debug("MinIO批量删除文件: bucket={}, count={}", bucketName, objectNames.size());
        
        for (String objectName : objectNames) {
            try {
                removeObject(bucketName, objectName);
                log.debug("MinIO批量删除成功: {}", objectName);
            } catch (Exception e) {
                log.error("MinIO批量删除失败: {}, 错误: {}", objectName, e.getMessage());
                throw new Exception("批量删除失败，文件: " + objectName + ", 错误: " + e.getMessage(), e);
            }
        }
        
        log.info("MinIO批量删除完成: bucket={}, 删除数量={}", bucketName, objectNames.size());
    }

    @Override
    public List<Boolean> doesObjectsExist(List<String> objectNames) throws Exception {
        return doesObjectsExist(storageProperties.getBucketName(), objectNames);
    }

    @Override
    public List<Boolean> doesObjectsExist(String bucketName, List<String> objectNames) throws Exception {
        log.debug("MinIO批量检查文件存在: bucket={}, count={}", bucketName, objectNames.size());
        
        List<Boolean> results = new ArrayList<>();
        for (String objectName : objectNames) {
            try {
                boolean exists = doesObjectExist(bucketName, objectName);
                results.add(exists);
                log.debug("MinIO批量检查: {} = {}", objectName, exists);
            } catch (Exception e) {
                log.error("MinIO批量检查失败: {}, 错误: {}", objectName, e.getMessage());
                // 出错时认为不存在
                results.add(false);
            }
        }
        
        log.info("MinIO批量检查完成: bucket={}, 检查数量={}", bucketName, results.size());
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
        log.debug("MinIO复制文件: sourceBucket={}, sourceObject={}, targetBucket={}, targetObject={}", 
                 sourceBucketName, sourceObjectName, targetBucketName, targetObjectName);
        
        try {
            // 使用MinIO的CopyObjectArgs来复制文件
            minioClient.copyObject(
                CopyObjectArgs.builder()
                    .source(CopySource.builder()
                        .bucket(sourceBucketName)
                        .object(sourceObjectName)
                        .build())
                    .bucket(targetBucketName)
                    .object(targetObjectName)
                    .build()
            );
            
            log.info("MinIO文件复制成功: {} -> {}", sourceObjectName, targetObjectName);
        } catch (Exception e) {
            log.error("MinIO文件复制失败: {}, 错误: {}", sourceObjectName, e.getMessage());
            throw new Exception("文件复制失败: " + e.getMessage(), e);
        }
    }
}
