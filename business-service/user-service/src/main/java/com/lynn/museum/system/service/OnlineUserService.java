package com.lynn.museum.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.system.dto.OnlineUserQueryRequest;
import com.lynn.museum.system.dto.OnlineUserResponse;

import java.util.List;

/**
 * 在线用户服务接口
 * 
 * @author lynn
 * @since 2024-01-01
 */
public interface OnlineUserService {

    /**
     * 分页查询在线用户列表
     */
    IPage<OnlineUserResponse> getOnlineUsers(OnlineUserQueryRequest query);

    /**
     * 获取在线用户统计
     */
    Object getOnlineUserStatistics();

    /**
     * 强制下线用户
     */
    void forceLogout(String sessionId);

    /**
     * 批量强制下线用户
     */
    void batchForceLogout(List<String> sessionIds);

    /**
     * 清理过期会话
     */
    int cleanExpiredSessions();

    /**
     * 根据用户ID强制下线
     */
    void forceLogoutByUserId(Long userId);

}
