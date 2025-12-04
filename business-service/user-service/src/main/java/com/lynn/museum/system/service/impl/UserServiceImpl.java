package com.lynn.museum.system.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lynn.museum.api.auth.client.AuthApiClient;
import com.lynn.museum.api.auth.dto.UserLoginInfo;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.common.result.ResultCode;
import com.lynn.museum.common.utils.PasswordUtils;
import com.lynn.museum.system.dto.UserBasicInfo;
import com.lynn.museum.system.dto.UserCreateRequest;
import com.lynn.museum.system.dto.UserQueryRequest;
import com.lynn.museum.system.dto.UserResponse;
import com.lynn.museum.system.dto.UserUpdateRequest;
import com.lynn.museum.system.model.entity.User;
import com.lynn.museum.system.model.entity.Role;
import com.lynn.museum.system.model.entity.UserRole;
import com.lynn.museum.system.mapper.UserMapper;
import com.lynn.museum.system.mapper.RoleMapper;
import com.lynn.museum.system.mapper.UserRoleMapper;
import com.lynn.museum.system.mapper.PermissionMapper;
import com.lynn.museum.system.service.UserService;
import com.lynn.museum.system.dto.UserExcelDto;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private PermissionMapper permissionMapper;
    private final AuthApiClient authApiClient;

    @Override
    public UserResponse getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        return convertToResponse(user);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public UserResponse getByUsernameResponse(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        return convertToResponse(user);
    }

    @Override
    public UserBasicInfo getUserBasicInfoByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        UserBasicInfo basicInfo = new UserBasicInfo();
        BeanUtils.copyProperties(user, basicInfo);
        return basicInfo;
    }

    @Override
    public boolean checkUsernameExists(String username) {
        User user = userMapper.selectByUsername(username);
        return user != null;
    }

    @Override
    public boolean checkEmailExists(String email) {
        User user = userMapper.selectByEmail(email);
        return user != null;
    }

    @Override
    public IPage<UserResponse> getPage(UserQueryRequest query) {
        // 使用自定义分页查询
        List<User> users = userMapper.selectPage(query);
        Long total = userMapper.selectCount(query);

        // 转换为响应对象
        List<UserResponse> records = users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        // 创建新的Page对象并设置转换后的记录
        Page<UserResponse> responsePage = new Page<>(query.getPageNum(), query.getPageSize(), total);
        responsePage.setRecords(records);

        return responsePage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateRequest request) {
        // 检查用户名是否已存在（包括软删除的用户）
        if (userMapper.selectByUsernameIncludeDeleted(request.getUsername()) != null) {
            throw new BizException(ResultCode.USER_ALREADY_EXISTS);
        }

        // 检查邮箱是否已存在（包括软删除的用户）
        if (request.getEmail() != null && userMapper.selectByEmailIncludeDeleted(request.getEmail()) != null) {
            throw new BizException(ResultCode.USER_EMAIL_ALREADY_EXISTS);
        }

        // 检查手机号是否已存在（包括软删除的用户）
        if (request.getPhone() != null && userMapper.selectByPhoneIncludeDeleted(request.getPhone()) != null) {
            throw new BizException(ResultCode.USER_PHONE_ALREADY_EXISTS);
        }

        // 创建用户对象
        User user = new User();
        BeanUtils.copyProperties(request, user);
        // 使用统一的密码加密工具
        user.setPassword(PasswordUtils.encode(request.getPassword()));
        user.setCreateAt(new Date());
        user.setUpdateAt(new Date());
        // 默认启用状态
        user.setStatus(1);

        // 插入用户
        userMapper.insert(user);

        // 分配角色
        if (!CollectionUtils.isEmpty(request.getRoleIds())) {
            assignRoles(user.getId(), request.getRoleIds());
        }

        log.info("创建用户成功，用户ID: {}", user.getId());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateRequest request) {
        User user = userMapper.selectById(request.getId());
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }

        // 检查用户名是否已存在（包括软删除的用户）
        if (!user.getUsername().equals(request.getUsername()) && 
            userMapper.selectByUsernameIncludeDeleted(request.getUsername()) != null) {
            throw new BizException(ResultCode.USER_ALREADY_EXISTS);
        }

        // 检查邮箱是否已存在（包括软删除的用户）
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail()) &&
            userMapper.selectByEmailIncludeDeleted(request.getEmail()) != null) {
            throw new BizException(ResultCode.USER_EMAIL_ALREADY_EXISTS);
        }

        // 检查手机号是否已存在（包括软删除的用户）
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone()) &&
            userMapper.selectByPhoneIncludeDeleted(request.getPhone()) != null) {
            throw new BizException(ResultCode.PHONE_ALREADY_EXISTS);
        }

        // 更新用户信息
        BeanUtils.copyProperties(request, user);
        user.setUpdateAt(new Date());

        userMapper.updateById(user);

        // 更新角色分配
        if (request.getRoleIds() != null) {
            assignRoles(user.getId(), request.getRoleIds());
        }

        log.info("更新用户成功，用户ID: {}", user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }

        userMapper.deleteById(id);

        log.info("删除用户成功，用户ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatchUsers(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        userMapper.deleteBatchIds(ids);

        log.info("批量删除用户成功，用户ID列表: {}", ids);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }

        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setStatus(status);
        updateUser.setUpdateAt(new Date());

        userMapper.updateById(updateUser);

        log.info("更新用户状态成功，用户ID: {}, 状态: {}", id, status);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        
        User updateUser = new User();
        updateUser.setId(id);
        // 使用统一的密码加密工具
        updateUser.setPassword(PasswordUtils.encode(newPassword));
        updateUser.setUpdateAt(new Date());
        
        userMapper.updateById(updateUser);
        
        log.info("重置用户密码成功，用户ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        if (userId == null) {
            throw new BizException(ResultCode.INVALID_USER_ID);
        }
        
        // 先删除用户现有的角色关联
        userRoleMapper.deleteByUserId(userId);
        
        // 如果角色ID列表为空，直接返回
        if (CollectionUtils.isEmpty(roleIds)) {
            log.info("清除用户角色成功，用户ID: {}", userId);
            return;
        }
        
        // 创建用户角色关联
        List<UserRole> userRoles = roleIds.stream()
                .map(roleId -> {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    userRole.setCreateAt(new Date());
                    // 使用当前用户ID作为创建者
                    userRole.setCreateBy(userId);
                    return userRole;
                })
                .collect(Collectors.toList());
        
        // 批量插入用户角色关联
        userRoleMapper.insertBatch(userRoles);
        
        log.info("分配用户角色成功，用户ID: {}, 角色ID列表: {}", userId, roleIds);
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        if (userId == null) {
            throw new BizException(ResultCode.INVALID_USER_ID);
        }
        
        // 查询用户角色ID列表
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return List.of();
        }
        
        // 根据角色ID查询角色编码
        return roleMapper.selectRoleCodesByIds(roleIds);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        if (userId == null) {
            return List.of();
        }
        
        try {
            // 通过用户ID查询权限编码列表
            return permissionMapper.selectPermissionCodesByUserId(userId);
        } catch (Exception e) {
            log.error("查询用户权限失败: userId={}", userId, e);
            return List.of();
        }
    }

    @Override
    public boolean existsByUsername(String username, Long excludeId) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username);
        if (excludeId != null) {
            queryWrapper.ne(User::getId, excludeId);
        }
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByEmail(String email, Long excludeId) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getEmail, email);
        if (excludeId != null) {
            queryWrapper.ne(User::getId, excludeId);
        }
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByPhone(String phone, Long excludeId) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getPhone, phone);
        if (excludeId != null) {
            queryWrapper.ne(User::getId, excludeId);
        }
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public Object getUserStatistics() {
        // TODO: 实现用户统计功能
        return "用户统计功能待实现";
    }

    @Override
    public List<UserResponse> getUsersByDeptId(Long deptId) {
        // TODO: 实现根据部门ID查询用户
        return List.of();
    }

    @Override
    public List<UserResponse> getUsersByRoleId(Long roleId) {
        // TODO: 实现根据角色ID查询用户
        return List.of();
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        log.info("修改用户{}密码", id);

        // 1. 查询用户
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 验证旧密码
        if (!PasswordUtils.matches(oldPassword, user.getPassword())) {
            throw new BizException(ResultCode.OLD_PASSWORD_INCORRECT);
        }

        // 3. 验证新密码是否与旧密码相同
        if (PasswordUtils.matches(newPassword, user.getPassword())) {
            throw new BizException(ResultCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        // 4. 更新密码
        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setPassword(PasswordUtils.encode(newPassword));
        updateUser.setUpdateAt(new Date());

        int updateResult = userMapper.updateById(updateUser);
        if (updateResult <= 0) {
            throw new BizException(ResultCode.FAILED_TO_UPDATE_PASSWORD);
        }

        log.info("用户{}密码修改成功", id);
    }

    /*
     * 用户头像上传（文件上传方法）- 已废弃
     * 统一使用 updateUserAvatar 方法（Base64）
     *
     * @deprecated 使用 updateUserAvatar 方法替代
     */
    /*
    @Override
    public String uploadAvatar(Long id, org.springframework.web.multipart.MultipartFile file) {
        // 检查用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        
        try {
            // 转换为Base64
            String base64Avatar = convertToBase64(file);
            
            // 更新用户头像
            User updateUser = new User();
            updateUser.setId(id);
            updateUser.setAvatar(base64Avatar);
            updateUser.setUpdateAt(new Date());
            
            userMapper.updateById(updateUser);
            
            log.info("用户头像上传成功，用户ID: {}", id);
            return base64Avatar;
        } catch (Exception e) {
            log.error("用户头像上传失败，用户ID: {}, 错误: {}", id, e.getMessage());
            throw new BizException(ResultCode.FAILED_TO_UPLOAD_AVATAR);
        }
    }
    */
//
//    /**
//     * 压缩图片
//     * @param imageData 原始图片数据
//     * @param format 图片格式
//     * @return 压缩后的图片数据
//     */
//    private byte[] compressImage(byte[] imageData, String format) {
//        try {
//            // 将字节数组转换为图片对象
//            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(imageData);
//            java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(inputStream);
//
//            // 如果图片太大，进行缩放
//            int maxWidth = 300;
//            int maxHeight = 300;
//            int originalWidth = originalImage.getWidth();
//            int originalHeight = originalImage.getHeight();
//
//            // 如果图片尺寸已经小于最大尺寸，则不需要缩放
//            if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
//                return imageData;
//            }
//
//            // 计算缩放比例
//            double scale = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
//            int scaledWidth = (int) (originalWidth * scale);
//            int scaledHeight = (int) (originalHeight * scale);
//
//            // 创建缩放后的图片
//            java.awt.image.BufferedImage scaledImage = new java.awt.image.BufferedImage(scaledWidth, scaledHeight, originalImage.getType());
//            java.awt.Graphics2D g2d = scaledImage.createGraphics();
//            g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
//            g2d.dispose();
//
//            // 将缩放后的图片转换为字节数组
//            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
//            javax.imageio.ImageIO.write(scaledImage, format, outputStream);
//
//            return outputStream.toByteArray();
//        } catch (Exception e) {
//            log.error("压缩图片失败: {}", e.getMessage());
//            return imageData; // 如果压缩失败，返回原始图片数据
//        }
//    }
    
//    @Override
//    public String convertToBase64(org.springframework.web.multipart.MultipartFile file) throws Exception {
//        try {
//            // 获取文件名和扩展名
//            String originalFilename = file.getOriginalFilename();
//            if (originalFilename == null || originalFilename.isEmpty()) {
//                throw new BizException(ResultCode.FILE_NAME_EMPTY);
//            }
//
//            // 检查文件大小（限制为2MB）
//            long maxSize = 2 * 1024 * 1024; // 2MB
//            if (file.getSize() > maxSize) {
//                throw new BizException(ResultCode.FILE_TOO_LARGE);
//            }
//
//            // 读取图片数据
//            byte[] imageData = file.getBytes();
//
//            // 压缩图片（如果需要）
//            byte[] compressedImageData = compressImage(imageData, getFileExtension(originalFilename));
//
//            // 使用Base64编码作为头像字符串
//            return "data:image/" + getFileExtension(originalFilename) + ";base64," +
//                   java.util.Base64.getEncoder().encodeToString(compressedImageData);
//        } catch (Exception e) {
//            log.error("转换头像为Base64失败: {}", e.getMessage());
//            throw e;
//        }
//    }
//
//    /**
//     * 获取文件扩展名
//     */
//    private String getFileExtension(String filename) {
//        int dotIndex = filename.lastIndexOf('.');
//        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
//            String extension = filename.substring(dotIndex + 1).toLowerCase();
//            // 只允许常见图片格式
//            if (extension.equals("jpg") || extension.equals("jpeg") ||
//                extension.equals("png") || extension.equals("gif")) {
//                return extension;
//            }
//        }
//        // 默认返回jpeg
//        return "jpeg";
//    }

    @Override
    public Result<String> updateUserAvatar(Long userId, String base64Avatar) {
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        
        try {
            // 更新用户头像
            User updateUser = new User();
            updateUser.setId(userId);
            updateUser.setAvatar(base64Avatar);
            updateUser.setUpdateAt(new Date());
            
            userMapper.updateById(updateUser);
            
            log.info("用户头像更新成功（Base64），用户ID: {}", userId);
            return Result.success(base64Avatar);
        } catch (Exception e) {
            log.error("用户头像更新失败（Base64），用户ID: {}, 错误: {}", userId, e.getMessage());
            throw new BizException(ResultCode.FAILED_TO_UPDATE_AVATAR);
        }
    }
    
    @Override
    public Object getUserProfile(Long id) {
        // 获取用户基本信息
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        
        // 构建个人资料响应对象
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("nickname", user.getNickname());
        profile.put("avatar", user.getAvatar());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("gender", user.getGender());
        profile.put("birthday", user.getBirthday());
        profile.put("status", user.getStatus());
        profile.put("createAt", user.getCreateAt());
        profile.put("updateAt", user.getUpdateAt());
        
        log.info("获取用户个人资料成功，用户ID: {}", id);
        return profile;
    }

    @Override
    public void updateUserProfile(Long id, java.util.Map<String, Object> profileData) {
        // 检查用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        
        // 构建更新对象
        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setUpdateAt(new Date());
        
        // 更新允许修改的字段
        if (profileData.containsKey("nickname")) {
            updateUser.setNickname((String) profileData.get("nickname"));
        }
        if (profileData.containsKey("email")) {
            updateUser.setEmail((String) profileData.get("email"));
        }
        if (profileData.containsKey("phone")) {
            updateUser.setPhone((String) profileData.get("phone"));
        }
        if (profileData.containsKey("gender")) {
            updateUser.setGender((Integer) profileData.get("gender"));
        }
        if (profileData.containsKey("birthday")) {
            Object birthdayObj = profileData.get("birthday");
            if (birthdayObj instanceof String) {
                try {
                    LocalDate birthday = LocalDate.parse((String) birthdayObj);
                    updateUser.setBirthday(birthday);
                } catch (Exception e) {
                    log.warn("生日日期格式解析失败: {}", birthdayObj);
                }
            }
        }
        
        // 执行更新
        userMapper.updateById(updateUser);
        log.info("更新用户个人资料成功，用户ID: {}", id);
    }

    @Override
    public void lockUser(Long id, String reason) {
        // TODO: 实现锁定用户功能
        log.info("锁定用户{}，原因：{}", id, reason);
    }

    @Override
    public void unlockUser(Long id) {
        // TODO: 实现解锁用户功能
        log.info("解锁用户{}", id);
    }

    /**
     * 转换User实体为UserResponse
     */
    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        
        // 填充角色信息
        if (user.getId() != null) {
            List<Role> roles = roleMapper.selectByUserId(user.getId());
            if (!CollectionUtils.isEmpty(roles)) {
                List<UserResponse.RoleInfo> roleInfos = roles.stream()
                        .map(role -> {
                            UserResponse.RoleInfo roleInfo = new UserResponse.RoleInfo();
                            roleInfo.setRoleId(role.getId());
                            roleInfo.setRoleName(role.getRoleName());
                            roleInfo.setRoleCode(role.getRoleCode());
                            return roleInfo;
                        })
                        .collect(Collectors.toList());
                response.setRoles(roleInfos);
            }
            
            // 从认证服务获取登录信息
            try {
                log.info("【认证检查】user-service准备调用auth-service获取用户登录信息: UserId={}", user.getId());
                
                Result<UserLoginInfo> loginInfoResult = authApiClient.getUserLoginInfo(user.getId());
                
                log.info("【认证检查】auth-service调用结果: UserId={}, Success={}, HasData={}", 
                    user.getId(), 
                    loginInfoResult != null && loginInfoResult.isSuccess(),
                    loginInfoResult != null && loginInfoResult.getData() != null);
                
                if (loginInfoResult != null && loginInfoResult.isSuccess() && loginInfoResult.getData() != null) {
                    UserLoginInfo loginInfo = loginInfoResult.getData();
                    // User实体已移除lastLoginTime等字段,这里从登录信息设置到响应
                    response.setLastLoginIp(loginInfo.getLastLoginIp());
                    response.setLoginCount(loginInfo.getLoginCount());
                    log.info("【认证检查】用户登录信息获取成功: UserId={}", user.getId());
                } else {
                    log.warn("【认证检查】auth-service返回无效结果: UserId={}, Result={}", user.getId(), loginInfoResult);
                }
            } catch (Exception e) {
                log.error("【认证检查】获取用户{}登录信息失败: 异常类型={}, 错误信息={}", user.getId(), e.getClass().getSimpleName(), e.getMessage());
                // 登录信息获取失败不影响主要用户信息的返回
            }
        }
        
        return response;
    }

    @Override
    public void exportUsers(UserQueryRequest query, HttpServletResponse response) throws Exception {
        // 查询用户数据
        List<User> users = userMapper.selectPage(query);
        
        // 转换为Excel DTO
        List<UserExcelDto> excelData = users.stream().map(this::convertToExcelDto).collect(Collectors.toList());
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = java.net.URLEncoder.encode("用户数据_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        // 写入Excel
        EasyExcel.write(response.getOutputStream(), UserExcelDto.class)
                .sheet("用户数据")
                .doWrite(excelData);
        
        log.info("导出用户数据成功，数量: {}", excelData.size());
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> importUsers(MultipartFile file) throws Exception {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        java.util.List<String> errorMessages = new java.util.ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        
        // 读取Excel数据
        EasyExcel.read(file.getInputStream(), UserExcelDto.class, new PageReadListener<UserExcelDto>(dataList -> {
            for (UserExcelDto excelDto : dataList) {
                try {
                    // 验证和转换数据
                    validateAndConvertExcelDto(excelDto);
                    
                    // 创建用户
                    UserCreateRequest createRequest = convertToCreateRequest(excelDto);
                    createUser(createRequest);
                    
                    result.put("successCount", (Integer) result.getOrDefault("successCount", 0) + 1);
                } catch (Exception e) {
                    result.put("errorCount", (Integer) result.getOrDefault("errorCount", 0) + 1);
                    @SuppressWarnings("unchecked")
                    java.util.List<String> errors = (java.util.List<String>) result.computeIfAbsent("errorMessages", k -> new java.util.ArrayList<String>());
                    errors.add("用户名: " + excelDto.getUsername() + ", 错误: " + e.getMessage());
                }
            }
        })).sheet().doRead();
        
        log.info("导入用户数据完成，成功: {}, 失败: {}", result.get("successCount"), result.get("errorCount"));
        return result;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws Exception {
        // 创建模板数据
        java.util.List<UserExcelDto> templateData = java.util.Arrays.asList(
            createTemplateRow("admin", "管理员", "admin@example.com", "13800138000", "男", "1990-01-01", "启用", "系统管理员"),
            createTemplateRow("user001", "张三", "zhangsan@example.com", "13800138001", "男", "1992-05-15", "启用", "普通用户"),
            createTemplateRow("user002", "李四", "lisi@example.com", "13800138002", "女", "1988-10-20", "启用", "普通用户")
        );
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = java.net.URLEncoder.encode("用户导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        // 写入Excel
        EasyExcel.write(response.getOutputStream(), UserExcelDto.class)
                .sheet("用户导入模板")
                .doWrite(templateData);
        
        log.info("下载用户导入模板成功");
    }

    /**
     * 将User实体转换为Excel DTO
     */
    private UserExcelDto convertToExcelDto(User user) {
        UserExcelDto dto = new UserExcelDto();
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setBirthday(user.getBirthday());
        dto.setRemark(user.getRemark());
        dto.setCreateTime(user.getCreateAt() != null ? user.getCreateAt().toString() : "");
        
        // 转换性别
        if (user.getGender() != null) {
            switch (user.getGender()) {
                case 1: dto.setGenderName("男"); break;
                case 2: dto.setGenderName("女"); break;
                default: dto.setGenderName("保密"); break;
            }
        }
        
        // 转换状态
        if (user.getStatus() != null) {
            dto.setStatusName(user.getStatus() == 1 ? "启用" : "禁用");
        }
        
        return dto;
    }

    /**
     * 验证和转换Excel DTO中的数据
     */
    private void validateAndConvertExcelDto(UserExcelDto dto) {
        // 验证必填字段
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new BizException(ResultCode.PARAM_MISSING.getCode(), "用户名不能为空");
        }
        
        // 转换性别
        if (dto.getGenderName() != null) {
            switch (dto.getGenderName().trim()) {
                case "男": dto.setGender(1); break;
                case "女": dto.setGender(2); break;
                default: dto.setGender(0); break;
            }
        } else {
            dto.setGender(0);
        }
        
        // 转换状态
        if (dto.getStatusName() != null) {
            dto.setStatus("启用".equals(dto.getStatusName().trim()) ? 1 : 0);
        } else {
            // 默认启用
            dto.setStatus(1);
        }
    }

    /**
     * 将Excel DTO转换为创建请求
     */
    private UserCreateRequest convertToCreateRequest(UserExcelDto dto) {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(dto.getUsername().trim());
        // 默认密码
        request.setPassword(dto.getPassword());
        request.setNickname(dto.getNickname());
        request.setEmail(dto.getEmail());
        request.setPhone(dto.getPhone());
        request.setGender(dto.getGender());
        request.setBirthday(dto.getBirthday());
        request.setStatus(dto.getStatus());
        request.setRemark(dto.getRemark());
        return request;
    }

    /**
     * 创建模板行数据
     */
    private UserExcelDto createTemplateRow(String username, String nickname, String email, String phone, 
                                          String gender, String birthday, String status, String remark) {
        UserExcelDto dto = new UserExcelDto();
        dto.setUsername(username);
        dto.setNickname(nickname);
        dto.setEmail(email);
        dto.setPhone(phone);
        dto.setGenderName(gender);
        dto.setBirthday(java.time.LocalDate.parse(birthday));
        dto.setStatusName(status);
        dto.setRemark(remark);
        return dto;
    }

    @Override
    public UserBasicInfo createThirdPartyUser(Map<String, Object> userInfo) {
        log.info("创建第三方登录用户: {}", userInfo);
        
        try {
            // 构建User实体
            User user = new User();
            user.setUsername((String) userInfo.get("username"));
            // 使用统一的密码加密工具，为第三方用户生成随机密码
            user.setPassword(PasswordUtils.encode("wx_" + System.currentTimeMillis()));
            user.setNickname((String) userInfo.getOrDefault("nickname", "第三方用户"));
            user.setEmail((String) userInfo.get("email"));
            user.setPhone((String) userInfo.get("phone"));
            user.setAvatar((String) userInfo.get("avatar"));
            user.setGender((Integer) userInfo.getOrDefault("gender", 0));
            user.setStatus((Integer) userInfo.getOrDefault("status", 1));
            user.setRemark("第三方登录用户");
            
            // 保存用户
            userMapper.insert(user);
            log.info("第三方用户创建成功: userId={}, username={}", user.getId(), user.getUsername());
            
            // 返回基础信息
            UserBasicInfo basicInfo = new UserBasicInfo();
            basicInfo.setId(user.getId());
            basicInfo.setUsername(user.getUsername());
            basicInfo.setNickname(user.getNickname());
            basicInfo.setEmail(user.getEmail());
            basicInfo.setPhone(user.getPhone());
            basicInfo.setAvatar(user.getAvatar());
            basicInfo.setStatus(user.getStatus());
            basicInfo.setCreateAt(user.getCreateAt());
            basicInfo.setUpdateAt(user.getUpdateAt());
            
            return basicInfo;
                    
        } catch (Exception e) {
            log.error("创建第三方用户失败", e);
            throw new BizException("创建第三方用户失败: " + e.getMessage());
        }
    }

    @Override
    public UserBasicInfo getUserBasicInfoByEmail(String email) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            // 注册场景中，邮箱不存在是正常的
            return null;
        }
        UserBasicInfo basicInfo = new UserBasicInfo();
        BeanUtils.copyProperties(user, basicInfo);
        return basicInfo;
    }

    @Override
    public UserBasicInfo createUserWithBasicInfo(Map<String, Object> userInfo) {
        log.info("📝 创建用户（注册）: {}", userInfo);
        
        try {
            // 构建User实体
            User user = new User();
            user.setUsername((String) userInfo.get("username"));
            // 使用统一的密码加密工具，确保所有创建用户的地方都使用同一加密方式
            String rawPassword = (String) userInfo.get("password");
            user.setPassword(PasswordUtils.encode(rawPassword));
            user.setNickname((String) userInfo.getOrDefault("nickname", userInfo.get("username")));
            user.setEmail((String) userInfo.get("email"));
            user.setPhone((String) userInfo.get("phone"));
            user.setGender((Integer) userInfo.getOrDefault("gender", 0));
            user.setStatus((Integer) userInfo.getOrDefault("status", 1));
            user.setRemark("用户注册");
            
            // 保存用户
            userMapper.insert(user);
            log.info("✅ 用户创建成功: userId={}, username={}", user.getId(), user.getUsername());
            
            // 返回基础信息
            UserBasicInfo basicInfo = new UserBasicInfo();
            basicInfo.setId(user.getId());
            basicInfo.setUsername(user.getUsername());
            basicInfo.setNickname(user.getNickname());
            basicInfo.setEmail(user.getEmail());
            basicInfo.setPhone(user.getPhone());
            basicInfo.setAvatar(user.getAvatar());
            basicInfo.setGender(user.getGender());
            basicInfo.setStatus(user.getStatus());
            basicInfo.setCreateAt(user.getCreateAt());
            basicInfo.setUpdateAt(user.getUpdateAt());
            // 返回加密后的密码用于登录验证
            basicInfo.setPassword(user.getPassword());
            
            return basicInfo;
                    
        } catch (Exception e) {
            log.error("❌ 创建用户失败", e);
            throw new BizException("创建用户失败: " + e.getMessage());
        }
    }

}