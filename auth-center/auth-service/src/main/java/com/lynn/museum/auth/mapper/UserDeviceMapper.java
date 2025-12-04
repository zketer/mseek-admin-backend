package com.lynn.museum.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.auth.model.entity.UserDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 用户设备 Mapper
 * 
 * @author lynn
 * @since 2024-11-28
 */
@Mapper
public interface UserDeviceMapper extends BaseMapper<UserDevice> {

    /**
     * 根据用户ID和设备ID查询设备
     * 
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 设备信息
     */
    UserDevice selectByUserIdAndDeviceId(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    /**
     * 根据 Refresh Token 查询设备
     * 
     * @param refreshToken Refresh Token
     * @return 设备信息
     */
    UserDevice selectByRefreshToken(@Param("refreshToken") String refreshToken);

    /**
     * 查询用户的所有设备
     * 
     * @param userId 用户ID
     * @return 设备列表
     */
    List<UserDevice> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的活跃设备数量
     * 
     * @param userId 用户ID
     * @return 活跃设备数量
     */
    int countActiveDevicesByUserId(@Param("userId") Long userId);

    /**
     * 更新设备最后活跃时间
     * 
     * @param id 设备ID
     * @param lastActiveTime 最后活跃时间
     * @return 更新行数
     */
    int updateLastActiveTime(@Param("id") Long id, @Param("lastActiveTime") Date lastActiveTime);

    /**
     * 禁用不活跃设备
     * 
     * @param inactiveDays 不活跃天数
     * @return 更新行数
     */
    int disableInactiveDevices(@Param("inactiveDays") int inactiveDays);
}
