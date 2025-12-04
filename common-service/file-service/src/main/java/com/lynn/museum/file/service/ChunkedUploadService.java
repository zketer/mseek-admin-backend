package com.lynn.museum.file.service;

import com.lynn.museum.file.entity.FileRecord;
import com.lynn.museum.file.enums.FileTypeEnum;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 分片上传服务接口
 * 
 * @author Lynn
 * @since 2025-11-25
 */
public interface ChunkedUploadService {

    /**
     * 初始化分片上传
     * 
     * @param fileName 原始文件名
     * @param fileSize 文件总大小
     * @param totalChunks 总分片数
     * @param fileType 文件类型字符串
     * @param uploaderId 上传者ID
     * @param md5Hash 文件MD5哈希值，用于秒传检查，可为null
     * @return 上传初始化结果，包含上传ID和是否秒传成功的标志
     */
    Map<String, Object> initMultipartUpload(String fileName, long fileSize, int totalChunks, String fileType, Long uploaderId, String md5Hash);

    /**
     * 上传分片
     * 
     * @param uploadId 上传ID
     * @param chunkNumber 分片序号（从1开始）
     * @param chunk 分片文件
     * @return 分片ETag
     */
    String uploadChunk(String uploadId, int chunkNumber, MultipartFile chunk);

    /**
     * 完成分片上传
     * 
     * @param uploadId 上传ID
     * @return 文件记录
     */
    FileRecord completeMultipartUpload(String uploadId);

    /**
     * 取消分片上传
     * 
     * @param uploadId 上传ID
     */
    void abortMultipartUpload(String uploadId);

    /**
     * 查询分片上传状态
     * 
     * @param uploadId 上传ID
     * @return 已上传的分片数
     */
    int getUploadStatus(String uploadId);
}
