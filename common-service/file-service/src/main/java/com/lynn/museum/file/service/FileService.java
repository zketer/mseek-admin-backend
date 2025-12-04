package com.lynn.museum.file.service;

import com.lynn.museum.file.entity.FileRecord;
import com.lynn.museum.file.enums.FileTypeEnum;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.List;

/**
 * 文件服务接口
 * 
 * @author Lynn
 * @since 2024-01-01
 */
public interface FileService {

    /**
     * 上传文件
     * 
     * @param file 文件
     * @param fileType 文件类型
     * @param uploaderId 上传者ID
     * @return 文件记录
     */
    FileRecord uploadFile(MultipartFile file, FileTypeEnum fileType, Long uploaderId);

    /**
     * 批量上传文件
     * 
     * @param files 文件数组
     * @param fileType 文件类型
     * @param uploaderId 上传者ID
     * @return 文件记录列表
     */
    List<FileRecord> uploadFiles(MultipartFile[] files, FileTypeEnum fileType, Long uploaderId);

    /**
     * 删除文件
     * 
     * @param fileId 文件ID
     */
    void deleteFile(Long fileId);

    /**
     * 删除文件（根据文件名）
     * 
     * @param fileName 文件名
     */
    void deleteFileByName(String fileName);

    /**
     * 获取文件访问URL
     * 
     * @param fileId 文件ID
     * @return 访问URL
     */
    String getFileUrl(Long fileId);

    /**
     * 批量获取文件访问URL
     * 
     * @param fileIds 文件ID列表
     * @return 文件URL信息列表
     */
    List<Map<String, Object>> getBatchFileUrls(List<Long> fileIds);

    /**
     * 获取文件访问URL（根据文件名）
     * 
     * @param fileName 文件名
     * @return 访问URL
     */
    String getFileUrlByName(String fileName);

    /**
     * 获取文件强制下载URL
     * 返回的URL会强制浏览器下载而不是预览
     * 
     * @param fileId 文件ID
     * @return 下载URL
     */
    String getFileDownloadUrl(Long fileId);

    /**
     * 获取文件强制下载URL（根据文件名）
     * 返回的URL会强制浏览器下载而不是预览
     * 
     * @param fileName 文件名
     * @return 下载URL
     */
    String getFileDownloadUrlByName(String fileName);

    /**
     * 下载文件
     * 
     * @param fileId 文件ID
     * @return 文件流
     */
    InputStream downloadFile(Long fileId);

    /**
     * 下载文件（根据文件名）
     * 
     * @param fileName 文件名
     * @return 文件流
     */
    InputStream downloadFileByName(String fileName);

    /**
     * 根据ID获取文件记录
     * 
     * @param fileId 文件ID
     * @return 文件记录
     */
    FileRecord getFileRecord(Long fileId);

    /**
     * 批量获取文件记录
     * 
     * @param fileIds 文件ID列表
     * @return 文件记录Map，key为文件ID，value为文件记录
     */
    Map<Long, FileRecord> getBatchFileRecords(List<Long> fileIds);

    /**
     * 根据文件名获取文件记录
     * 
     * @param fileName 文件名
     * @return 文件记录
     */
    FileRecord getFileRecordByName(String fileName);

    /**
     * 检查文件是否存在
     * 
     * @param fileName 文件名
     * @return 是否存在
     */
    boolean fileExists(String fileName);
}
