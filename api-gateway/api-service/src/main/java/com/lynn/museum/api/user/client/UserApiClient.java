package com.lynn.museum.api.user.client;

import com.lynn.museum.api.user.dto.RoleBasicInfo;
import com.lynn.museum.api.user.dto.UserBasicInfo;
import com.lynn.museum.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 用户服务API客户端
 * 
 * @author lynn
 * @since 2024-01-01
 */
@FeignClient(name = "user-service", path = "/api/v1/system")
public interface UserApiClient {
    
    /**
     * 根据用户名获取用户基础信息
     */
    @GetMapping("/users/username/{username}/basic")
    Result<UserBasicInfo> getUserByUsername(@PathVariable("username") String username);
    
    /**
     * 根据邮箱获取用户基础信息
     */
    @GetMapping("/users/email/{email}/basic")
    Result<UserBasicInfo> getUserByEmail(@PathVariable("email") String email);
    
    /**
     * 检查用户名是否存在（用于注册场景）
     * @param username 用户名
     * @return true-已存在，false-不存在
     */
    @GetMapping("/users/check-username/{username}")
    Result<Boolean> checkUsernameExists(@PathVariable("username") String username);
    
    /**
     * 检查邮箱是否存在（用于注册场景）
     * @param email 邮箱
     * @return true-已存在，false-不存在
     */
    @GetMapping("/users/check-email/{email}")
    Result<Boolean> checkEmailExists(@PathVariable("email") String email);
    
    /**
     * 根据用户ID获取用户基础信息
     */
    @GetMapping("/users/{userId}")
    Result<UserBasicInfo> getUserById(@PathVariable("userId") Long userId);
    
    /**
     * 创建用户
     */
    @PostMapping("/users")
    Result<UserBasicInfo> createUser(@RequestBody Map<String, Object> userInfo);
    
    /**
     * 获取用户权限列表
     */
    @GetMapping("/users/{userId}/permissions")
    Result<List<String>> getUserPermissions(@PathVariable("userId") Long userId);
    
    /**
     * 获取用户角色列表
     */
    @GetMapping("/users/{userId}/roles")
    Result<List<String>> getUserRoles(@PathVariable("userId") Long userId);
    
    /**
     * 验证用户状态
     */
    @GetMapping("/users/{userId}/verify")
    Result<Boolean> verifyUserStatus(@PathVariable("userId") Long userId);
    
    /**
     * 创建第三方登录用户（微信、支付宝等）
     */
    @PostMapping("/users/third-party")
    Result<UserBasicInfo> createThirdPartyUser(@RequestBody Map<String, Object> userInfo);
    
    /**
     * 分配用户角色
     */
    @PutMapping("/users/{userId}/roles")
    Result<Void> assignRoles(@PathVariable("userId") Long userId, @RequestBody List<Long> roleIds);
    
    /**
     * 根据角色编码查询角色信息
     */
    @GetMapping("/roles/code/{roleCode}")
    Result<RoleBasicInfo> getRoleByCode(@PathVariable("roleCode") String roleCode);
    
    /**
     * 重置用户密码
     */
    @PutMapping("/users/{userId}/password")
    Result<Void> resetPassword(@PathVariable("userId") Long userId, @RequestBody Map<String, String> passwordMap);
    
}