package com.lynn.museum.system.controller;

import com.lynn.museum.common.result.Result;
import com.lynn.museum.system.dto.UserExportRequest;
import com.lynn.museum.system.dto.UserImportRequest;
import com.lynn.museum.system.dto.UserImportResult;
import com.lynn.museum.system.service.UserImportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * 用户导入导出控制器
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "UserImportExportController", description = "用户导入导出相关接口")
@RestController
@RequestMapping("/user-import-export")
@RequiredArgsConstructor
@Validated
public class UserImportExportController {

    private final UserImportExportService userImportExportService;

    @Operation(summary = "下载用户导入模板")
    @GetMapping("/template")
    public ResponseEntity<Resource> downloadTemplate() {
        return userImportExportService.downloadTemplate();
    }

    @Operation(summary = "导入用户")
    @PostMapping("/import")
    public Result<UserImportResult> importUsers(
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file,
            @Valid UserImportRequest request) {
        UserImportResult result = userImportExportService.importUsers(file, request);
        return Result.success(result);
    }

    @Operation(summary = "导出用户")
    @PostMapping("/export")
    public void exportUsers(
            @Valid @RequestBody UserExportRequest request,
            HttpServletResponse response) {
        userImportExportService.exportUsers(request, response);
    }

    @Operation(summary = "导出所有用户")
    @GetMapping("/export/all")
    public void exportAllUsers(HttpServletResponse response) {
        userImportExportService.exportAllUsers(response);
    }

    @Operation(summary = "根据条件导出用户")
    @GetMapping("/export/query")
    public void exportUsersByQuery(
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "昵称") @RequestParam(required = false) String nickname,
            @Parameter(description = "邮箱") @RequestParam(required = false) String email,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            HttpServletResponse response) {
        
        UserExportRequest request = new UserExportRequest();
        request.setUsername(username);
        request.setNickname(nickname);
        request.setEmail(email);
        request.setPhone(phone);
        request.setStatus(status);
        
        userImportExportService.exportUsers(request, response);
    }

}
