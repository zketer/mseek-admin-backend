package com.lynn.museum.system.service;

import com.lynn.museum.system.dto.UserExportRequest;
import com.lynn.museum.system.dto.UserImportRequest;
import com.lynn.museum.system.dto.UserImportResult;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户导入导出服务接口
 * 
 * @author lynn
 * @since 2024-01-01
 */
public interface UserImportExportService {

    /**
     * 下载用户导入模板
     */
    ResponseEntity<Resource> downloadTemplate();

    /**
     * 导入用户
     */
    UserImportResult importUsers(MultipartFile file, UserImportRequest request);

    /**
     * 导出用户
     */
    void exportUsers(UserExportRequest request, HttpServletResponse response);

    /**
     * 导出所有用户
     */
    void exportAllUsers(HttpServletResponse response);

}
