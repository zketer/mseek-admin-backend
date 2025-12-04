package com.lynn.museum.file.controller;

import com.lynn.museum.common.result.Result;
import com.lynn.museum.common.result.ResultUtils;
import com.lynn.museum.file.entity.FileRecord;
import com.lynn.museum.file.enums.FileTypeEnum;
import com.lynn.museum.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件管理控制器
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "FileController", description = "文件上传下载相关接口")
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传单个文件")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FileRecord> uploadFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件类型") @RequestParam("type") String fileType,
            @Parameter(description = "上传者ID") @RequestParam(value = "uploaderId", required = false) Long uploaderId) {
        
        log.info("上传文件: {}, 类型: {}, 上传者: {}", file.getOriginalFilename(), fileType, uploaderId);
        
        FileTypeEnum type = FileTypeEnum.fromCode(fileType);
        FileRecord fileRecord = fileService.uploadFile(file, type, uploaderId);
        
        return ResultUtils.success(fileRecord);
    }

    @Operation(summary = "批量上传文件")
    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<List<FileRecord>> uploadFiles(
            @Parameter(description = "文件列表") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "文件类型") @RequestParam("type") String fileType,
            @Parameter(description = "上传者ID") @RequestParam(value = "uploaderId", required = false) Long uploaderId) {
        
        log.info("批量上传文件: {} 个, 类型: {}, 上传者: {}", files.length, fileType, uploaderId);
        
        FileTypeEnum type = FileTypeEnum.fromCode(fileType);
        List<FileRecord> fileRecords = fileService.uploadFiles(files, type, uploaderId);
        
        return ResultUtils.success(fileRecords);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteFile(
            @Parameter(description = "文件ID") @PathVariable Long fileId) {
        
        log.info("删除文件: {}", fileId);
        
        fileService.deleteFile(fileId);
        
        return ResultUtils.success();
    }

    @Operation(summary = "根据文件名删除文件")
    @DeleteMapping("/name/{fileName:.+}")
    public Result<Void> deleteFileByName(
            @Parameter(description = "文件名") @PathVariable String fileName) {
        
        log.info("根据文件名删除文件: {}", fileName);
        
        fileService.deleteFileByName(fileName);
        
        return ResultUtils.success();
    }

    @Operation(summary = "获取文件访问URL")
    @GetMapping("/url/{fileId}")
    public Result<String> getFileUrl(
            @Parameter(description = "文件ID") @PathVariable Long fileId) {
        
        String url = fileService.getFileUrl(fileId);
        
        return ResultUtils.success(url);
    }

    @Operation(summary = "批量获取文件访问URL")
    @PostMapping("/urls/batch")
    public Result<List<Map<String, Object>>> getBatchFileUrls(
            @Parameter(description = "文件ID列表") @RequestBody List<Long> fileIds) {
        
        List<Map<String, Object>> urls = fileService.getBatchFileUrls(fileIds);
        
        return ResultUtils.success(urls);
    }

    @Operation(summary = "根据文件名获取访问URL")
    @GetMapping("/url/name/{fileName:.+}")
    public Result<String> getFileUrlByName(
            @Parameter(description = "文件名") @PathVariable String fileName) {
        
        String url = fileService.getFileUrlByName(fileName);
        
        return ResultUtils.success(url);
    }

    @Operation(summary = "获取文件强制下载URL")
    @GetMapping("/download-url/{fileId}")
    public Result<String> getFileDownloadUrl(
            @Parameter(description = "文件ID") @PathVariable Long fileId) {
        
        log.info("获取文件强制下载URL，文件ID: {}", fileId);
        String url = fileService.getFileDownloadUrl(fileId);
        
        return ResultUtils.success(url);
    }

    @Operation(summary = "根据文件名获取强制下载URL")
    @GetMapping("/download-url/name/{fileName:.+}")
    public Result<String> getFileDownloadUrlByName(
            @Parameter(description = "文件名") @PathVariable String fileName) {
        
        log.info("根据文件名获取强制下载URL: {}", fileName);
        String url = fileService.getFileDownloadUrlByName(fileName);
        
        return ResultUtils.success(url);
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download/{fileId}")
    public void downloadFile(
            @Parameter(description = "文件ID") @PathVariable Long fileId,
            HttpServletResponse response) {
        
        try {
            FileRecord fileRecord = fileService.getFileRecord(fileId);
            InputStream inputStream = fileService.downloadFile(fileId);
            
            // 设置响应头
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", 
                "attachment; filename=\"" + fileRecord.getOriginalName() + "\"");
            
            // 写入响应流
            inputStream.transferTo(response.getOutputStream());
            response.flushBuffer();
            
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            response.setStatus(500);
        }
    }

    @Operation(summary = "根据文件名下载文件")
    @GetMapping("/download/name/{fileName:.+}")
    public void downloadFileByName(
            @Parameter(description = "文件名") @PathVariable String fileName,
            HttpServletResponse response) {
        
        try {
            FileRecord fileRecord = fileService.getFileRecordByName(fileName);
            InputStream inputStream = fileService.downloadFileByName(fileName);
            
            // 设置响应头
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", 
                "attachment; filename=\"" + fileRecord.getOriginalName() + "\"");
            
            // 写入响应流
            inputStream.transferTo(response.getOutputStream());
            response.flushBuffer();
            
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            response.setStatus(500);
        }
    }

    @Operation(summary = "获取文件信息")
    @GetMapping("/{fileId}")
    public Result<FileRecord> getFileRecord(
            @Parameter(description = "文件ID") @PathVariable Long fileId) {
        
        FileRecord fileRecord = fileService.getFileRecord(fileId);
        
        return ResultUtils.success(fileRecord);
    }

    @Operation(summary = "批量获取文件信息")
    @PostMapping("/batch")
    public Result<Map<Long, FileRecord>> getBatchFileRecords(
            @Parameter(description = "文件ID列表") @RequestBody List<Long> fileIds) {
        
        log.info("批量获取文件信息: {} 个文件", fileIds.size());
        
        Map<Long, FileRecord> fileRecords = fileService.getBatchFileRecords(fileIds);
        
        return ResultUtils.success(fileRecords);
    }

    @Operation(summary = "根据文件名获取文件信息")
    @GetMapping("/name/{fileName:.+}")
    public Result<FileRecord> getFileRecordByName(
            @Parameter(description = "文件名") @PathVariable String fileName) {
        
        FileRecord fileRecord = fileService.getFileRecordByName(fileName);
        
        return ResultUtils.success(fileRecord);
    }

    @Operation(summary = "检查文件是否存在")
    @GetMapping("/exists/{fileName:.+}")
    public Result<Boolean> fileExists(
            @Parameter(description = "文件名") @PathVariable String fileName) {
        
        boolean exists = fileService.fileExists(fileName);
        
        return ResultUtils.success(exists);
    }
}
