package com.lynn.museum.auth.controller;

import com.lynn.museum.auth.dto.UserLoginInfoResponse;
import com.lynn.museum.auth.service.UserLoginInfoService;
import com.lynn.museum.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;

/**
 * 用户登录信息控制器
 * 提供用户登录信息的查询接口
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "UserLoginInfoController", description = "用户登录信息管理接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserLoginInfoController {

    private final UserLoginInfoService userLoginInfoService;

    @Operation(summary = "获取用户登录信息", description = "获取用户的最后登录时间、登录IP、登录次数等信息")
    @GetMapping("/{userId}/login-info")
    public Result<UserLoginInfoResponse> getUserLoginInfo(
            @Parameter(description = "用户ID") @PathVariable @NotNull Long userId) {
        log.info("【认证检查】auth-service收到获取用户登录信息请求: UserId={}", userId);
        
        try {
            UserLoginInfoResponse loginInfo = userLoginInfoService.getUserLoginInfo(userId);
            log.info("【认证检查】auth-service成功获取用户登录信息: UserId={}, HasData={}", userId, loginInfo != null);
            return Result.success(loginInfo);
        } catch (Exception e) {
            log.error("【认证检查】auth-service获取用户登录信息失败: UserId={}, Error={}", userId, e.getMessage(), e);
            throw e;
        }
    }
}
