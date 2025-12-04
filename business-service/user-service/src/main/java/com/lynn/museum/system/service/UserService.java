package com.lynn.museum.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.system.dto.UserBasicInfo;
import com.lynn.museum.system.dto.UserCreateRequest;
import com.lynn.museum.system.dto.UserQueryRequest;
import com.lynn.museum.system.dto.UserResponse;
import com.lynn.museum.system.dto.UserUpdateRequest;
import com.lynn.museum.system.model.entity.User;

import java.util.List;

/**
 * 用户服务接口
 * 
 * @author lynn
 * @since 2024-01-01
 */
public interface UserService {

    /**
     * 根据用户ID查询用户详细信息
     * 
     * @param id 用户主键ID，不能为null
     * @return 用户响应对象，包含用户的详细信息（不包含密码等敏感信息）
     * @throws com.lynn.museum.common.exception.BizException 当用户不存在时抛出异常
     * @since 1.0.0
     */
    UserResponse getById(Long id);

    /**
     * 根据用户名查询用户实体对象
     * 
     * @param username 用户名，不能为空，长度3-20个字符
     * @return 用户实体对象，如果用户不存在则返回null
     * @since 1.0.0
     */
    User getByUsername(String username);

    /**
     * 根据用户名查询用户响应对象
     * 
     * @param username 用户名，不能为空，长度3-20个字符
     * @return 用户响应对象，包含用户的详细信息（不包含密码等敏感信息）
     * @throws com.lynn.museum.common.exception.BizException 当用户不存在时抛出异常
     * @since 1.0.0
     */
    UserResponse getByUsernameResponse(String username);

    /**
     * 根据用户名查询用户基础信息
     * 用于内部服务调用，只返回必要的基础信息
     * 
     * @param username 用户名，不能为空，长度3-20个字符
     * @return 用户基础信息对象，包含ID、用户名、昵称等基础字段
     * @throws com.lynn.museum.common.exception.BizException 当用户不存在时抛出异常
     * @since 1.0.0
     */
    UserBasicInfo getUserBasicInfoByUsername(String username);

    /**
     * 根据邮箱查询用户基础信息
     * 用于内部服务调用，只返回必要的基础信息
     * 
     * @param email 邮箱，不能为空
     * @return 用户基础信息对象，如果用户不存在则返回null
     * @since 2025-01-01
     */
    UserBasicInfo getUserBasicInfoByEmail(String email);

    /**
     * 检查用户名是否存在（用于注册）
     * 不抛出异常，只返回是否存在的布尔值
     * 
     * @param username 用户名，不能为空
     * @return true-用户名已存在，false-用户名不存在（可以注册）
     * @since 2025-01-01
     */
    boolean checkUsernameExists(String username);

    /**
     * 检查邮箱是否存在（用于注册）
     * 不抛出异常，只返回是否存在的布尔值
     * 
     * @param email 邮箱，不能为空
     * @return true-邮箱已存在，false-邮箱不存在（可以注册）
     * @since 2025-01-01
     */
    boolean checkEmailExists(String email);

    /**
     * 分页查询用户列表
     * 支持按用户名、昵称、邮箱、状态等条件进行筛选
     * 
     * @param query 查询条件对象，包含分页参数和筛选条件
     * @return 分页结果对象，包含用户列表和分页信息
     * @since 1.0.0
     */
    IPage<UserResponse> getPage(UserQueryRequest query);

    /**
     * 创建新用户
     * 会自动对密码进行BCrypt加密处理
     * 
     * @param request 用户创建请求对象，包含用户名、密码、昵称等必要信息
     * @return 新创建用户的ID
     * @throws com.lynn.museum.common.exception.BizException 当用户名已存在或数据验证失败时抛出异常
     * @since 1.0.0
     */
    Long createUser(UserCreateRequest request);

    /**
     * 创建新用户并返回基础信息
     * 用于用户注册场景，会自动对密码进行BCrypt加密处理
     * 
     * @param userInfo 用户信息Map，包含username、password、email、nickname等字段
     * @return 新创建用户的基础信息
     * @throws com.lynn.museum.common.exception.BizException 当用户名已存在或数据验证失败时抛出异常
     * @since 2025-01-01
     */
    UserBasicInfo createUserWithBasicInfo(java.util.Map<String, Object> userInfo);

    /**
     * 更新用户信息
     * 不包含密码更新，密码更新请使用resetPassword方法
     * 
     * @param request 用户更新请求对象，包含要更新的用户信息
     * @throws com.lynn.museum.common.exception.BizException 当用户不存在或数据验证失败时抛出异常
     * @since 1.0.0
     */
    void updateUser(UserUpdateRequest request);

    /**
     * 删除用户（逻辑删除）
     * 使用逻辑删除，不会物理删除数据库记录
     * 
     * @param id 用户主键ID，不能为null
     * @throws com.lynn.museum.common.exception.BizException 当用户不存在时抛出异常
     * @since 1.0.0
     */
    void deleteUser(Long id);

    /**
     * 批量删除用户（逻辑删除）
     * 使用逻辑删除，不会物理删除数据库记录
     * 
     * @param ids 用户ID列表，不能为空，列表中的ID不能为null
     * @throws com.lynn.museum.common.exception.BizException 当任何一个用户不存在时抛出异常
     * @since 1.0.0
     */
    void deleteBatchUsers(List<Long> ids);

    /**
     * 更新用户状态（启用/禁用）
     * 
     * @param id 用户主键ID，不能为null
     * @param status 用户状态，0-禁用，1-启用
     * @throws com.lynn.museum.common.exception.BizException 当用户不存在或状态值无效时抛出异常
     * @since 1.0.0
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 重置用户密码
     * 新密码会自动进行BCrypt加密处理
     * 
     * @param id 用户主键ID，不能为null
     * @param newPassword 新密码，长度6-20个字符，不能为空
     * @throws com.lynn.museum.common.exception.BizException 当用户不存在或密码格式不正确时抛出异常
     * @since 1.0.0
     */
    void resetPassword(Long id, String newPassword);


    /**
     * 为用户分配角色
     * 会先清除用户现有的所有角色，再分配新的角色列表
     * 
     * @param userId 用户主键ID，不能为null
     * @param roleIds 角色ID列表，可以为空（表示清除所有角色）
     * @throws com.lynn.museum.common.exception.BizException 当用户不存在或角色不存在时抛出异常
     * @since 1.0.0
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 获取用户的角色编码列表
     * 
     * @param userId 用户主键ID，不能为null
     * @return 角色编码列表，如果用户没有角色则返回空列表
     * @throws com.lynn.museum.common.exception.BizException 当用户不存在时抛出异常
     * @since 1.0.0
     */
    List<String> getUserRoles(Long userId);

    /**
     * 获取用户的权限编码列表
     * 通过用户的角色获取所有权限，会自动去重
     * 
     * @param userId 用户主键ID，不能为null
     * @return 权限编码列表，如果用户没有权限则返回空列表
     * @throws com.lynn.museum.common.exception.BizException 当用户不存在时抛出异常
     * @since 1.0.0
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username, Long excludeId);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email, Long excludeId);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone, Long excludeId);

    /**
     * 获取用户统计信息
     */
    Object getUserStatistics();

    /**
     * 根据部门ID查询用户列表
     */
    List<UserResponse> getUsersByDeptId(Long deptId);

    /**
     * 根据角色ID查询用户列表
     */
    List<UserResponse> getUsersByRoleId(Long roleId);

    /**
     * 修改用户密码
     */
    void changePassword(Long id, String oldPassword, String newPassword);

    /**
     * 更新用户头像（Base64格式）
     * @param userId 用户ID
     * @param base64Avatar Base64格式的头像数据
     * @return 处理结果
     */
    Result<String> updateUserAvatar(Long userId, String base64Avatar);

    /**
     * 获取用户个人资料
     */
    Object getUserProfile(Long id);

    /**
     * 更新用户个人资料
     */
    void updateUserProfile(Long id, java.util.Map<String, Object> profileData);

    /**
     * 锁定用户
     */
    void lockUser(Long id, String reason);

    /**
     * 解锁用户
     */
    void unlockUser(Long id);

    /**
     * 批量导出用户数据到Excel
     * 
     * @param query 查询条件
     * @param response HTTP响应对象
     * @throws Exception 导出失败时抛出异常
     */
    void exportUsers(UserQueryRequest query, jakarta.servlet.http.HttpServletResponse response) throws Exception;

    /**
     * 批量导入用户数据从Excel
     * 
     * @param file Excel文件
     * @return 导入结果统计信息
     * @throws Exception 导入失败时抛出异常
     */
    java.util.Map<String, Object> importUsers(org.springframework.web.multipart.MultipartFile file) throws Exception;

    /**
     * 下载用户导入模板
     * 
     * @param response HTTP响应对象
     * @throws Exception 下载失败时抛出异常
     */
    void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) throws Exception;

    /**
     * 创建第三方登录用户
     * 
     * @param userInfo 第三方用户信息
     * @return 创建的用户基础信息
     */
    UserBasicInfo createThirdPartyUser(java.util.Map<String, Object> userInfo);

}