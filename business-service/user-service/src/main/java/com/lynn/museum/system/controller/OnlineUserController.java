package com.lynn.museum.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.system.dto.OnlineUserQueryRequest;
import com.lynn.museum.system.dto.OnlineUserResponse;
import com.lynn.museum.system.service.OnlineUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 在线用户管理控制器
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "OnlineUserController", description = "在线用户管理相关接口")
@RestController
@RequestMapping("/online-users")
@RequiredArgsConstructor
@Validated
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    @Operation(summary = "分页查询在线用户列表")
    @GetMapping
    public Result<IPage<OnlineUserResponse>> getOnlineUsers(@Valid OnlineUserQueryRequest query) {
        IPage<OnlineUserResponse> page = onlineUserService.getOnlineUsers(query);
        return Result.success(page);
    }

    @Operation(summary = "获取在线用户统计")
    @GetMapping("/statistics")
    public Result<Object> getOnlineUserStatistics() {
        Object statistics = onlineUserService.getOnlineUserStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "强制下线用户")
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> forceLogout(
            @Parameter(description = "会话ID") @PathVariable @NotBlank String sessionId) {
        onlineUserService.forceLogout(sessionId);
        return Result.success();
    }

    @Operation(summary = "批量强制下线用户")
    @DeleteMapping("/sessions/batch")
    public Result<Void> batchForceLogout(
            @Parameter(description = "会话ID列表") @RequestBody @NotEmpty List<String> sessionIds) {
        onlineUserService.batchForceLogout(sessionIds);
        return Result.success();
    }

    @Operation(summary = "清理过期会话")
    @DeleteMapping("/sessions/expired")
    public Result<Integer> cleanExpiredSessions() {
        int count = onlineUserService.cleanExpiredSessions();
        return Result.success(count);
    }

    @Operation(summary = "根据用户ID强制下线")
    @DeleteMapping("/users/{userId}/sessions")
    public Result<Void> forceLogoutByUserId(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        onlineUserService.forceLogoutByUserId(userId);
        return Result.success();
    }

}
