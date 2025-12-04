package com.lynn.museum.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.file.config.StorageProperties;
import com.lynn.museum.file.entity.FileRecord;
import com.lynn.museum.file.enums.FileTypeEnum;
import com.lynn.museum.file.mapper.FileRecordMapper;
import com.lynn.museum.file.service.FileService;
import com.lynn.museum.file.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.List;

/**
 * 文件服务实现类
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final StorageService storageService;
    private final StorageProperties storageProperties;
    private final FileRecordMapper fileRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileRecord uploadFile(MultipartFile file, FileTypeEnum fileType, Long uploaderId) {
        try {
            // 验证文件
            validateFile(file);
            
            // 确保存储桶存在
            ensureBucketExists();
            
            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename(), fileType);
            
            // 计算文件MD5
            String md5Hash = DigestUtil.md5Hex(file.getInputStream());
            
            // 上传文件到存储服务（使用默认bucket）
            storageService.putObject(
                fileName,
                file.getInputStream(),
                file.getContentType(),
                file.getSize()
            );
            
            log.info("文件上传到{}存储成功: {}", storageService.getStorageType().toUpperCase(), fileName);
            
            // 保存文件记录到数据库
            FileRecord fileRecord = new FileRecord();
            fileRecord.setOriginalName(file.getOriginalFilename());
            fileRecord.setFileName(fileName);
            fileRecord.setFileSize(file.getSize());
            fileRecord.setContentType(file.getContentType());
            fileRecord.setBucketName(storageProperties.getBucketName());
            fileRecord.setCategory(fileType.getCode());
            fileRecord.setUploaderId(uploaderId);
            // 正常状态
            fileRecord.setStatus(1);
            fileRecord.setAccessCount(0);
            fileRecord.setMd5Hash(md5Hash);
            
            fileRecordMapper.insert(fileRecord);
            
            log.info("文件记录保存成功: {}", fileRecord.getId());
            
            return fileRecord;
            
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BizException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FileRecord> uploadFiles(MultipartFile[] files, FileTypeEnum fileType, Long uploaderId) {
        List<FileRecord> fileRecords = new ArrayList<>();
        
        for (MultipartFile file : files) {
            FileRecord fileRecord = uploadFile(file, fileType, uploaderId);
            fileRecords.add(fileRecord);
        }
        
        return fileRecords;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId) {
        FileRecord fileRecord = fileRecordMapper.selectById(fileId);
        if (fileRecord == null) {
            throw new BizException("文件不存在");
        }
        
        try {
            // 从存储服务删除文件
            storageService.removeObject(fileRecord.getFileName());
            
            // 从数据库删除记录
            fileRecordMapper.deleteById(fileId);
            
            log.info("文件删除成功: {}", fileRecord.getFileName());
            
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new BizException("文件删除失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFileByName(String fileName) {
        LambdaQueryWrapper<FileRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileRecord::getFileName, fileName);
        
        FileRecord fileRecord = fileRecordMapper.selectOne(queryWrapper);
        if (fileRecord == null) {
            throw new BizException("文件不存在");
        }
        
        deleteFile(fileRecord.getId());
    }

    @Override
    public String getFileUrl(Long fileId) {
        FileRecord fileRecord = fileRecordMapper.selectById(fileId);
        if (fileRecord == null) {
            throw new BizException("文件不存在");
        }
        
        return getFileUrlByName(fileRecord.getFileName());
    }

    @Override
    public List<Map<String, Object>> getBatchFileUrls(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 批量查询文件记录
        List<FileRecord> fileRecords = fileRecordMapper.selectBatchIds(fileIds);
        
        for (FileRecord fileRecord : fileRecords) {
            try {
                String url = getFileUrlByName(fileRecord.getFileName());
                
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("fileId", fileRecord.getId());
                fileInfo.put("url", url);
                fileInfo.put("fileName", fileRecord.getFileName());
                fileInfo.put("originalName", fileRecord.getOriginalName());
                
                result.add(fileInfo);
            } catch (Exception e) {
                log.warn("获取文件URL失败，文件ID: {}, 错误: {}", fileRecord.getId(), e.getMessage());
                // 失败的文件跳过，不影响其他文件
            }
        }
        
        return result;
    }

    @Override
    public String getFileUrlByName(String fileName) {
        try {
            return storageService.getPresignedObjectUrl(
                fileName,
                (int) storageProperties.getUrlExpiry()
            );
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", e.getMessage(), e);
            throw new BizException("获取文件URL失败: " + e.getMessage());
        }
    }

    @Override
    public String getFileDownloadUrl(Long fileId) {
        FileRecord fileRecord = fileRecordMapper.selectById(fileId);
        if (fileRecord == null) {
            throw new BizException("文件不存在");
        }
        
        return getFileDownloadUrlByName(fileRecord.getFileName());
    }

    @Override
    public String getFileDownloadUrlByName(String fileName) {
        // 获取文件记录，用于获取原始文件名
        FileRecord fileRecord = getFileRecordByName(fileName);
        
        try {
            // 使用原始文件名生成强制下载URL
            // 如果原始文件名为空，使用存储文件名
            String originalName = fileRecord.getOriginalName();
            if (originalName == null || originalName.isEmpty()) {
                log.warn("文件记录中原始文件名为空，使用存储文件名: {}", fileName);
                originalName = fileName;
            }
            
            return storageService.getPresignedDownloadUrl(
                fileName,
                originalName,
                (int) storageProperties.getUrlExpiry()
            );
        } catch (Exception e) {
            log.error("获取文件下载URL失败: {}", e.getMessage(), e);
            throw new BizException("获取文件下载URL失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadFile(Long fileId) {
        FileRecord fileRecord = fileRecordMapper.selectById(fileId);
        if (fileRecord == null) {
            throw new BizException("文件不存在");
        }
        
        // 增加访问次数
        fileRecord.setAccessCount(fileRecord.getAccessCount() + 1);
        fileRecordMapper.updateById(fileRecord);
        
        return downloadFileByName(fileRecord.getFileName());
    }

    @Override
    public InputStream downloadFileByName(String fileName) {
        try {
            return storageService.getObject(fileName);
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new BizException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public FileRecord getFileRecord(Long fileId) {
        FileRecord fileRecord = fileRecordMapper.selectById(fileId);
        if (fileRecord == null) {
            throw new BizException("文件不存在");
        }
        return fileRecord;
    }

    @Override
    public Map<Long, FileRecord> getBatchFileRecords(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // 批量查询文件记录
        List<FileRecord> fileRecords = fileRecordMapper.selectBatchIds(fileIds);
        
        // 转换为Map，key为文件ID
        return fileRecords.stream()
                .collect(Collectors.toMap(FileRecord::getId, record -> record));
    }

    @Override
    public FileRecord getFileRecordByName(String fileName) {
        LambdaQueryWrapper<FileRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileRecord::getFileName, fileName);
        
        FileRecord fileRecord = fileRecordMapper.selectOne(queryWrapper);
        if (fileRecord == null) {
            throw new BizException("文件不存在");
        }
        return fileRecord;
    }

    @Override
    public boolean fileExists(String fileName) {
        try {
            return storageService.doesObjectExist(fileName);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > storageProperties.getMaxFileSize()) {
            throw new BizException("文件大小超过限制: " + (storageProperties.getMaxFileSize() / 1024 / 1024) + "MB");
        }
        
        // 临时注释：解除文件类型限制，用于测试
        // 检查文件类型
        // String contentType = file.getContentType();
        // if (StrUtil.isBlank(contentType) || 
        //     !Arrays.asList(storageProperties.getAllowedContentTypes()).contains(contentType)) {
        //     throw new BizException("不支持的文件类型: " + contentType);
        // }
    }

    /**
     * 确保存储桶存在
     */
    private void ensureBucketExists() {
        try {
            storageService.ensureBucketExists();
        } catch (Exception e) {
            log.error("检查/创建存储桶失败: {}", e.getMessage(), e);
            throw new BizException("存储桶操作失败: " + e.getMessage());
        }
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String originalFilename, FileTypeEnum fileType) {
        String extension = FileUtil.extName(originalFilename);
        String datePath = DateUtil.format(LocalDateTime.now(), "yyyy/MM/dd");
        String uuid = IdUtil.simpleUUID();
        
        return String.format("%s/%s/%s.%s", 
            getPathByFileType(fileType), datePath, uuid, extension);
    }

    /**
     * 根据文件类型获取路径前缀
     */
    private String getPathByFileType(FileTypeEnum fileType) {
        StorageProperties.PathConfig pathConfig = storageProperties.getPath();
        return switch (fileType) {
            case AVATAR -> pathConfig.getAvatar();
            case MUSEUM -> pathConfig.getMuseum();
            case ANNOUNCEMENT -> pathConfig.getAnnouncement();
            case BANNER -> pathConfig.getBanner();
            case RECOMMENDATION -> pathConfig.getRecommendation();
            case CHECKIN -> pathConfig.getCheckin();
            case APP_VERSION -> pathConfig.getAppVersion();
            case TEMP -> pathConfig.getTemp();
            case EXHIBITION -> pathConfig.getExhibition();
        };
    }
}
