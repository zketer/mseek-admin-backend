package com.lynn.museum.api.file.client;

import com.lynn.museum.api.file.dto.BatchFileUrlRequest;
import com.lynn.museum.api.file.dto.FileUrlResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文件服务API客户端
 *
 * @author lynn
 * @since 2024-01-01
 */
@FeignClient(
    name = "file-service",
    path = "/api/v1/files"
)
public interface FileApiClient {

    /**
     * 获取单个文件的访问URL
     *
     * @param fileId 文件ID
     * @return 文件URL
     */
    @GetMapping("/url/{fileId}")
    Map<String, Object> getFileUrl(@PathVariable("fileId") Long fileId);

    /**
     * 批量获取文件访问URL
     *
     * @param fileIds 文件ID列表
     * @return 文件URL列表
     */
    @PostMapping("/urls/batch")
    Map<String, Object> getBatchFileUrls(@RequestBody List<Long> fileIds);

    /**
     * 获取文件强制下载URL
     * 返回的URL会强制浏览器下载而不是预览
     *
     * @param fileId 文件ID
     * @return 下载URL
     */
    @GetMapping("/download-url/{fileId}")
    Map<String, Object> getFileDownloadUrl(@PathVariable("fileId") Long fileId);

    /**
     * 获取文件详细信息（包含文件大小等）
     *
     * @param fileId 文件ID
     * @return 文件详细信息
     */
    @GetMapping("/{fileId}")
    Map<String, Object> getFileInfo(@PathVariable("fileId") Long fileId);

    /**
     * 批量获取文件详细信息
     *
     * @param fileIds 文件ID列表
     * @return 文件详细信息Map，key为文件ID
     */
    @PostMapping("/batch")
    Map<String, Object> getBatchFileInfo(@RequestBody List<Long> fileIds);
}
