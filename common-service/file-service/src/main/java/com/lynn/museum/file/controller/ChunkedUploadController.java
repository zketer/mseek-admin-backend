package com.lynn.museum.file.controller;

import com.lynn.museum.common.result.Result;
import com.lynn.museum.file.entity.FileRecord;
import com.lynn.museum.file.service.ChunkedUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 分片上传控制器
 * 
 * @author Lynn
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping("/chunked")
@RequiredArgsConstructor
@Tag(name = "ChunkedUploadController", description = "用于大文件分片上传")
public class ChunkedUploadController {

    private final ChunkedUploadService chunkedUploadService;

    @PostMapping("/init")
    @Operation(summary = "初始化分片上传", description = "开始一个新的分片上传任务，支持秒传")
    public Result<Map<String, Object>> initUpload(
            @Parameter(description = "原始文件名") @RequestParam String fileName,
            @Parameter(description = "文件总大小（字节）") @RequestParam long fileSize,
            @Parameter(description = "总分片数") @RequestParam int totalChunks,
            @Parameter(description = "文件类型") @RequestParam String fileType,
            @Parameter(description = "上传者ID") @RequestParam(required = false) Long uploaderId,
            @Parameter(description = "文件MD5哈希值，用于秒传") @RequestParam(required = false) String md5Hash) {
        
        // 默认上传者ID为1（系统用户）
        if (uploaderId == null) {
            uploaderId = 1L;
        }
        
        // 调用服务层初始化上传
        Map<String, Object> serviceResult = chunkedUploadService.initMultipartUpload(
                fileName, fileSize, totalChunks, fileType, uploaderId, md5Hash);
        
        return Result.success(serviceResult);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传分片", description = "上传单个分片")
    public Result<String> uploadChunk(
            @Parameter(description = "上传ID") @RequestParam String uploadId,
            @Parameter(description = "分片序号（从1开始）") @RequestParam int chunkNumber,
            @Parameter(description = "分片文件") @RequestParam("file") MultipartFile chunk) {
        
        String etag = chunkedUploadService.uploadChunk(uploadId, chunkNumber, chunk);
        return Result.success(etag);
    }

    @PostMapping("/complete")
    @Operation(summary = "完成上传", description = "合并所有分片完成上传")
    public Result<FileRecord> completeUpload(
            @Parameter(description = "上传ID") @RequestParam String uploadId) {
        
        FileRecord fileRecord = chunkedUploadService.completeMultipartUpload(uploadId);
        return Result.success(fileRecord);
    }

    @PostMapping("/abort")
    @Operation(summary = "取消上传", description = "取消上传并清理临时文件")
    public Result<Void> abortUpload(
            @Parameter(description = "上传ID") @RequestParam String uploadId) {
        
        chunkedUploadService.abortMultipartUpload(uploadId);
        return Result.success();
    }

    @GetMapping("/status")
    @Operation(summary = "查询上传状态", description = "获取已上传的分片数")
    public Result<Map<String, Object>> getUploadStatus(
            @Parameter(description = "上传ID") @RequestParam String uploadId) {
        
        int uploadedChunks = chunkedUploadService.getUploadStatus(uploadId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("uploadId", uploadId);
        result.put("uploadedChunks", uploadedChunks);
        
        return Result.success(result);
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
}
