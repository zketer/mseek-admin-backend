package com.lynn.museum.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lynn.museum.auth.mapper.UserDeviceMapper;
import com.lynn.museum.auth.model.entity.UserDevice;
import com.lynn.museum.auth.service.UserDeviceService;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 用户设备服务实现类
 * 
 * @author lynn
 * @since 2024-11-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDeviceServiceImpl implements UserDeviceService {

    private final UserDeviceMapper userDeviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDevice bindOrUpdateDevice(Long userId, String deviceId, String deviceName,
                                          String deviceModel, String osVersion, String appVersion,
                                          String platform, String refreshToken,
                                          String loginIp, String loginLocation) {
        // 查询设备是否已存在
        UserDevice existingDevice = userDeviceMapper.selectByUserIdAndDeviceId(userId, deviceId);

        if (existingDevice != null) {
            // 更新现有设备
            existingDevice.setDeviceName(deviceName);
            existingDevice.setDeviceModel(deviceModel);
            existingDevice.setOsVersion(osVersion);
            existingDevice.setAppVersion(appVersion);
            existingDevice.setPlatform(platform);
            existingDevice.setRefreshToken(refreshToken);
            existingDevice.setLastActiveTime(new Date());
            existingDevice.setLoginIp(loginIp);
            existingDevice.setLoginLocation(loginLocation);
            existingDevice.setStatus(1); // 重新激活
            existingDevice.setUpdateAt(new Date());
            existingDevice.setUpdateBy(userId);

            userDeviceMapper.updateById(existingDevice);
            
            log.info("✅ 更新设备信息: userId={}, deviceId={}, deviceName={}", 
                     userId, deviceId, deviceName);
            
            return existingDevice;
        } else {
            // 创建新设备
            UserDevice newDevice = new UserDevice();
            newDevice.setUserId(userId);
            newDevice.setDeviceId(deviceId);
            newDevice.setDeviceName(deviceName);
            newDevice.setDeviceModel(deviceModel);
            newDevice.setOsVersion(osVersion);
            newDevice.setAppVersion(appVersion);
            newDevice.setPlatform(platform);
            newDevice.setRefreshToken(refreshToken);
            newDevice.setLastActiveTime(new Date());
            newDevice.setLoginIp(loginIp);
            newDevice.setLoginLocation(loginLocation);
            newDevice.setStatus(1);
            newDevice.setCreateAt(new Date());
            newDevice.setUpdateAt(new Date());
            newDevice.setCreateBy(userId);
            newDevice.setUpdateBy(userId);
            newDevice.setDeleted(0);

            userDeviceMapper.insert(newDevice);
            
            log.info("✅ 绑定新设备: userId={}, deviceId={}, deviceName={}", 
                     userId, deviceId, deviceName);
            
            return newDevice;
        }
    }

    @Override
    public UserDevice getByRefreshToken(String refreshToken) {
        return userDeviceMapper.selectByRefreshToken(refreshToken);
    }

    @Override
    public List<UserDevice> getUserDevices(Long userId) {
        return userDeviceMapper.selectByUserId(userId);
    }

    @Override
    public void updateLastActiveTime(Long deviceId) {
        userDeviceMapper.updateLastActiveTime(deviceId, new Date());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableDevice(Long deviceId, Long userId) {
        // 查询设备
        UserDevice device = userDeviceMapper.selectById(deviceId);
        
        if (device == null || device.getDeleted() == 1) {
            throw new BizException(ResultCode.DATA_NOT_FOUND);
        }
        
        // 验证设备所有权
        if (!device.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        
        // 禁用设备
        LambdaUpdateWrapper<UserDevice> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserDevice::getId, deviceId)
                    .set(UserDevice::getStatus, 0)
                    .set(UserDevice::getUpdateAt, new Date())
                    .set(UserDevice::getUpdateBy, userId);
        
        int rows = userDeviceMapper.update(null, updateWrapper);
        
        log.info("✅ 禁用设备: deviceId={}, userId={}", deviceId, userId);
        
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDevice(Long deviceId, Long userId) {
        // 查询设备
        UserDevice device = userDeviceMapper.selectById(deviceId);
        
        if (device == null || device.getDeleted() == 1) {
            throw new BizException(ResultCode.DATA_NOT_FOUND);
        }
        
        // 验证设备所有权
        if (!device.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        
        // 软删除设备
        LambdaUpdateWrapper<UserDevice> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserDevice::getId, deviceId)
                    .set(UserDevice::getDeleted, 1)
                    .set(UserDevice::getUpdateAt, new Date())
                    .set(UserDevice::getUpdateBy, userId);
        
        int rows = userDeviceMapper.update(null, updateWrapper);
        
        log.info("✅ 删除设备: deviceId={}, userId={}", deviceId, userId);
        
        return rows > 0;
    }

    @Override
    public boolean isDeviceLimitExceeded(Long userId, int maxDevices) {
        if (maxDevices <= 0) {
            // 0 表示不限制
            return false;
        }
        
        int activeDeviceCount = userDeviceMapper.countActiveDevicesByUserId(userId);
        return activeDeviceCount >= maxDevices;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanInactiveDevices(int inactiveDays) {
        int rows = userDeviceMapper.disableInactiveDevices(inactiveDays);
        
        if (rows > 0) {
            log.info("✅ 清理不活跃设备: 清理数量={}, 不活跃天数={}", rows, inactiveDays);
        }
        
        return rows;
    }
}
