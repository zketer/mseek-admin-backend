package com.lynn.museum.auth.service;

import com.lynn.museum.auth.model.entity.UserDevice;

import java.util.List;

/**
 * 用户设备服务接口
 * 
 * @author lynn
 * @since 2024-11-28
 */
public interface UserDeviceService {

    /**
     * 绑定或更新设备
     * 
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     * @param deviceModel 设备型号
     * @param osVersion 操作系统版本
     * @param appVersion APP版本
     * @param platform 平台类型
     * @param refreshToken Refresh Token
     * @param loginIp 登录IP
     * @param loginLocation 登录地理位置
     * @return 设备信息
     */
    UserDevice bindOrUpdateDevice(Long userId, String deviceId, String deviceName, 
                                   String deviceModel, String osVersion, String appVersion,
                                   String platform, String refreshToken, 
                                   String loginIp, String loginLocation);

    /**
     * 根据 Refresh Token 查询设备
     * 
     * @param refreshToken Refresh Token
     * @return 设备信息
     */
    UserDevice getByRefreshToken(String refreshToken);

    /**
     * 查询用户的所有设备
     * 
     * @param userId 用户ID
     * @return 设备列表
     */
    List<UserDevice> getUserDevices(Long userId);

    /**
     * 更新设备最后活跃时间
     * 
     * @param deviceId 设备ID
     */
    void updateLastActiveTime(Long deviceId);

    /**
     * 禁用设备
     * 
     * @param deviceId 设备ID
     * @param userId 用户ID（用于权限验证）
     * @return 是否成功
     */
    boolean disableDevice(Long deviceId, Long userId);

    /**
     * 删除设备
     * 
     * @param deviceId 设备ID
     * @param userId 用户ID（用于权限验证）
     * @return 是否成功
     */
    boolean deleteDevice(Long deviceId, Long userId);

    /**
     * 检查用户设备数量是否超限
     * 
     * @param userId 用户ID
     * @param maxDevices 最大设备数
     * @return 是否超限
     */
    boolean isDeviceLimitExceeded(Long userId, int maxDevices);

    /**
     * 清理不活跃设备
     * 
     * @param inactiveDays 不活跃天数
     * @return 清理数量
     */
    int cleanInactiveDevices(int inactiveDays);
}
