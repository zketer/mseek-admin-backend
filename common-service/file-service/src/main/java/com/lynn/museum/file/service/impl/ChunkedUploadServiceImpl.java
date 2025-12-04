package com.lynn.museum.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.file.config.StorageProperties;
import com.lynn.museum.file.entity.FileRecord;
import com.lynn.museum.file.enums.FileTypeEnum;
import com.lynn.museum.file.mapper.FileRecordMapper;
import com.lynn.museum.file.service.ChunkedUploadService;
import com.lynn.museum.file.service.StorageService;
import com.lynn.museum.file.service.impl.MinioStorageServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分片上传服务实现类
 * 
 * @author Lynn
 * @since 2025-11-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkedUploadServiceImpl implements ChunkedUploadService {

    private final StorageService storageService;
    private final StorageProperties storageProperties;
    private final FileRecordMapper fileRecordMapper;
    
    // 存储上传信息的内存缓存（生产环境应该使用Redis等分布式缓存）
    private final Map<String, UploadInfo> uploadInfoMap = new ConcurrentHashMap<>();
    
    // 临时文件存储目录
    private final String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "mseek-uploads";

    @Override
    public Map<String, Object> initMultipartUpload(String fileName, long fileSize, int totalChunks, String fileTypeStr, Long uploaderId, String md5Hash) {
        Map<String, Object> result = new HashMap<>();
        
        // 转换文件类型
        FileTypeEnum fileType;
        try {
            // 先尝试直接使用枚举名称
            fileType = FileTypeEnum.valueOf(fileTypeStr.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            // 如果失败，尝试使用code查找
            try {
                fileType = FileTypeEnum.fromCode(fileTypeStr);
            } catch (IllegalArgumentException ex) {
                log.warn("无效的文件类型: {}, 使用默认类型TEMP", fileTypeStr);
                fileType = FileTypeEnum.TEMP;
            }
        }
        
        // 验证文件大小
        if (fileSize > storageProperties.getMaxFileSize()) {
            throw new BizException("文件大小超过限制: " + (storageProperties.getMaxFileSize() / 1024 / 1024) + "MB");
        }
        
        // 如果提供了MD5哈希，尝试秒传
        if (md5Hash != null && !md5Hash.isEmpty()) {
            // 查询数据库中是否存在相同哈希值的文件
            LambdaQueryWrapper<FileRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FileRecord::getMd5Hash, md5Hash)
                       .eq(FileRecord::getStatus, 1)
                       .orderByDesc(FileRecord::getCreateAt); // 按创建时间降序排序，获取最新的记录
            
            // 使用selectList代替selectOne，避免TooManyResultsException
            List<FileRecord> existingFiles = fileRecordMapper.selectList(queryWrapper);
            FileRecord existingFile = existingFiles.isEmpty() ? null : existingFiles.get(0);
            
            if (existingFile != null) {
                log.info("秒传成功: 文件{}已存在，直接返回文件ID {}", fileName, existingFile.getId());
                
                try {
                    // 验证文件是否真实存在于MinIO中
                    log.info("秒传检查 - 数据库记录: ID={}, 文件名={}, 存储路径={}, MD5={}", 
                            existingFile.getId(), 
                            existingFile.getOriginalName(),
                            existingFile.getFileName(),
                            existingFile.getMd5Hash());
                    
                    // 检查存储桶名称
                    String bucketName = existingFile.getBucketName();
                    if (bucketName == null || bucketName.isEmpty()) {
                        bucketName = storageProperties.getBucketName();
                        log.warn("文件记录缺少存储桶名称，使用默认存储桶: {}", bucketName);
                    }
                    
                    // 检查文件路径
                    String objectName = existingFile.getFileName();
                    if (objectName == null || objectName.isEmpty()) {
                        log.error("文件记录缺少文件路径，无法检查文件存在");
                        return null;
                    }
                    
                    // 检查文件是否存在
                    log.info("检查文件在存储服务中是否存在: 存储桶={}, 对象名={}", bucketName, objectName);
                    boolean fileExists = false;
                    
                    // 尝试直接使用MinIO客户端检查文件存在
                    if (storageService instanceof MinioStorageServiceImpl) {
                        MinioStorageServiceImpl minioService = (MinioStorageServiceImpl) storageService;
                        try {
                            fileExists = minioService.doesObjectExist(bucketName, objectName);
                            log.info("使用MinIO客户端直接检查文件存在结果: {}", fileExists);
                        } catch (Exception e) {
                            log.error("使用MinIO客户端检查文件存在时发生异常: {}", e.getMessage(), e);
                        }
                    } else {
                        // 使用通用存储服务接口
                        try {
                            fileExists = storageService.doesObjectExist(bucketName, objectName);
                            log.info("文件存在检查结果: {}, 存储桶={}, 对象名={}", fileExists, bucketName, objectName);
                        } catch (Exception e) {
                            log.error("检查文件存在时发生异常: {}", e.getMessage(), e);
                        }
                    }
                    
                    if (!fileExists) {
                        log.warn("数据库有记录但MinIO中文件不存在: {}", existingFile.getFileName());
                        
                        // 尝试清理数据库中的无效记录
                        try {
                            // 将记录状态改为无效，而不是直接删除
                            existingFile.setStatus(0); // 0表示无效
                            fileRecordMapper.updateById(existingFile);
                            log.info("已将无效文件记录标记为失效: ID={}", existingFile.getId());
                        } catch (Exception e) {
                            log.error("清理无效文件记录失败: {}", e.getMessage(), e);
                        }
                        
                        // 如果文件不存在，跳过秒传，进行正常上传
                        return null;
                    }
                    
                    // 生成新的存储文件名
                    String extension = FileUtil.extName(fileName);
                    String datePath = DateUtil.format(LocalDateTime.now(), "yyyy/MM/dd");
                    String uuid = IdUtil.simpleUUID();
                    String newStoredFileName = String.format("%s/%s/%s.%s", 
                        getPathByFileType(fileType), datePath, uuid, extension);
                    
                    // 复制文件到新的路径
                    storageService.copyObject(existingFile.getFileName(), newStoredFileName);
                    log.info("文件复制成功: {} -> {}", existingFile.getFileName(), newStoredFileName);
                    
                    // 创建新的文件记录
                    FileRecord newFileRecord = new FileRecord();
                    newFileRecord.setOriginalName(fileName);
                    newFileRecord.setFileName(newStoredFileName);
                    newFileRecord.setFileSize(fileSize);
                    newFileRecord.setContentType(existingFile.getContentType());
                    newFileRecord.setBucketName(storageProperties.getBucketName());
                    newFileRecord.setCategory(fileType.getCode());
                    newFileRecord.setUploaderId(uploaderId);
                    newFileRecord.setStatus(1);
                    newFileRecord.setAccessCount(0);
                    newFileRecord.setMd5Hash(md5Hash);
                    
                    fileRecordMapper.insert(newFileRecord);
                    log.info("新文件记录保存成功: {}", newFileRecord.getId());
                    
                    // 返回秒传成功的结果
                    result.put("uploadId", ""); // 不需要上传ID
                    result.put("fastUpload", true); // 秒传成功标志
                    result.put("fileRecord", newFileRecord); // 返回新的文件记录
                    result.put("chunkSize", calculateOptimalChunkSize(fileSize)); // 保持一致性
                    
                    return result;
                } catch (Exception e) {
                    log.error("秒传处理失败: {}", e.getMessage(), e);
                    // 如果复制失败，进行正常上传
                }
            }
        }
        
        // 如果不能秒传，进行正常的分片上传初始化
        // 生成上传ID
        String uploadId = IdUtil.fastSimpleUUID();
        
        // 生成存储文件名
        String extension = FileUtil.extName(fileName);
        String datePath = DateUtil.format(LocalDateTime.now(), "yyyy/MM/dd");
        String uuid = IdUtil.simpleUUID();
        String storedFileName = String.format("%s/%s/%s.%s", 
            getPathByFileType(fileType), datePath, uuid, extension);
        
        // 创建临时目录
        Path tempDir = Paths.get(TEMP_DIR, uploadId);
        try {
            Files.createDirectories(tempDir);
        } catch (IOException e) {
            log.error("创建临时目录失败: {}", e.getMessage(), e);
            throw new BizException("初始化上传失败: " + e.getMessage());
        }
        
        // 存储上传信息
        UploadInfo uploadInfo = new UploadInfo();
        uploadInfo.setUploadId(uploadId);
        uploadInfo.setOriginalFileName(fileName);
        uploadInfo.setStoredFileName(storedFileName);
        uploadInfo.setFileSize(fileSize);
        uploadInfo.setTotalChunks(totalChunks);
        uploadInfo.setUploadedChunks(new ArrayList<>());
        uploadInfo.setFileType(fileType);
        uploadInfo.setUploaderId(uploaderId);
        uploadInfo.setTempDir(tempDir.toString());
        uploadInfo.setStartTime(System.currentTimeMillis());
        uploadInfo.setMd5Hash(md5Hash); // 存储MD5哈希值，如果有
        
        uploadInfoMap.put(uploadId, uploadInfo);
        
        log.info("初始化分片上传: uploadId={}, fileName={}, totalChunks={}", uploadId, fileName, totalChunks);
        
        // 返回正常上传的结果
        result.put("uploadId", uploadId);
        result.put("fastUpload", false); // 非秒传
        result.put("chunkSize", calculateOptimalChunkSize(fileSize));
        
        return result;
    }

    @Override
    public String uploadChunk(String uploadId, int chunkNumber, MultipartFile chunk) {
        UploadInfo uploadInfo = getUploadInfo(uploadId);
        
        // 验证分片序号
        if (chunkNumber < 1) {
            throw new BizException("分片序号必须大于0: " + chunkNumber);
        }
        
        // 如果总分片数为0，表示还没有设置正确的分片数，自动更新
        int totalChunks = uploadInfo.getTotalChunks();
        if (totalChunks == 0 || chunkNumber > totalChunks) {
            // 更新总分片数，使用最大的分片序号
            uploadInfo.setTotalChunks(Math.max(totalChunks, chunkNumber));
            log.info("更新总分片数: uploadId={}, totalChunks={}", uploadId, uploadInfo.getTotalChunks());
        }
        
        try {
            // 保存分片到临时文件
            Path chunkPath = Paths.get(uploadInfo.getTempDir(), String.valueOf(chunkNumber));
            Files.write(chunkPath, chunk.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            // 记录已上传的分片
            uploadInfo.getUploadedChunks().add(chunkNumber);
            
            log.info("分片上传成功: uploadId={}, chunkNumber={}, progress={}/{}",
                    uploadId, chunkNumber, uploadInfo.getUploadedChunks().size(), uploadInfo.getTotalChunks());
            
            return "chunk-" + chunkNumber;
        } catch (IOException e) {
            log.error("分片上传失败: {}", e.getMessage(), e);
            throw new BizException("分片上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public FileRecord completeMultipartUpload(String uploadId) {
        log.info("开始完成分片上传: uploadId={}", uploadId);
        long startTime = System.currentTimeMillis();
        
        UploadInfo uploadInfo = getUploadInfo(uploadId);
        
        // 检查是否所有分片都已上传
        if (uploadInfo.getUploadedChunks().size() != uploadInfo.getTotalChunks()) {
            int missingChunks = uploadInfo.getTotalChunks() - uploadInfo.getUploadedChunks().size();
            log.warn("分片上传不完整: uploadId={}, 已上传={}, 总分片={}, 缺失={}", 
                    uploadId, uploadInfo.getUploadedChunks().size(), uploadInfo.getTotalChunks(), missingChunks);
            throw new BizException("分片上传不完整，还有" + missingChunks + "个分片未上传");
        }
        
        log.info("分片上传完整性检查通过: uploadId={}, 总分片={}", uploadId, uploadInfo.getTotalChunks());
        
        // 使用更高效的方式检查分片序号是否连续
        List<Integer> uploadedChunks = new ArrayList<>(uploadInfo.getUploadedChunks());
        java.util.Collections.sort(uploadedChunks);
        
        for (int i = 0; i < uploadedChunks.size(); i++) {
            if (uploadedChunks.get(i) != i + 1) {
                log.warn("分片序号不连续: uploadId={}, 期望={}, 实际={}", uploadId, i + 1, uploadedChunks.get(i));
                throw new BizException("分片" + (i + 1) + "未上传");
            }
        }
        
        log.info("分片序号连续性检查通过: uploadId={}", uploadId);
        
        try {
            // 合并分片
            Path mergedFile = Paths.get(uploadInfo.getTempDir(), "merged");
            mergeChunks(uploadInfo, mergedFile);
            
            // 计算文件MD5
            String md5Hash = DigestUtil.md5Hex(Files.newInputStream(mergedFile));
            
            // 确保存储桶存在
            try {
                storageService.ensureBucketExists();
                log.info("已确保存储桶 {} 存在", storageProperties.getBucketName());
            } catch (Exception e) {
                log.error("确保存储桶存在失败: {}", e.getMessage(), e);
                throw new BizException("创建存储桶失败: " + e.getMessage());
            }
            
            // 上传到存储服务
            log.info("开始上传文件到{}存储: {}, 大小: {}MB", 
                    storageService.getStorageType().toUpperCase(), 
                    uploadInfo.getStoredFileName(),
                    String.format("%.2f", uploadInfo.getFileSize() / 1024.0 / 1024.0));
            
            long uploadStartTime = System.currentTimeMillis();
            try (InputStream inputStream = Files.newInputStream(mergedFile)) {
                storageService.putObject(
                    uploadInfo.getStoredFileName(),
                    inputStream,
                    getContentTypeByFileName(uploadInfo.getOriginalFileName()),
                    uploadInfo.getFileSize()
                );
            }
            
            long uploadEndTime = System.currentTimeMillis();
            log.info("文件上传到{}存储成功: {}, 耗时: {}ms, 速度: {}MB/s", 
                    storageService.getStorageType().toUpperCase(), 
                    uploadInfo.getStoredFileName(),
                    (uploadEndTime - uploadStartTime),
                    String.format("%.2f", (uploadInfo.getFileSize() / 1024.0 / 1024.0) / ((uploadEndTime - uploadStartTime) / 1000.0)));
            
            // 保存文件记录到数据库
            FileRecord fileRecord = new FileRecord();
            fileRecord.setOriginalName(uploadInfo.getOriginalFileName());
            fileRecord.setFileName(uploadInfo.getStoredFileName());
            fileRecord.setFileSize(uploadInfo.getFileSize());
            fileRecord.setContentType(getContentTypeByFileName(uploadInfo.getOriginalFileName()));
            fileRecord.setBucketName(storageProperties.getBucketName());
            fileRecord.setCategory(uploadInfo.getFileType().getCode());
            fileRecord.setUploaderId(uploadInfo.getUploaderId());
            fileRecord.setStatus(1);
            fileRecord.setAccessCount(0);
            fileRecord.setMd5Hash(md5Hash);
            
            fileRecordMapper.insert(fileRecord);
            
            log.info("文件记录保存成功: {}", fileRecord.getId());
            
            // 清理临时文件
            cleanupTempFiles(uploadInfo);
            
            // 从缓存中移除上传信息
            uploadInfoMap.remove(uploadId);
            
            long endTime = System.currentTimeMillis();
            log.info("分片上传全过程完成: uploadId={}, 总耗时: {}ms", uploadId, (endTime - startTime));
            
            return fileRecord;
        } catch (Exception e) {
            log.error("完成分片上传失败: {}", e.getMessage(), e);
            throw new BizException("完成分片上传失败: " + e.getMessage());
        }
    }

    @Override
    public void abortMultipartUpload(String uploadId) {
        UploadInfo uploadInfo = uploadInfoMap.get(uploadId);
        if (uploadInfo != null) {
            // 清理临时文件
            cleanupTempFiles(uploadInfo);
            
            // 从缓存中移除上传信息
            uploadInfoMap.remove(uploadId);
            
            log.info("取消分片上传: uploadId={}", uploadId);
        }
    }

    @Override
    public int getUploadStatus(String uploadId) {
        UploadInfo uploadInfo = getUploadInfo(uploadId);
        return uploadInfo.getUploadedChunks().size();
    }
    
    /**
     * 获取上传信息
     */
    private UploadInfo getUploadInfo(String uploadId) {
        UploadInfo uploadInfo = uploadInfoMap.get(uploadId);
        if (uploadInfo == null) {
            throw new BizException("无效的上传ID: " + uploadId);
        }
        
        // 检查上传是否超时（默认24小时）
        long currentTime = System.currentTimeMillis();
        if (currentTime - uploadInfo.getStartTime() > 24 * 60 * 60 * 1000) {
            abortMultipartUpload(uploadId);
            throw new BizException("上传已超时，请重新上传");
        }
        
        return uploadInfo;
    }
    
    /**
     * 合并分片（优化版）
     * 使用缓冲流提高效率
     */
    private void mergeChunks(UploadInfo uploadInfo, Path mergedFile) throws IOException {
        log.info("开始合并文件分片，总分片数: {}", uploadInfo.getTotalChunks());
        long startTime = System.currentTimeMillis();
        
        // 使用缓冲输出流
        try (java.io.OutputStream out = new java.io.BufferedOutputStream(
                Files.newOutputStream(mergedFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE), 8192 * 16)) {
            
            byte[] buffer = new byte[8192 * 16]; // 128KB缓冲区
            int bytesRead;
            
            for (int i = 1; i <= uploadInfo.getTotalChunks(); i++) {
                Path chunkPath = Paths.get(uploadInfo.getTempDir(), String.valueOf(i));
                
                // 使用缓冲输入流
                try (java.io.InputStream in = new java.io.BufferedInputStream(
                        Files.newInputStream(chunkPath), 8192 * 16)) {
                    
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        log.info("文件分片合并完成，耗时: {}ms", (endTime - startTime));
    }
    
    /**
     * 清理临时文件
     */
    private void cleanupTempFiles(UploadInfo uploadInfo) {
        try {
            FileUtil.del(uploadInfo.getTempDir());
        } catch (Exception e) {
            log.warn("清理临时文件失败: {}", e.getMessage());
        }
    }
    
    /**
     * 根据文件名获取内容类型
     */
    private String getContentTypeByFileName(String fileName) {
        String extension = FileUtil.extName(fileName).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "avi" -> "video/avi";
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain";
            case "apk" -> "application/vnd.android.package-archive";
            default -> "application/octet-stream";
        };
    }
    
    /**
     * 根据文件类型获取路径前缀
     */
    private String getPathByFileType(FileTypeEnum fileType) {
        return fileType.getCode();
    }
    
    /**
     * 计算最佳分片大小
     * 根据文件大小动态调整分片大小，保证分片数量在合理范围内
     */
    private int calculateOptimalChunkSize(long fileSize) {
        // 默认分片大小为2MB
        int defaultChunkSize = 2 * 1024 * 1024;
        
        // 如果文件小于10MB，使用1MB分片
        if (fileSize < 10 * 1024 * 1024) {
            return 1024 * 1024;
        }
        
        // 如果文件大于100MB，使用5MB分片
        if (fileSize > 100 * 1024 * 1024) {
            return 5 * 1024 * 1024;
        }
        
        return defaultChunkSize;
    }
    
    /**
     * 上传信息类
     */
    private static class UploadInfo {
        private String uploadId;
        private String originalFileName;
        private String storedFileName;
        private long fileSize;
        private int totalChunks;
        private List<Integer> uploadedChunks;
        private FileTypeEnum fileType;
        private Long uploaderId;
        private String tempDir;
        private long startTime;
        private String md5Hash;
        
        // Getters and setters
        public String getUploadId() { return uploadId; }
        public void setUploadId(String uploadId) { this.uploadId = uploadId; }
        
        public String getOriginalFileName() { return originalFileName; }
        public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
        
        public String getStoredFileName() { return storedFileName; }
        public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }
        
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        
        public int getTotalChunks() { return totalChunks; }
        public void setTotalChunks(int totalChunks) { this.totalChunks = totalChunks; }
        
        public List<Integer> getUploadedChunks() { return uploadedChunks; }
        public void setUploadedChunks(List<Integer> uploadedChunks) { this.uploadedChunks = uploadedChunks; }
        
        public FileTypeEnum getFileType() { return fileType; }
        public void setFileType(FileTypeEnum fileType) { this.fileType = fileType; }
        
        public Long getUploaderId() { return uploaderId; }
        public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }
        
        public String getTempDir() { return tempDir; }
        public void setTempDir(String tempDir) { this.tempDir = tempDir; }
        
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        
        public String getMd5Hash() { return md5Hash; }
        public void setMd5Hash(String md5Hash) { this.md5Hash = md5Hash; }
    }
}
