package com.lynn.museum.auth.service.impl;

import com.lynn.museum.api.user.client.UserApiClient;
import com.lynn.museum.api.user.dto.UserBasicInfo;
import com.lynn.museum.auth.dto.LoginRequest;
import com.lynn.museum.auth.dto.LoginResponse;
import com.lynn.museum.auth.dto.RegisterRequest;
import com.lynn.museum.auth.dto.ResetPasswordRequest;
import com.lynn.museum.auth.service.AuthService;
import com.lynn.museum.auth.service.CaptchaService;
import com.lynn.museum.auth.service.EmailService;
import com.lynn.museum.auth.service.UserDeviceService;
import com.lynn.museum.auth.service.UserLoginInfoService;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.common.result.ResultCode;
import com.lynn.museum.auth.utils.AdvancedJwtUtils;
import com.lynn.museum.common.utils.PasswordUtils;
import com.lynn.museum.common.utils.RedisKeyBuilder;
import com.lynn.museum.common.redis.utils.RedisUtils;
import com.lynn.museum.common.web.utils.RequestUtils;
import cn.hutool.core.util.StrUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证服务实现类
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserApiClient userApiClient;
    private final RedisUtils redisUtils;
    private final AdvancedJwtUtils advancedJwtUtils;
    private final UserLoginInfoService userLoginInfoService;
    private final EmailService emailService;
    private final CaptchaService captchaService;
    private final UserDeviceService userDeviceService;

    @Value("${museum.auth.jwt.access-token-expire:7200}")
    private Long accessTokenExpire;

    @Value("${museum.auth.jwt.refresh-token-expire:604800}")
    private Long refreshTokenExpire;

    @Value("${museum.auth.jwt.app-refresh-token-expire:7776000}")
    private Long appRefreshTokenExpire;

    @Value("${museum.auth.jwt.refresh-token-sliding-window:30}")
    private Integer refreshTokenSlidingWindow;

    @Value("${museum.auth.device.max-devices-per-user:5}")
    private Integer maxDevicesPerUser;

    @Value("${museum.auth.device.inactive-days:90}")
    private Integer inactiveDays;

    @Value("${museum.auth.login.max-retry:5}")
    private Integer maxRetry;

    @Value("${museum.auth.login.lock-time:1800}")
    private Long lockTime;
    
    @Value("${museum.auth.register.default-role-code:READONLY_USER}")
    // 默认分配的角色编码（只读用户）
    private String defaultRoleCode;

    @Override
    public LoginResponse register(RegisterRequest request) {
        String username = request.getUsername();
        String email = request.getEmail();
        String password = request.getPassword();
        String code = request.getCode();
        String captcha = request.getCaptcha();
        String captchaKey = request.getCaptchaKey();
        
        log.info("📝 用户注册: username={}, email={}", username, email);
        
        // 1. 验证图形验证码
        if (StrUtil.isNotBlank(captchaKey) && StrUtil.isNotBlank(captcha)) {
            if (!captchaService.verifyCaptcha(captchaKey, captcha)) {
                throw new BizException(ResultCode.CAPTCHA_ERROR);
            }
        }
        
        // 2. 验证两次密码是否一致
        if (!password.equals(request.getConfirmPassword())) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }
        
        // 3. 验证邮箱验证码
        if (!emailService.verifyCode(email, code)) {
            throw new BizException(ResultCode.CAPTCHA_ERROR);
        }
        
        // 3. 检查用户名是否已存在
        Result<Boolean> usernameExistsResult = userApiClient.checkUsernameExists(username);
        if (usernameExistsResult != null && usernameExistsResult.isSuccess() && Boolean.TRUE.equals(usernameExistsResult.getData())) {
            log.warn("用户注册失败: 用户名已存在 - {}", username);
            throw new BizException(ResultCode.USER_ALREADY_EXISTS);
        }
        log.debug("用户名检查: {} 不存在，可以注册", username);
        
        // 4. 检查邮箱是否已被注册
        Result<Boolean> emailExistsResult = userApiClient.checkEmailExists(email);
        if (emailExistsResult != null && emailExistsResult.isSuccess() && Boolean.TRUE.equals(emailExistsResult.getData())) {
            log.warn("用户注册失败: 邮箱已被注册 - {}", email);
            throw new BizException(ResultCode.USER_EMAIL_ALREADY_EXISTS);
        }
        log.debug("邮箱检查: {} 不存在，可以注册", email);
        
        // 5. 构建用户信息（密码不在此处加密，由UserService统一处理）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", username);
        // 传递明文密码，由UserService加密
        userInfo.put("password", password);
        userInfo.put("email", email);
        userInfo.put("nickname", StrUtil.isNotBlank(request.getNickname()) ? request.getNickname() : username);
        userInfo.put("phone", request.getPhone());
        // 默认启用
        userInfo.put("status", 1);
        // 默认未知
        userInfo.put("gender", 0);
        
        // 6. 调用用户服务创建用户
        Result<UserBasicInfo> createResult = userApiClient.createUser(userInfo);
        if (createResult == null || !createResult.isSuccess() || createResult.getData() == null) {
            log.error("用户注册失败: {}", createResult != null ? createResult.getMessage() : "未知错误");
            throw new BizException(ResultCode.OPERATION_FAILED);
        }
        
        UserBasicInfo newUser = createResult.getData();
        log.info("✅ 用户注册成功: userId={}, username={}", newUser.getId(), username);
        
        // 7. 分配默认角色（仅网页端）
        // 只读用户角色代码
        String defaultRoleCode = "READONLY_USER";
        String userAgent = getCurrentUserAgent();
        boolean isWebClient = isWebClient(userAgent);
        if (isWebClient) {
            try {
                // 根据角色编码查询角色ID
                log.debug("查询默认角色: roleCode={}", defaultRoleCode);
                Result<com.lynn.museum.api.user.dto.RoleBasicInfo> roleResult = userApiClient.getRoleByCode(defaultRoleCode);
                
                if (roleResult == null || !roleResult.isSuccess() || roleResult.getData() == null) {
                    log.warn("⚠️ 未找到默认角色: roleCode={}, 跳过角色分配", defaultRoleCode);
                } else {
                    Long roleId = roleResult.getData().getId();
                    log.debug("找到默认角色: roleCode={}, roleId={}, roleName={}", 
                            defaultRoleCode, roleId, roleResult.getData().getRoleName());
                    
                    // 分配只读用户角色
                    List<Long> roleIds = List.of(roleId);
                    Result<Void> assignResult = userApiClient.assignRoles(newUser.getId(), roleIds);
                    if (assignResult != null && assignResult.isSuccess()) {
                        log.info("✅ 网页端注册用户自动分配只读用户角色: userId={}, roleCode={}, roleId={}", 
                                newUser.getId(), defaultRoleCode, roleId);
                    } else {
                        log.warn("⚠️ 分配用户角色失败: userId={}, roleCode={}, roleId={}, message={}", 
                                newUser.getId(), defaultRoleCode, roleId, 
                                assignResult != null ? assignResult.getMessage() : "未知错误");
                    }
                }
            } catch (Exception e) {
                log.error("❌ 分配用户角色异常: userId={}, roleCode={}, error={}", 
                        newUser.getId(), defaultRoleCode, e.getMessage());
                // 不影响注册流程，继续执行
            }
        } else {
            log.info("📱 APP端注册用户不分配角色: userId={}", newUser.getId());
        }
        
        // 9. 记录登录信息（注册后自动登录）
        String clientIp = getCurrentClientIp();
        userLoginInfoService.recordLoginInfo(newUser.getId(), username, clientIp, userAgent, 1, "注册自动登录");
        
        // 10. 生成JWT令牌，自动登录
        return generateTokenResponse(newUser);
    }
    
    /**
     * 判断是否为网页端客户端
     * 
     * @param userAgent User-Agent字符串
     * @return true-网页端，false-APP端
     */
    private boolean isWebClient(String userAgent) {
        if (StrUtil.isBlank(userAgent)) {
            // 默认认为是网页端
            return true;
        }
        
        userAgent = userAgent.toLowerCase();
        
        // APP端特征：包含自定义的APP标识
        // 例如：MuseumApp、MSeek、Flutter、Dart等
        String[] appIdentifiers = {
            // 自定义APP标识
            "museumapp",
            // 小程序标识
            "mseek",
            // Flutter应用
            "flutter",
            // Dart应用
            "dart",
            // Android常用HTTP客户端
            "okhttp",
            // iOS网络库
            "cfnetwork"
        };
        
        for (String identifier : appIdentifiers) {
            if (userAgent.contains(identifier)) {
                // 是APP端
                return false;
            }
        }
        
        // 其他情况认为是网页端（包括浏览器）
        return true;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String usernameOrEmail = request.getUsername();
        String password = request.getPassword();
        String captcha = request.getCaptcha();
        String captchaKey = request.getCaptchaKey();
        
        log.info("🔐 用户登录: {}", usernameOrEmail);
        log.info("⚙️ Token配置: accessTokenExpire={}秒, refreshTokenExpire={}秒, appRefreshTokenExpire={}秒", 
                 accessTokenExpire, refreshTokenExpire, appRefreshTokenExpire);
        
        // 1. 验证图形验证码
        if (StrUtil.isNotBlank(captchaKey) && StrUtil.isNotBlank(captcha)) {
            if (!captchaService.verifyCaptcha(captchaKey, captcha)) {
                throw new BizException(ResultCode.CAPTCHA_ERROR);
            }
        }
        
        // 2. 检查账户锁定状态
        checkAccountLocked(usernameOrEmail);
        
        try {
            // 判断是邮箱还是用户名（通过是否包含@符号判断）
            boolean isEmail = usernameOrEmail.contains("@");
            
            // 根据类型获取用户信息
            Result<UserBasicInfo> result;
            if (isEmail) {
                log.info("📧 使用邮箱登录: {}", usernameOrEmail);
                result = userApiClient.getUserByEmail(usernameOrEmail);
            } else {
                log.info("👤 使用用户名登录: {}", usernameOrEmail);
                result = userApiClient.getUserByUsername(usernameOrEmail);
            }
            
            if (result == null || !result.isSuccess() || result.getData() == null) {
                log.warn("[AUTH] 登录失败: {}, 原因: 用户不存在", usernameOrEmail);
                // 统一提示，不暴露用户是否存在
                throw new BizException(ResultCode.UNAUTHORIZED);
            }
            
            UserBasicInfo user = result.getData();
            
            // 检查用户状态
            if (user.getStatus() == null || user.getStatus() != 1) {
                log.warn("[AUTH] 登录失败: {}, 原因: 用户已被禁用", usernameOrEmail);
                throw new BizException(ResultCode.ACCOUNT_DISABLED);
            }
            
            // 验证密码
            if (!PasswordUtils.matches(password, user.getPassword())) {
                handleLoginFailure(usernameOrEmail);
                log.warn("[AUTH] 登录失败: {}, 原因: 密码错误", usernameOrEmail);
                throw new BizException(ResultCode.USER_PASSWORD_ERROR);
            }
            
            // 清除登录失败记录
            clearLoginFailure(usernameOrEmail);
            
            // 记录登录成功信息
            String clientIp = getCurrentClientIp();
            String userAgent = getCurrentUserAgent();
            // 记录时使用实际的用户名，不是输入的邮箱/用户名
            userLoginInfoService.recordLoginInfo(user.getId(), user.getUsername(), clientIp, userAgent, 1, null);
            
            // 生成令牌并返回响应（支持设备绑定）
            return generateTokenResponse(user, request);
                    
        } catch (BizException e) {
            // 业务异常直接向上抛出
            // 注意：GlobalFeignErrorDecoder会将远程服务的错误自动转换为BizException
            // 例如：用户不存在、参数错误等都会被解析为BizException
            log.warn("[AUTH] 登录失败: {}, 错误码: {}, 错误信息: {}", 
                usernameOrEmail, e.getCode(), e.getMessage());
            
            // 为了安全性，对于用户不存在等敏感错误，统一返回"用户名或密码错误"
            if (e.getCode() != null && e.getCode().equals(ResultCode.USER_NOT_FOUND.getCode())) {
                throw new BizException(ResultCode.USER_PASSWORD_ERROR);
            }
            
            throw e;
        } catch (Exception e) {
            log.error("[AUTH] 登录异常: {}", usernameOrEmail, e);
            throw new BizException(ResultCode.SYSTEM_ERROR);
        }
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("🔄 刷新令牌请求");
        
        try {
            // 使用高级JWT工具刷新令牌
            Map<String, String> tokens = advancedJwtUtils.refreshAccessToken(refreshToken);
            String newAccessToken = tokens.get("accessToken");
            String newRefreshToken = tokens.get("refreshToken");
            
            // 从新的 Access Token 中解析用户信息
            Long userId = advancedJwtUtils.getUserIdFromToken(newAccessToken);
            String username = advancedJwtUtils.getUsernameFromToken(newAccessToken);
            
            // 获取用户完整信息
            Result<UserBasicInfo> userResult = userApiClient.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                throw new BizException(ResultCode.USER_NOT_FOUND);
            }
            UserBasicInfo user = userResult.getData();
            
            // 获取用户角色和权限
            List<String> roles = getUserRoles(userId);
            List<String> permissions = getUserPermissions(userId);
            
            // 构建用户信息
            LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .avatar(user.getAvatar())
                    .gender(user.getGender())
                    .roles(roles)
                    .permissions(permissions)
                    .build();
            
            log.info("✅ 令牌刷新成功: userId={}, username={}, expiresIn={}秒", userId, username, accessTokenExpire);
            
            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(accessTokenExpire)
                    .userInfo(userInfo)
                    .build();
                    
        } catch (Exception e) {
            log.error("❌ 刷新令牌失败: {}", e.getMessage(), e);
            throw new BizException(ResultCode.TOKEN_INVALID);
        }
    }

    @Override
    public void logout() {
        try {
            // 从当前请求上下文获取用户ID
            Long userId = getCurrentUserId();
            if (userId != null) {
                // 清除缓存的令牌
                String accessTokenKey = RedisKeyBuilder.buildAuthTokenKey(userId);
                String refreshTokenKey = RedisKeyBuilder.buildAuthRefreshTokenKey(userId);

                // 删除Redis中的token
                redisUtils.del(accessTokenKey, refreshTokenKey);

                log.info("用户登出成功: userId={}, 已清理accessToken和refreshToken", userId);
            } else {
                log.warn("登出时无法获取用户ID，可能是未认证用户或token已失效");
            }
        } catch (Exception e) {
            log.error("登出异常", e);
            // 不抛出异常，允许登出流程继续
        }
    }
    
    /**
     * 获取当前用户ID
     * 从请求头中的Authorization token解析用户ID
     */
    private Long getCurrentUserId() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                
                // 从请求头获取Authorization token
                String authHeader = request.getHeader("Authorization");
                if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
                    // 移除 "Bearer " 前缀
                    String token = authHeader.substring(7);
                    
                    // 使用JWT工具类解析token获取用户ID
                    Long userId = advancedJwtUtils.getUserIdFromToken(token);
                    if (userId != null) {
                        return userId;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("获取当前用户ID失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取当前客户端IP
     */
    private String getCurrentClientIp() {
        try {
            // 尝试从当前HTTP请求获取IP
            // 在微服务环境中，可以通过RequestContextHolder获取
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                return RequestUtils.getClientIp(request);
            }
        } catch (Exception e) {
            log.debug("获取客户端IP失败: {}", e.getMessage());
        }
        return "unknown";
    }

    /**
     * 获取当前用户代理
     */
    private String getCurrentUserAgent() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("获取用户代理失败: {}", e.getMessage());
        }
        return "unknown";
    }

    /**
     * 获取用户角色列表
     */
    private List<String> getUserRoles(Long userId) {
        try {
            Result<List<String>> result = userApiClient.getUserRoles(userId);
            if (result != null && result.isSuccess() && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("获取用户角色失败: {}", userId, e);
        }
        // 返回空列表而不是null
        return List.of();
    }

    /**
     * 获取用户权限列表
     */
    private List<String> getUserPermissions(Long userId) {
        try {
            Result<List<String>> result = userApiClient.getUserPermissions(userId);
            if (result != null && result.isSuccess() && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("获取用户权限失败: {}", userId, e);
        }
        // 返回空列表而不是null
        return List.of();
    }

    /**
     * 检查账户锁定状态
     */
    private void checkAccountLocked(String username) {
        String lockKey = RedisKeyBuilder.buildAuthUserLockKey(username);
        if (redisUtils.hasKey(lockKey)) {
            throw new BizException(ResultCode.ACCOUNT_LOCKED);
        }
    }

    /**
     * 处理登录失败
     */
    private void handleLoginFailure(String username) {
        String failKey = RedisKeyBuilder.buildAuthUserFailKey(username);
        long failCount = redisUtils.incr(failKey, 1);

        // 设置失败记录过期时间
        if (failCount == 1) {
            redisUtils.expire(failKey, lockTime);
        }

        // 达到最大重试次数，锁定账户
        if (failCount >= maxRetry) {
            String lockKey = RedisKeyBuilder.buildAuthUserLockKey(username);
            redisUtils.set(lockKey, "locked", lockTime);
            redisUtils.del(failKey);

            log.warn("账户被锁定: {}, 失败次数: {}, 锁定时长: {}分钟", username, failCount, lockTime / 60);
            throw new BizException(ResultCode.ACCOUNT_LOCKED);
        }
    }

    /**
     * 清除登录失败记录
     */
    private void clearLoginFailure(String username) {
        String failKey = RedisKeyBuilder.buildAuthUserFailKey(username);
        redisUtils.del(failKey);
    }

    /**
     * 缓存令牌
     */
    private void cacheToken(Long userId, String accessToken, String refreshToken) {
        String accessTokenKey = RedisKeyBuilder.buildAuthTokenKey(userId);
        String refreshTokenKey = RedisKeyBuilder.buildAuthRefreshTokenKey(userId);

        redisUtils.set(accessTokenKey, accessToken, accessTokenExpire);
        redisUtils.set(refreshTokenKey, refreshToken, refreshTokenExpire);
    }

    /**
     * 生成令牌响应（兼容旧版本，不带设备信息）
     */
    private LoginResponse generateTokenResponse(UserBasicInfo user) {
        return generateTokenResponse(user, null);
    }

    /**
     * 生成令牌响应（支持设备绑定和差异化 Token 策略）
     * 
     * @param user 用户信息
     * @param loginRequest 登录请求（可能包含设备信息）
     * @return 登录响应
     */
    private LoginResponse generateTokenResponse(UserBasicInfo user, LoginRequest loginRequest) {
        // 获取用户角色和权限
        List<String> roles = getUserRoles(user.getId());
        List<String> permissions = getUserPermissions(user.getId());
        
        // 判断是否为 APP 端（有设备信息）
        boolean isAppClient = loginRequest != null && StrUtil.isNotBlank(loginRequest.getDeviceId());
        
        // 根据客户端类型选择 Refresh Token 过期时间（秒）
        Long refreshExpire = isAppClient ? appRefreshTokenExpire : refreshTokenExpire;
        
        // 生成令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of("USER"));
        String accessToken = advancedJwtUtils.generateAccessToken(user.getId(), user.getUsername(), claims);
        // 传递过期时间（转换为毫秒）
        String refreshToken = advancedJwtUtils.generateRefreshToken(user.getId(), user.getUsername(), refreshExpire * 1000);
        
        // 如果是 APP 端，绑定设备
        if (isAppClient) {
            try {
                // 检查设备数量限制
                if (maxDevicesPerUser > 0 && userDeviceService.isDeviceLimitExceeded(user.getId(), maxDevicesPerUser)) {
                    log.warn("⚠️ 用户设备数量已达上限: userId={}, maxDevices={}", user.getId(), maxDevicesPerUser);
                    // 可以选择：1. 抛出异常 2. 删除最旧的设备 3. 允许继续
                    // 这里选择允许继续，但会在后续清理不活跃设备
                }
                
                // 绑定或更新设备
                String clientIp = getCurrentClientIp();
                userDeviceService.bindOrUpdateDevice(
                    user.getId(),
                    loginRequest.getDeviceId(),
                    loginRequest.getDeviceName(),
                    loginRequest.getDeviceModel(),
                    loginRequest.getOsVersion(),
                    loginRequest.getAppVersion(),
                    loginRequest.getPlatform(),
                    refreshToken,
                    clientIp,
                    null // 地理位置可以后续添加
                );
                
                log.info("✅ APP端登录成功，已绑定设备: userId={}, deviceId={}, platform={}, refreshExpire={}秒 ({}分钟)", 
                         user.getId(), loginRequest.getDeviceId(), loginRequest.getPlatform(), refreshExpire, refreshExpire / 60);
            } catch (Exception e) {
                log.error("❌ 设备绑定失败: userId={}, deviceId={}, error={}", 
                          user.getId(), loginRequest.getDeviceId(), e.getMessage());
                // 设备绑定失败不影响登录，继续执行
            }
        } else {
            log.info("✅ Web端登录成功: userId={}, refreshExpire={}秒 ({}分钟)", 
                     user.getId(), refreshExpire, refreshExpire / 60);
        }
        
        // 缓存令牌
        cacheToken(user.getId(), accessToken, refreshToken);
        
        // 构建响应
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .status(user.getStatus())
                .roles(roles)
                .permissions(permissions)
                .build();
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpire)
                .userInfo(userInfo)
                .build();
    }

    @Override
    public void resetPasswordByEmail(ResetPasswordRequest request) {
        String username = request.getUsername();
        String email = request.getEmail();
        String code = request.getCode();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();
        String captcha = request.getCaptcha();
        String captchaKey = request.getCaptchaKey();
        
        log.info("🔐 用户通过邮箱重置密码: username={}, email={}", username, email);
        
        // 1. 验证图形验证码
        if (StrUtil.isNotBlank(captchaKey) && StrUtil.isNotBlank(captcha)) {
            if (!captchaService.verifyCaptcha(captchaKey, captcha)) {
                throw new BizException(ResultCode.CAPTCHA_ERROR);
            }
        }
        
        // 2. 验证两次密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }
        
        // 3. 验证邮箱验证码
        if (!emailService.verifyCode(email, code)) {
            throw new BizException(ResultCode.CAPTCHA_ERROR);
        }
        
        // 3. 根据邮箱查询用户
        Result<UserBasicInfo> userResult = userApiClient.getUserByEmail(email);
        if (userResult == null || !userResult.isSuccess() || userResult.getData() == null) {
            log.warn("重置密码失败：邮箱未注册: {}", email);
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        
        UserBasicInfo user = userResult.getData();
        
        // 4. 验证用户名和邮箱是否匹配
        if (!username.equals(user.getUsername())) {
            log.warn("重置密码失败：用户名与邮箱不匹配: username={}, email={}", username, email);
            throw new BizException(ResultCode.USERNAME_EMAIL_MISMATCH);
        }
        
        Long userId = user.getId();
        
        // 4. 更新用户密码（传递明文密码，由user-service负责加密）
        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("newPassword", newPassword);
        
        try {
            // 调用用户服务更新密码
            Result<Void> updateResult = userApiClient.resetPassword(userId, passwordMap);
            if (updateResult == null || !updateResult.isSuccess()) {
                log.error("更新密码失败: userId={}, message={}", 
                         userId, updateResult != null ? updateResult.getMessage() : "未知错误");
                throw new BizException(ResultCode.OPERATION_FAILED);
            }
            
            log.info("✅ 密码重置成功: email={}, userId={}", email, userId);
            
            // 6. 清除该用户的所有令牌缓存（强制重新登录）
            String accessTokenKey = RedisKeyBuilder.buildAuthTokenKey(userId);
            String refreshTokenKey = RedisKeyBuilder.buildAuthRefreshTokenKey(userId);
            redisUtils.del(accessTokenKey);
            redisUtils.del(refreshTokenKey);
            
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("重置密码异常: email={}, userId={}", email, userId, e);
            throw new BizException(ResultCode.SYSTEM_ERROR);
        }
    }

}