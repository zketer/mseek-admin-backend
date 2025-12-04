package com.lynn.museum.system.service.impl;

import com.lynn.museum.system.dto.UserExportRequest;
import com.lynn.museum.system.dto.UserImportRequest;
import com.lynn.museum.system.dto.UserImportResult;
import com.lynn.museum.system.service.UserImportExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户导入导出服务实现类
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserImportExportServiceImpl implements UserImportExportService {

    @Override
    public ResponseEntity<Resource> downloadTemplate() {
        // TODO: 实现下载用户导入模板
        log.info("下载用户导入模板");
        return ResponseEntity.ok().build();
    }

    @Override
    public UserImportResult importUsers(MultipartFile file, UserImportRequest request) {
        // TODO: 实现导入用户
        log.info("导入用户: {}", file.getOriginalFilename());
        UserImportResult result = new UserImportResult();
        result.setSuccess(true);
        result.setMessage("导入功能待实现");
        return result;
    }

    @Override
    public void exportUsers(UserExportRequest request, HttpServletResponse response) {
        // TODO: 实现导出用户
        log.info("导出用户");
    }

    @Override
    public void exportAllUsers(HttpServletResponse response) {
        // TODO: 实现导出所有用户
        log.info("导出所有用户");
    }
}
