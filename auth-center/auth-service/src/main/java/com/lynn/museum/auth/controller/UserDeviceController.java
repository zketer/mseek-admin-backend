package com.lynn.museum.auth.controller;

import com.lynn.museum.auth.model.entity.UserDevice;
import com.lynn.museum.auth.service.UserDeviceService;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.common.web.utils.UserContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户设备管理 Controller
 * 
 * @author lynn
 * @since 2024-11-28
 */
@Slf4j
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@Tag(name = "UserDeviceController", description = "用户设备管理相关接口")
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    @GetMapping
    @Operation(summary = "获取当前用户的所有设备", description = "查询当前登录用户的所有设备列表")
    public Result<List<UserDevice>> getUserDevices() {
        try {
            Long userId = UserContextUtils.getCurrentUserId();
            List<UserDevice> devices = userDeviceService.getUserDevices(userId);
            return Result.success(devices);
        } catch (Exception e) {
            log.error("获取用户设备列表失败", e);
            return Result.error("获取设备列表失败");
        }
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "删除设备", description = "删除指定的设备（软删除）")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    public Result<Boolean> deleteDevice(@PathVariable Long deviceId) {
        try {
            Long userId = UserContextUtils.getCurrentUserId();
            boolean success = userDeviceService.deleteDevice(deviceId, userId);
            return Result.success(success);
        } catch (Exception e) {
            log.error("删除设备失败: deviceId={}", deviceId, e);
            return Result.error("删除设备失败");
        }
    }

    @PutMapping("/{deviceId}/disable")
    @Operation(summary = "禁用设备", description = "禁用指定的设备，该设备将无法登录")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    public Result<Boolean> disableDevice(@PathVariable Long deviceId) {
        try {
            Long userId = UserContextUtils.getCurrentUserId();
            boolean success = userDeviceService.disableDevice(deviceId, userId);
            return Result.success(success);
        } catch (Exception e) {
            log.error("禁用设备失败: deviceId={}", deviceId, e);
            return Result.error("禁用设备失败");
        }
    }
}
