package com.lynn.museum.auth.service.impl;

import java.util.Date;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lynn.museum.api.user.client.UserApiClient;
import com.lynn.museum.api.user.dto.UserBasicInfo;
import com.lynn.museum.auth.config.WechatProperties;
import com.lynn.museum.auth.config.AlipayProperties;
import com.lynn.museum.auth.config.QqProperties;
import com.lynn.museum.auth.config.GithubProperties;
import com.lynn.museum.auth.dto.LoginResponse;
import com.lynn.museum.auth.dto.WechatCode2SessionResponse;
import com.lynn.museum.auth.mapper.OAuth2UserProviderMapper;
import com.lynn.museum.auth.model.entity.OAuth2UserProvider;
import com.lynn.museum.auth.service.AuthService;
import com.lynn.museum.auth.service.OAuth2Service;
import com.lynn.museum.auth.utils.AdvancedJwtUtils;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth 2.0 服务实现类
 * 
 * 当前版本实现了基础的OAuth2框架，具体的第三方登录逻辑
 * 可以根据实际需求逐步完善。
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {

    private final AuthService authService;
    private final WechatProperties wechatProperties;
    private final AlipayProperties alipayProperties;
    private final QqProperties qqProperties;
    private final GithubProperties githubProperties;
    private final UserApiClient userApiClient;
    private final OAuth2UserProviderMapper providerMapper;
    private final AdvancedJwtUtils advancedJwtUtils;

    @Override
    public String generateAuthorizeUrl(String provider, String redirectUri) {
        log.info("生成OAuth2授权URL: provider={}, redirectUri={}", provider, redirectUri);
        
        switch (provider.toLowerCase()) {
            case "wechat":
                return generateWechatAuthorizeUrl(redirectUri);
            case "alipay":
                return generateAlipayAuthorizeUrl(redirectUri);
            case "qq":
                return generateQqAuthorizeUrl(redirectUri);
            case "github":
                return generateGithubAuthorizeUrl(redirectUri);
            default:
                throw new BizException("不支持的OAuth2提供商: " + provider);
        }
    }

    @Override
    public LoginResponse handleCallback(String provider, String code, String state) {
        log.info("处理OAuth2回调: provider={}, code={}, state={}", provider, code, state);
        
        switch (provider.toLowerCase()) {
            case "wechat":
                return handleWechatCallback(code, state);
            case "alipay":
                return handleAlipayCallback(code, state);
            case "qq":
                return handleQqCallback(code, state);
            case "github":
                return handleGithubCallback(code, state);
            default:
                throw new BizException("不支持的OAuth2提供商: " + provider);
        }
    }

    @Override
    public LoginResponse wechatMiniProgramLogin(String code, String userInfo) {
        log.info("微信小程序登录: code={}", code);
        
        try {
            // 1. 使用code调用微信API获取session_key和openid
            WechatCode2SessionResponse wechatResponse = callWechatCode2Session(code);
            
            // 2. 根据openid查询或创建用户
            UserBasicInfo user = findOrCreateWechatUser(wechatResponse.getOpenid(), wechatResponse.getUnionid(), userInfo);
            
            // 3. 复用现有AuthService的JWT生成逻辑
            return generateJwtLoginResponse(user);
            
        } catch (Exception e) {
            log.error("微信小程序登录失败", e);
            throw new BizException("微信小程序登录失败: " + e.getMessage());
        }
    }

    /**
     * 调用微信code2Session接口
     */
    private WechatCode2SessionResponse callWechatCode2Session(String code) {
        log.info("调用微信code2Session接口，code: {}", code);
        
        // 构建请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("appid", wechatProperties.getMiniprogram().getAppId());
        params.put("secret", wechatProperties.getMiniprogram().getAppSecret());
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");
        
        try {
            // 使用Hutool调用微信API
            String url = wechatProperties.getMiniprogram().getApiBaseUrl() + "/sns/jscode2session";
            log.debug("请求微信API: {}", url);
            
            String responseBody = HttpUtil.get(url, params);
            log.debug("微信API响应: {}", responseBody);
            
            // 解析响应
            WechatCode2SessionResponse response = JSONUtil.toBean(responseBody, WechatCode2SessionResponse.class);
            
            if (!response.isSuccess()) {
                log.error("微信API返回错误: errcode={}, errmsg={}", response.getErrcode(), response.getErrmsg());
                throw new BizException("微信登录失败: " + response.getErrmsg());
            }
            
            log.info("微信code2Session成功，openid: {}", response.getOpenid());
            return response;
            
        } catch (Exception e) {
            log.error("调用微信code2Session接口失败", e);
            throw new BizException("微信登录服务异常: " + e.getMessage());
        }
    }

    /**
     * 根据openid查询或创建微信用户
     */
    private UserBasicInfo findOrCreateWechatUser(String openid, String unionId, String userInfoJson) {
        // 1. 先查找是否已有绑定关系
        LambdaQueryWrapper<OAuth2UserProvider> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OAuth2UserProvider::getProvider, "wechat")
                   .eq(OAuth2UserProvider::getProviderUserId, openid)
                   .eq(OAuth2UserProvider::getStatus, 1)
                   .orderByDesc(OAuth2UserProvider::getBindTime)
                   .last("LIMIT 1");
        OAuth2UserProvider provider = providerMapper.selectOne(queryWrapper);
        
        if (provider != null && provider.getStatus() == 1) {
            // 已有绑定，先验证用户是否还存在
            try {
                Result<UserBasicInfo> userResult = userApiClient.getUserById(provider.getUserId());
                if (userResult != null && userResult.isSuccess() && userResult.getData() != null) {
                    // 用户存在，更新最后登录时间
                    provider.setLastLoginTime(new Date());
                    providerMapper.updateById(provider);
                    return userResult.getData();
                } else {
                    // 用户不存在，删除无效绑定记录
                    log.warn("用户ID {} 不存在，删除无效的OAuth2绑定记录", provider.getUserId());
                    providerMapper.deleteById(provider.getId());
                }
            } catch (Exception e) {
                // API调用失败，也删除绑定记录并重新创建
                log.warn("查询用户ID {} 失败: {}，删除绑定记录并重新创建", provider.getUserId(), e.getMessage());
                providerMapper.deleteById(provider.getId());
            }
        }
        
        // 2. 创建新的系统用户
        Map<String, Object> createUserRequest = new HashMap<>();
        createUserRequest.put("username", "wx_" + openid.substring(0, 8) + "_" + System.currentTimeMillis() % 10000);
        createUserRequest.put("nickname", "微信用户");
        createUserRequest.put("gender", 0);
        createUserRequest.put("status", 1);
        
        // 解析微信用户信息
        if (StrUtil.isNotBlank(userInfoJson)) {
            try {
                Map<String, Object> userInfoMap = JSONUtil.parseObj(userInfoJson);
                
                // 微信用户基础信息
                createUserRequest.put("nickname", userInfoMap.getOrDefault("nickName", "微信用户"));
                createUserRequest.put("avatar", userInfoMap.get("avatarUrl"));
                
                // 性别处理：微信返回 1=男性，2=女性，0=未知
                Object genderObj = userInfoMap.get("gender");
                // 默认未知
                int gender = 0;
                if (genderObj != null) {
                    try {
                        gender = Integer.parseInt(genderObj.toString());
                    } catch (NumberFormatException e) {
                        log.warn("解析性别失败: {}", genderObj);
                    }
                }
                createUserRequest.put("gender", gender);
                
                // 地区信息（可选）
                String country = (String) userInfoMap.get("country");
                String province = (String) userInfoMap.get("province");
                String city = (String) userInfoMap.get("city");
                
                StringBuilder locationBuilder = new StringBuilder();
                if (StrUtil.isNotBlank(country)) {
                    locationBuilder.append(country);
                }
                if (StrUtil.isNotBlank(province)) {
                    locationBuilder.append(" ").append(province);
                }
                if (StrUtil.isNotBlank(city)) {
                    locationBuilder.append(" ").append(city);
                }
                
                if (locationBuilder.length() > 0) {
                    createUserRequest.put("remark", "来自: " + locationBuilder.toString().trim());
                }
                
                log.info("解析微信用户信息成功: nickname={}, gender={}, location={}", 
                    userInfoMap.get("nickName"), gender, locationBuilder.toString());
                    
            } catch (Exception e) {
                log.warn("解析微信用户信息失败: {}", e.getMessage());
            }
        } else {
            log.info("用户未提供详细信息，使用默认信息创建账户");
        }
        
        // 调用用户服务创建用户
        log.info("调用用户服务创建微信用户，请求参数: {}", createUserRequest);
        Result<UserBasicInfo> createResult = userApiClient.createThirdPartyUser(createUserRequest);
        if (createResult == null || !createResult.isSuccess() || createResult.getData() == null) {
            log.error("创建微信用户失败，响应: {}", createResult);
            String errorMsg = "创建微信用户失败";
            if (createResult != null && createResult.getMessage() != null) {
                errorMsg = "创建微信用户失败: " + createResult.getMessage();
            }
            throw new BizException(errorMsg);
        }
        
        UserBasicInfo newUser = createResult.getData();
        log.info("成功创建微信用户，用户ID: {}, 用户名: {}", newUser.getId(), newUser.getUsername());
        
        // 3. 分配默认只读用户角色
        assignDefaultRole(newUser.getId(), "微信");
        
        // 4. 创建第三方绑定关系
        OAuth2UserProvider newProvider = new OAuth2UserProvider();
        newProvider.setUserId(newUser.getId());
        newProvider.setProvider("wechat");
        newProvider.setProviderUserId(openid);
        newProvider.setUnionId(unionId);
        newProvider.setProviderUsername(newUser.getUsername());
        newProvider.setProviderAvatar(newUser.getAvatar());
        newProvider.setRawUserInfo(userInfoJson);
        newProvider.setBindTime(new Date());
        newProvider.setLastLoginTime(new Date());
        newProvider.setStatus(1);
        // createAt和updateAt由BaseEntity自动填充
        
        providerMapper.insert(newProvider);
        log.info("创建微信用户绑定: openid={}, userId={}", openid, newUser.getId());
        
        return newUser;
    }

    /**
     * 生成JWT登录响应（复用AuthService逻辑）
     */
    private LoginResponse generateJwtLoginResponse(UserBasicInfo user) {
        try {
            // 获取用户角色和权限
            List<String> roles = getUserRoles(user.getId());
            List<String> permissions = getUserPermissions(user.getId());
            
            // 生成JWT令牌（复用AuthService的逻辑）
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", roles);
            claims.put("permissions", permissions);
            
            String accessToken = advancedJwtUtils.generateAccessToken(user.getId(), user.getUsername(), claims);
            String refreshToken = advancedJwtUtils.generateRefreshToken(user.getId(), user.getUsername());
            
            // 构建用户信息
            LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .avatar(user.getAvatar())
                    // UserBasicInfo没有gender字段，使用默认值
                    .gender(0)
                    .status(user.getStatus())
                    .lastLoginTime(new Date())
                    .roles(roles)
                    .permissions(permissions)
                    .build();
            
            // 构建登录响应
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    // 2小时
                    .expiresIn(7200L)
                    .userInfo(userInfo)
                    .build();
                    
        } catch (Exception e) {
            log.error("生成JWT登录响应失败", e);
            throw new BizException("登录失败，请稍后重试");
        }
    }

    /**
     * 获取用户角色（复用AuthService逻辑）
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
        return List.of();
    }

    /**
     * 获取用户权限（复用AuthService逻辑）
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
        return List.of();
    }

    @Override
    public LoginResponse alipayMiniProgramLogin(String authCode, String userInfo) {
        log.info("支付宝小程序登录: authCode={}", authCode);
        
        try {
            // TODO: 实现支付宝小程序登录逻辑
            // 1. 使用authCode调用支付宝API获取用户信息
            // 2. 根据用户ID查询或创建用户
            // 3. 生成JWT令牌
            
            // 临时实现 - 返回模拟的登录响应
            return createMockLoginResponse("alipay_mini", authCode);
            
        } catch (Exception e) {
            log.error("支付宝小程序登录失败", e);
            throw new BizException("支付宝小程序登录失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getSupportedProviders() {
        log.debug("获取支持的OAuth2提供商列表");
        
        List<Map<String, Object>> providers = new ArrayList<>();
        
        // 微信登录
        Map<String, Object> wechat = new HashMap<>();
        wechat.put("id", "wechat");
        wechat.put("name", "微信登录");
        wechat.put("icon", "wechat");
        wechat.put("enabled", true);
        wechat.put("supports", List.of("web", "miniprogram"));
        providers.add(wechat);
        
        // 支付宝登录
        Map<String, Object> alipay = new HashMap<>();
        alipay.put("id", "alipay");
        alipay.put("name", "支付宝登录");
        alipay.put("icon", "alipay");
        alipay.put("enabled", true);
        alipay.put("supports", List.of("web", "miniprogram"));
        providers.add(alipay);
        
        return providers;
    }

    @Override
    public void bindThirdPartyAccount(String provider, String code, Long userId) {
        log.info("绑定第三方账号: provider={}, userId={}", provider, userId);
        
        try {
            // TODO: 实现第三方账号绑定逻辑
            // 1. 根据code获取第三方用户信息
            // 2. 检查该第三方账号是否已被其他用户绑定
            // 3. 保存绑定关系到数据库
            
            log.info("第三方账号绑定成功: provider={}, userId={}", provider, userId);
            
        } catch (Exception e) {
            log.error("绑定第三方账号失败", e);
            throw new BizException("绑定第三方账号失败: " + e.getMessage());
        }
    }

    @Override
    public void unbindThirdPartyAccount(String provider, Long userId) {
        log.info("解绑第三方账号: provider={}, userId={}", provider, userId);
        
        try {
            // TODO: 实现第三方账号解绑逻辑
            // 1. 检查用户是否绑定了该第三方账号
            // 2. 删除绑定关系
            // 3. 记录解绑日志
            
            log.info("第三方账号解绑成功: provider={}, userId={}", provider, userId);
            
        } catch (Exception e) {
            log.error("解绑第三方账号失败", e);
            throw new BizException("解绑第三方账号失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getUserBindings(Long userId) {
        log.debug("获取用户绑定的第三方账号: userId={}", userId);
        
        try {
            // TODO: 实现查询用户绑定的第三方账号
            // 1. 从数据库查询用户的绑定关系
            // 2. 返回绑定的第三方账号列表
            
            // 临时实现 - 返回空列表
            List<Map<String, Object>> bindings = new ArrayList<>();
            
            return bindings;
            
        } catch (Exception e) {
            log.error("获取用户绑定信息失败", e);
            throw new BizException("获取绑定信息失败: " + e.getMessage());
        }
    }

    /**
     * 生成微信Web扫码登录授权URL
     * 
     * 支持两种模式：
     * 1. 开放平台模式（企业认证）：使用 qrconnect 扫码登录
     * 2. 测试模式（公众平台测试号）：使用 oauth2/authorize 网页授权
     * 
     * 文档：https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html
     */
    private String generateWechatAuthorizeUrl(String redirectUri) {
        log.info("生成微信Web登录授权URL: redirectUri={}", redirectUri);
        
        String appId = wechatProperties.getWeb().getAppId();
        String defaultRedirectUri = wechatProperties.getWeb().getRedirectUri();
        Boolean testMode = wechatProperties.getWeb().getTestMode();
        
        if (StrUtil.isBlank(appId)) {
            throw new BizException("微信AppID未配置");
        }
        
        String finalRedirectUri = StrUtil.isNotBlank(redirectUri) ? redirectUri : defaultRedirectUri;
        
        if (StrUtil.isBlank(finalRedirectUri)) {
            throw new BizException("微信授权回调地址未配置");
        }
        
        StringBuilder url = new StringBuilder();
        
        if (testMode != null && testMode) {
            // 测试模式：使用公众号网页授权（适合公众平台测试号）
            log.info("使用微信测试模式（公众号网页授权）");
            url.append("https://open.weixin.qq.com/connect/oauth2/authorize");
            url.append("?appid=").append(appId);
            url.append("&redirect_uri=").append(java.net.URLEncoder.encode(finalRedirectUri, java.nio.charset.StandardCharsets.UTF_8));
            url.append("&response_type=code");
            // 测试号使用 snsapi_userinfo
            url.append("&scope=snsapi_userinfo");
            url.append("&state=").append(System.currentTimeMillis());
            url.append("#wechat_redirect");
        } else {
            // 生产模式：使用开放平台扫码登录（需要企业认证）
            log.info("使用微信开放平台模式（扫码登录）");
            url.append("https://open.weixin.qq.com/connect/qrconnect");
            url.append("?appid=").append(appId);
            url.append("&redirect_uri=").append(java.net.URLEncoder.encode(finalRedirectUri, java.nio.charset.StandardCharsets.UTF_8));
            url.append("&response_type=code");
            // 开放平台使用 snsapi_login
            url.append("&scope=snsapi_login");
            url.append("&state=").append(System.currentTimeMillis());
            url.append("#wechat_redirect");
        }
        
        log.info("微信登录URL生成成功: appId={}, redirectUri={}, testMode={}", appId, finalRedirectUri, testMode);
        
        return url.toString();
    }

    /**
     * 生成支付宝授权URL
     */
    private String generateAlipayAuthorizeUrl(String redirectUri) {
        log.debug("生成支付宝授权URL: redirectUri={}", redirectUri);
        
        String appId = alipayProperties.getWeb().getAppId();
        String defaultRedirectUri = alipayProperties.getWeb().getRedirectUri();
        
        if (StrUtil.isBlank(appId)) {
            throw new BizException("支付宝AppID未配置");
        }
        
        String finalRedirectUri = StrUtil.isNotBlank(redirectUri) ? redirectUri : defaultRedirectUri;
        
        if (StrUtil.isBlank(finalRedirectUri)) {
            throw new BizException("支付宝回调地址未配置");
        }
        
        log.info("支付宝授权URL生成: appId={}, redirectUri={}", appId, finalRedirectUri);
        
        StringBuilder url = new StringBuilder();
        url.append("https://openauth.alipay.com/oauth2/publicAppAuthorize.htm");
        url.append("?app_id=").append(appId);
        url.append("&scope=auth_user");
        url.append("&redirect_uri=").append(finalRedirectUri);
        url.append("&state=").append(System.currentTimeMillis());
        
        return url.toString();
    }

    /**
     * 处理微信Web扫码登录回调
     */
    private LoginResponse handleWechatCallback(String code, String state) {
        log.info("处理微信Web扫码登录回调: code={}, state={}", code, state);
        
        try {
            // 1. 使用code换取access_token
            WechatAccessToken accessTokenInfo = getWechatAccessToken(code);
            log.info("获取微信access_token成功");
            
            // 2. 使用access_token获取用户信息
            WechatWebUserInfo wechatUserInfo = getWechatWebUserInfo(accessTokenInfo.getAccessToken(), accessTokenInfo.getOpenid());
            log.info("获取微信用户信息成功: openId={}, nickname={}", 
                    wechatUserInfo.getOpenid(), wechatUserInfo.getNickname());
            
            // 3. 根据微信用户信息查询或创建本地用户
            UserBasicInfo user = findOrCreateWechatWebUser(wechatUserInfo);
            
            // 4. 生成JWT令牌
            return generateJwtLoginResponse(user);
            
        } catch (Exception e) {
            log.error("微信Web扫码登录回调处理失败", e);
            throw new BizException("微信登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用授权码换取微信access_token
     */
    private WechatAccessToken getWechatAccessToken(String code) {
        log.info("使用授权码换取微信access_token: code={}", code);
        
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("appid", wechatProperties.getWeb().getAppId());
            params.put("secret", wechatProperties.getWeb().getAppSecret());
            params.put("code", code);
            params.put("grant_type", "authorization_code");
            
            // 调用微信API
            String url = wechatProperties.getWeb().getApiBaseUrl() + "/sns/oauth2/access_token";
            String responseBody = HttpUtil.get(url, params);
            log.debug("微信access_token接口响应: {}", responseBody);
            
            // 解析响应
            WechatAccessToken response = JSONUtil.toBean(responseBody, WechatAccessToken.class);
            
            if (response.getErrcode() != null && response.getErrcode() != 0) {
                throw new BizException("获取微信access_token失败: " + response.getErrmsg());
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("获取微信access_token失败", e);
            throw new BizException("获取微信access_token失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用access_token获取微信用户信息
     */
    private WechatWebUserInfo getWechatWebUserInfo(String accessToken, String openid) {
        log.info("使用access_token获取微信用户信息");
        
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", accessToken);
            params.put("openid", openid);
            params.put("lang", "zh_CN");
            
            // 调用微信API
            String url = wechatProperties.getWeb().getApiBaseUrl() + "/sns/userinfo";
            String responseBody = HttpUtil.get(url, params);
            log.debug("微信用户信息接口响应: {}", responseBody);
            
            // 解析响应
            WechatWebUserInfo response = JSONUtil.toBean(responseBody, WechatWebUserInfo.class);
            
            if (response.getErrcode() != null && response.getErrcode() != 0) {
                throw new BizException("获取微信用户信息失败: " + response.getErrmsg());
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("获取微信用户信息失败", e);
            throw new BizException("获取微信用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 处理支付宝回调
     */
    private LoginResponse handleAlipayCallback(String code, String state) {
        log.info("处理支付宝登录回调: code={}, state={}", code, state);
        
        try {
            // 1. 使用code换取access_token
            String accessToken = getAlipayAccessToken(code);
            log.info("获取支付宝access_token成功");
            
            // 2. 使用access_token获取用户信息
            AlipayUserInfo alipayUserInfo = getAlipayUserInfo(accessToken);
            log.info("获取支付宝用户信息成功: openId={}, nickName={}", 
                    alipayUserInfo.getOpenId(), alipayUserInfo.getNickName());
            
            // 3. 根据支付宝用户信息查询或创建本地用户
            UserBasicInfo user = findOrCreateAlipayUser(alipayUserInfo);
            
            // 4. 生成JWT令牌
            return generateJwtLoginResponse(user);
            
        } catch (Exception e) {
            log.error("支付宝登录回调处理失败", e);
            throw new BizException("支付宝登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用授权码换取access_token
     */
    private String getAlipayAccessToken(String code) {
        log.info("使用授权码换取access_token: code={}", code);
        
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("app_id", alipayProperties.getWeb().getAppId());
            params.put("method", "alipay.system.oauth.token");
            params.put("format", "json");
            params.put("charset", "utf-8");
            params.put("sign_type", "RSA2");
            params.put("timestamp", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            params.put("version", "1.0");
            params.put("grant_type", "authorization_code");
            params.put("code", code);
            
            // 生成签名
            String sign = generateAlipaySign(params);
            params.put("sign", sign);
            
            // 调用支付宝API
            String url = alipayProperties.getWeb().getServerUrl();
            String responseBody = HttpUtil.post(url, params);
            log.debug("支付宝token接口响应: {}", responseBody);
            
            // 解析响应
            Map<String, Object> response = JSONUtil.toBean(responseBody, Map.class);
            Map<String, Object> tokenResponse = (Map<String, Object>) response.get("alipay_system_oauth_token_response");
            
            if (tokenResponse == null || tokenResponse.get("access_token") == null) {
                throw new BizException("获取支付宝access_token失败: " + responseBody);
            }
            
            return (String) tokenResponse.get("access_token");
            
        } catch (Exception e) {
            log.error("获取支付宝access_token失败", e);
            throw new BizException("获取支付宝access_token失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用access_token获取支付宝用户信息
     */
    private AlipayUserInfo getAlipayUserInfo(String accessToken) {
        log.info("使用access_token获取用户信息");
        
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("app_id", alipayProperties.getWeb().getAppId());
            params.put("method", "alipay.user.info.share");
            params.put("format", "json");
            params.put("charset", "utf-8");
            params.put("sign_type", "RSA2");
            params.put("timestamp", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            params.put("version", "1.0");
            params.put("auth_token", accessToken);
            
            // 生成签名
            String sign = generateAlipaySign(params);
            params.put("sign", sign);
            
            // 调用支付宝API
            String url = alipayProperties.getWeb().getServerUrl();
            String responseBody = HttpUtil.post(url, params);
            log.debug("支付宝用户信息接口响应: {}", responseBody);
            
            // 解析响应
            Map<String, Object> response = JSONUtil.toBean(responseBody, Map.class);
            Map<String, Object> userInfoResponse = (Map<String, Object>) response.get("alipay_user_info_share_response");
            
            if (userInfoResponse == null || userInfoResponse.get("open_id") == null) {
                throw new BizException("获取支付宝用户信息失败: " + responseBody);
            }
            
            // 解析用户信息
            AlipayUserInfo userInfo = new AlipayUserInfo();
            userInfo.setOpenId((String) userInfoResponse.get("open_id"));
            userInfo.setNickName((String) userInfoResponse.get("nick_name"));
            userInfo.setAvatar((String) userInfoResponse.get("avatar"));
            
            log.info("获取支付宝用户信息成功: openId={}, nickName={}, avatar={}", 
                    userInfo.getOpenId(), userInfo.getNickName(), userInfo.getAvatar());
            
            return userInfo;
            
        } catch (Exception e) {
            log.error("获取支付宝用户信息失败", e);
            throw new BizException("获取支付宝用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成支付宝签名
     */
    private String generateAlipaySign(Map<String, Object> params) {
        try {
            // 1. 排序参数
            List<String> keys = new ArrayList<>(params.keySet());
            keys.sort(String::compareTo);
            
            // 2. 拼接待签名字符串（只排除sign，sign_type需要参与签名）
            StringBuilder content = new StringBuilder();
            for (String key : keys) {
                if ("sign".equals(key)) {
                    continue;
                }
                Object value = params.get(key);
                if (value != null && StrUtil.isNotBlank(value.toString())) {
                    if (content.length() > 0) {
                        content.append("&");
                    }
                    content.append(key).append("=").append(value);
                }
            }
            
            log.debug("支付宝待签名字符串: {}", content);
            
            // 3. 使用私钥签名
            String privateKey = alipayProperties.getWeb().getPrivateKey();
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            byte[] privateKeyBytes = java.util.Base64.getDecoder().decode(privateKey);
            java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(privateKeyBytes);
            java.security.PrivateKey priKey = keyFactory.generatePrivate(keySpec);
            
            java.security.Signature signature = java.security.Signature.getInstance("SHA256withRSA");
            signature.initSign(priKey);
            signature.update(content.toString().getBytes("UTF-8"));
            
            byte[] signed = signature.sign();
            return java.util.Base64.getEncoder().encodeToString(signed);
            
        } catch (Exception e) {
            log.error("生成支付宝签名失败", e);
            throw new BizException("生成支付宝签名失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询或创建支付宝用户
     */
    private UserBasicInfo findOrCreateAlipayUser(AlipayUserInfo alipayUserInfo) {
        log.info("查询或创建支付宝用户: openId={}, nickName={}, avatar={}", 
                alipayUserInfo.getOpenId(), alipayUserInfo.getNickName(), alipayUserInfo.getAvatar());
        
        try {
            // 1. 根据openid查询OAuth2UserProvider表
            LambdaQueryWrapper<OAuth2UserProvider> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OAuth2UserProvider::getProvider, "alipay")
                       .eq(OAuth2UserProvider::getProviderUserId, alipayUserInfo.getOpenId());
            
            OAuth2UserProvider provider = providerMapper.selectOne(queryWrapper);
            
            if (provider != null && provider.getStatus() == 1) {
                // 已有绑定，先验证用户是否还存在
                try {
                    Result<UserBasicInfo> userResult = userApiClient.getUserById(provider.getUserId());
                    if (userResult != null && userResult.isSuccess() && userResult.getData() != null) {
                        // 用户存在，更新最后登录时间
                        provider.setLastLoginTime(new Date());
                        providerMapper.updateById(provider);
                        log.info("支付宝用户登录成功: userId={}, openId={}", provider.getUserId(), alipayUserInfo.getOpenId());
                        return userResult.getData();
                    } else {
                        // 用户不存在，删除无效绑定记录
                        log.warn("用户ID {} 不存在，删除无效的OAuth2绑定记录", provider.getUserId());
                        providerMapper.deleteById(provider.getId());
                    }
                } catch (Exception e) {
                    // API调用失败，也删除绑定记录并重新创建
                    log.warn("查询用户ID {} 失败: {}，删除绑定记录并重新创建", provider.getUserId(), e.getMessage());
                    providerMapper.deleteById(provider.getId());
                }
            }
            
            // 2. 不存在，创建新用户
            log.info("创建新的支付宝用户");
            
            // 为第三方登录用户生成随机密码（用户不会使用密码登录，但数据库要求必填）
            String randomPassword = java.util.UUID.randomUUID().toString().replace("-", "");
            
            Map<String, Object> newUserMap = new HashMap<>();
            newUserMap.put("username", "alipay_" + alipayUserInfo.getOpenId().substring(0, Math.min(8, alipayUserInfo.getOpenId().length())));
            // 随机密码
            newUserMap.put("password", randomPassword);
            // 使用支付宝返回的真实昵称
            newUserMap.put("nickname", StrUtil.isNotBlank(alipayUserInfo.getNickName()) 
                    ? alipayUserInfo.getNickName() 
                    : "支付宝用户_" + System.currentTimeMillis() % 10000);
            newUserMap.put("email", "alipay_" + alipayUserInfo.getOpenId() + "@museum.local");
            // 未知
            newUserMap.put("gender", 0);
            // 启用
            newUserMap.put("status", 1);
            
            Result<UserBasicInfo> createResult = userApiClient.createUser(newUserMap);
            
            if (!createResult.isSuccess() || createResult.getData() == null) {
                throw new BizException("创建支付宝用户失败");
            }
            
            UserBasicInfo createdUser = createResult.getData();
            
            // 3. 分配默认只读用户角色
            assignDefaultRole(createdUser.getId(), "支付宝");
            
            // 4. 创建OAuth2关联关系
            OAuth2UserProvider newProvider = new OAuth2UserProvider();
            newProvider.setUserId(createdUser.getId());
            newProvider.setProvider("alipay");
            newProvider.setProviderUserId(alipayUserInfo.getOpenId());
            newProvider.setProviderUsername(createdUser.getUsername());
            newProvider.setProviderAvatar(alipayUserInfo.getAvatar());
            // 已绑定
            newProvider.setStatus(1);
            newProvider.setBindTime(new Date());
            newProvider.setLastLoginTime(new Date());
            
            providerMapper.insert(newProvider);
            
            log.info("支付宝用户创建成功: userId={}, openId={}, nickName={}", 
                    createdUser.getId(), alipayUserInfo.getOpenId(), alipayUserInfo.getNickName());
            
            return createdUser;
            
        } catch (Exception e) {
            log.error("查询或创建支付宝用户失败", e);
            throw new BizException("支付宝用户处理失败: " + e.getMessage());
        }
    }

    /**
     * 生成QQ授权URL
     */
    private String generateQqAuthorizeUrl(String redirectUri) {
        String appId = qqProperties.getWeb().getAppId();
        String defaultRedirectUri = qqProperties.getWeb().getRedirectUri();
        
        if (StrUtil.isBlank(appId)) {
            throw new BizException("QQ AppID未配置");
        }
        
        String finalRedirectUri = StrUtil.isNotBlank(redirectUri) ? redirectUri : defaultRedirectUri;
        
        if (StrUtil.isBlank(finalRedirectUri)) {
            throw new BizException("QQ回调地址未配置");
        }
        
        log.info("QQ授权URL生成: appId={}, redirectUri={}", appId, finalRedirectUri);
        
        // 在state中包含provider信息，格式：qq_timestamp
        String state = "qq_" + System.currentTimeMillis();
        
        StringBuilder url = new StringBuilder();
        url.append(qqProperties.getWeb().getAuthorizeUrl());
        url.append("?response_type=").append(qqProperties.getWeb().getResponseType());
        url.append("&client_id=").append(appId);
        url.append("&redirect_uri=").append(finalRedirectUri);
        url.append("&scope=").append(qqProperties.getWeb().getScope());
        url.append("&display=").append(qqProperties.getWeb().getDisplay());
        url.append("&state=").append(state);
        
        return url.toString();
    }

    /**
     * 处理QQ回调
     */
    private LoginResponse handleQqCallback(String code, String state) {
        log.info("处理QQ登录回调: code={}, state={}", code, state);
        
        try {
            // 1. 使用code换取access_token
            String accessToken = getQqAccessToken(code);
            log.info("获取QQ access_token成功");
            
            // 2. 使用access_token获取OpenID
            String openId = getQqOpenId(accessToken);
            log.info("获取QQ openId成功: openId={}", openId);
            
            // 3. 使用access_token和openId获取用户信息
            QqUserInfo qqUserInfo = getQqUserInfo(accessToken, openId);
            log.info("获取QQ用户信息成功: openId={}, nickname={}", openId, qqUserInfo.getNickname());
            
            // 4. 根据QQ用户信息查询或创建本地用户
            UserBasicInfo user = findOrCreateQqUser(qqUserInfo);
            
            // 5. 生成JWT令牌
            return generateJwtLoginResponse(user);
            
        } catch (Exception e) {
            log.error("QQ登录回调处理失败", e);
            throw new BizException("QQ登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用授权码换取QQ access_token
     */
    private String getQqAccessToken(String code) {
        log.info("使用授权码换取QQ access_token: code={}", code);
        
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("grant_type", "authorization_code");
            params.put("client_id", qqProperties.getWeb().getAppId());
            params.put("client_secret", qqProperties.getWeb().getAppKey());
            params.put("code", code);
            params.put("redirect_uri", qqProperties.getWeb().getRedirectUri());
            // 指定返回JSON格式
            params.put("fmt", "json");
            
            // 调用QQ API
            String url = qqProperties.getWeb().getTokenUrl();
            String responseBody = HttpUtil.get(url, params);
            log.debug("QQ token接口响应: {}", responseBody);
            
            // 解析响应（QQ返回JSON格式）
            Map<String, Object> response = JSONUtil.toBean(responseBody, Map.class);
            
            String accessToken = (String) response.get("access_token");
            if (StrUtil.isBlank(accessToken)) {
                throw new BizException("获取QQ access_token失败: " + responseBody);
            }
            
            return accessToken;
            
        } catch (Exception e) {
            log.error("获取QQ access_token失败", e);
            throw new BizException("获取QQ access_token失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用access_token获取QQ OpenID
     */
    private String getQqOpenId(String accessToken) {
        log.info("使用access_token获取QQ OpenID");
        
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", accessToken);
            // 指定返回JSON格式
            params.put("fmt", "json");
            
            // 调用QQ API
            String url = qqProperties.getWeb().getOpenIdUrl();
            String responseBody = HttpUtil.get(url, params);
            log.debug("QQ openId接口响应: {}", responseBody);
            
            // 解析响应
            Map<String, Object> response = JSONUtil.toBean(responseBody, Map.class);
            
            String openId = (String) response.get("openid");
            if (StrUtil.isBlank(openId)) {
                throw new BizException("获取QQ OpenID失败: " + responseBody);
            }
            
            return openId;
            
        } catch (Exception e) {
            log.error("获取QQ OpenID失败", e);
            throw new BizException("获取QQ OpenID失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用access_token和openId获取QQ用户信息
     */
    private QqUserInfo getQqUserInfo(String accessToken, String openId) {
        log.info("使用access_token获取QQ用户信息");
        
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", accessToken);
            params.put("oauth_consumer_key", qqProperties.getWeb().getAppId());
            params.put("openid", openId);
            
            // 调用QQ API
            String url = qqProperties.getWeb().getUserInfoUrl();
            String responseBody = HttpUtil.get(url, params);
            log.debug("QQ用户信息接口响应: {}", responseBody);
            
            // 解析响应
            QqUserInfo response = JSONUtil.toBean(responseBody, QqUserInfo.class);
            
            // QQ API成功返回时ret=0
            if (response.getRet() != null && response.getRet() != 0) {
                throw new BizException("获取QQ用户信息失败: " + response.getMsg());
            }
            
            // 设置openId（API响应中不包含，需要手动设置）
            response.setOpenId(openId);
            
            return response;
            
        } catch (Exception e) {
            log.error("获取QQ用户信息失败", e);
            throw new BizException("获取QQ用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询或创建QQ用户
     */
    private UserBasicInfo findOrCreateQqUser(QqUserInfo qqUserInfo) {
        log.info("查询或创建QQ用户: openId={}, nickname={}", 
                qqUserInfo.getOpenId(), qqUserInfo.getNickname());
        
        try {
            // 1. 先根据openId查询是否已有绑定关系
            LambdaQueryWrapper<OAuth2UserProvider> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OAuth2UserProvider::getProvider, "qq")
                       .eq(OAuth2UserProvider::getProviderUserId, qqUserInfo.getOpenId());
            
            OAuth2UserProvider provider = providerMapper.selectOne(queryWrapper);
            
            if (provider != null && provider.getStatus() == 1) {
                // 已有绑定，先验证用户是否还存在
                try {
                    Result<UserBasicInfo> userResult = userApiClient.getUserById(provider.getUserId());
                    if (userResult != null && userResult.isSuccess() && userResult.getData() != null) {
                        // 用户存在，更新最后登录时间
                        provider.setLastLoginTime(new Date());
                        providerMapper.updateById(provider);
                        log.info("QQ用户登录成功: userId={}, openId={}", provider.getUserId(), qqUserInfo.getOpenId());
                        return userResult.getData();
                    } else {
                        // 用户不存在，删除无效绑定记录
                        log.warn("用户ID {} 不存在，删除无效的OAuth2绑定记录", provider.getUserId());
                        providerMapper.deleteById(provider.getId());
                    }
                } catch (Exception e) {
                    // API调用失败，也删除绑定记录并重新创建
                    log.warn("查询用户ID {} 失败: {}，删除绑定记录并重新创建", provider.getUserId(), e.getMessage());
                    providerMapper.deleteById(provider.getId());
                }
            }
            
            // 2. 不存在，创建新用户
            log.info("创建新的QQ用户");
            
            // 为第三方登录用户生成随机密码（用户不会使用密码登录，但数据库要求必填）
            String randomPassword = java.util.UUID.randomUUID().toString().replace("-", "");
            
            Map<String, Object> newUserMap = new HashMap<>();
            newUserMap.put("username", "qq_" + qqUserInfo.getOpenId().substring(0, Math.min(8, qqUserInfo.getOpenId().length())));
            // 随机密码
            newUserMap.put("password", randomPassword);
            // 使用QQ返回的真实昵称
            newUserMap.put("nickname", StrUtil.isNotBlank(qqUserInfo.getNickname()) 
                    ? qqUserInfo.getNickname() 
                    : "QQ用户_" + System.currentTimeMillis() % 10000);
            newUserMap.put("email", "qq_" + qqUserInfo.getOpenId() + "@museum.local");
            // QQ gender字段: 男-"男", 女-"女", 未知-""
            // 默认未知
            int gender = 0;
            if ("男".equals(qqUserInfo.getGender())) {
                gender = 1;
            } else if ("女".equals(qqUserInfo.getGender())) {
                gender = 2;
            }
            newUserMap.put("gender", gender);
            // 启用
            newUserMap.put("status", 1);
            
            Result<UserBasicInfo> createResult = userApiClient.createUser(newUserMap);
            
            if (!createResult.isSuccess() || createResult.getData() == null) {
                throw new BizException("创建QQ用户失败");
            }
            
            UserBasicInfo createdUser = createResult.getData();
            
            // 3. 分配默认只读用户角色
            assignDefaultRole(createdUser.getId(), "QQ");
            
            // 4. 创建OAuth2关联关系
            OAuth2UserProvider newProvider = new OAuth2UserProvider();
            newProvider.setUserId(createdUser.getId());
            newProvider.setProvider("qq");
            newProvider.setProviderUserId(qqUserInfo.getOpenId());
            newProvider.setProviderUsername(createdUser.getUsername());
            // 使用QQ头像
            newProvider.setProviderAvatar(qqUserInfo.getFigureurl_qq_1());
            // 已绑定
            newProvider.setStatus(1);
            newProvider.setBindTime(new Date());
            newProvider.setLastLoginTime(new Date());
            
            providerMapper.insert(newProvider);
            
            log.info("QQ用户创建成功: userId={}, openId={}, nickname={}", 
                    createdUser.getId(), qqUserInfo.getOpenId(), qqUserInfo.getNickname());
            
            return createdUser;
            
        } catch (Exception e) {
            log.error("查询或创建QQ用户失败", e);
            throw new BizException("QQ用户处理失败: " + e.getMessage());
        }
    }

    /**
     * 生成GitHub授权URL
     */
    private String generateGithubAuthorizeUrl(String redirectUri) {
        String clientId = githubProperties.getWeb().getClientId();
        String defaultRedirectUri = githubProperties.getWeb().getRedirectUri();
        
        if (StrUtil.isBlank(clientId)) {
            throw new BizException("GitHub Client ID未配置");
        }
        
        String finalRedirectUri = StrUtil.isNotBlank(redirectUri) ? redirectUri : defaultRedirectUri;
        
        if (StrUtil.isBlank(finalRedirectUri)) {
            throw new BizException("GitHub回调地址未配置");
        }
        
        log.info("GitHub授权URL生成: clientId={}, redirectUri={}", clientId, finalRedirectUri);
        
        // 在state中包含provider信息，格式：github_timestamp
        String state = "github_" + System.currentTimeMillis();
        
        StringBuilder url = new StringBuilder();
        url.append(githubProperties.getWeb().getAuthorizeUrl());
        url.append("?client_id=").append(clientId);
        url.append("&redirect_uri=").append(finalRedirectUri);
        url.append("&scope=").append(githubProperties.getWeb().getScope());
        url.append("&state=").append(state);
        
        return url.toString();
    }

    /**
     * 处理GitHub回调
     */
    private LoginResponse handleGithubCallback(String code, String state) {
        log.info("处理GitHub登录回调: code={}, state={}", code, state);
        
        try {
            // 1. 使用code换取access_token
            String accessToken = getGithubAccessToken(code);
            log.info("获取GitHub access_token成功");
            
            // 2. 使用access_token获取用户信息
            GithubUserInfo githubUserInfo = getGithubUserInfo(accessToken);
            log.info("获取GitHub用户信息成功: id={}, login={}", 
                    githubUserInfo.getId(), githubUserInfo.getLogin());
            
            // 3. 根据GitHub用户信息查询或创建本地用户
            UserBasicInfo user = findOrCreateGithubUser(githubUserInfo);
            
            // 4. 生成JWT令牌
            return generateJwtLoginResponse(user);
            
        } catch (Exception e) {
            log.error("GitHub登录回调处理失败", e);
            throw new BizException("GitHub登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用授权码换取GitHub access_token
     */
    private String getGithubAccessToken(String code) {
        log.info("使用授权码换取GitHub access_token: code={}", code);
        
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("client_id", githubProperties.getWeb().getClientId());
            params.put("client_secret", githubProperties.getWeb().getClientSecret());
            params.put("code", code);
            params.put("redirect_uri", githubProperties.getWeb().getRedirectUri());
            
            // 调用GitHub API
            String url = githubProperties.getWeb().getTokenUrl();
            String responseBody = HttpUtil.createPost(url)
                    // GitHub需要指定Accept为JSON
                    .header("Accept", "application/json")
                    .form(params)
                    .execute()
                    .body();
            
            log.debug("GitHub token接口响应: {}", responseBody);
            
            // 解析响应
            Map<String, Object> response = JSONUtil.toBean(responseBody, Map.class);
            
            String accessToken = (String) response.get("access_token");
            if (StrUtil.isBlank(accessToken)) {
                String error = (String) response.get("error");
                String errorDescription = (String) response.get("error_description");
                throw new BizException("获取GitHub access_token失败: " + error + " - " + errorDescription);
            }
            
            return accessToken;
            
        } catch (Exception e) {
            log.error("获取GitHub access_token失败", e);
            throw new BizException("获取GitHub access_token失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用access_token获取GitHub用户信息
     */
    private GithubUserInfo getGithubUserInfo(String accessToken) {
        log.info("使用access_token获取GitHub用户信息");
        
        try {
            // 调用GitHub API
            String url = githubProperties.getWeb().getUserInfoUrl();
            String responseBody = HttpUtil.createGet(url)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .execute()
                    .body();
            
            log.debug("GitHub用户信息接口响应: {}", responseBody);
            
            // 解析响应
            GithubUserInfo response = JSONUtil.toBean(responseBody, GithubUserInfo.class);
            
            if (response.getId() == null) {
                throw new BizException("获取GitHub用户信息失败: " + responseBody);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("获取GitHub用户信息失败", e);
            throw new BizException("获取GitHub用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询或创建GitHub用户
     */
    private UserBasicInfo findOrCreateGithubUser(GithubUserInfo githubUserInfo) {
        log.info("查询或创建GitHub用户: id={}, login={}", 
                githubUserInfo.getId(), githubUserInfo.getLogin());
        
        try {
            // 1. 先根据GitHub ID查询是否已有绑定关系
            LambdaQueryWrapper<OAuth2UserProvider> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OAuth2UserProvider::getProvider, "github")
                       .eq(OAuth2UserProvider::getProviderUserId, String.valueOf(githubUserInfo.getId()));
            
            OAuth2UserProvider provider = providerMapper.selectOne(queryWrapper);
            
            if (provider != null && provider.getStatus() == 1) {
                // 已有绑定，先验证用户是否还存在
                try {
                    Result<UserBasicInfo> userResult = userApiClient.getUserById(provider.getUserId());
                    if (userResult != null && userResult.isSuccess() && userResult.getData() != null) {
                        // 用户存在，更新最后登录时间
                        provider.setLastLoginTime(new Date());
                        providerMapper.updateById(provider);
                        log.info("GitHub用户登录成功: userId={}, githubId={}", provider.getUserId(), githubUserInfo.getId());
                        return userResult.getData();
                    } else {
                        // 用户不存在，删除无效绑定记录
                        log.warn("用户ID {} 不存在，删除无效的OAuth2绑定记录", provider.getUserId());
                        providerMapper.deleteById(provider.getId());
                    }
                } catch (Exception e) {
                    // API调用失败，也删除绑定记录并重新创建
                    log.warn("查询用户ID {} 失败: {}，删除绑定记录并重新创建", provider.getUserId(), e.getMessage());
                    providerMapper.deleteById(provider.getId());
                }
            }
            
            // 2. 不存在，创建新用户
            log.info("创建新的GitHub用户");
            
            // 为第三方登录用户生成随机密码（用户不会使用密码登录，但数据库要求必填）
            String randomPassword = java.util.UUID.randomUUID().toString().replace("-", "");
            
            Map<String, Object> newUserMap = new HashMap<>();
            newUserMap.put("username", "github_" + githubUserInfo.getLogin());
            // 随机密码
            newUserMap.put("password", randomPassword);
            // 使用GitHub返回的name，如果没有则使用login
            newUserMap.put("nickname", StrUtil.isNotBlank(githubUserInfo.getName()) 
                    ? githubUserInfo.getName() 
                    : githubUserInfo.getLogin());
            // 使用GitHub email，如果没有则生成一个
            newUserMap.put("email", StrUtil.isNotBlank(githubUserInfo.getEmail())
                    ? githubUserInfo.getEmail()
                    : "github_" + githubUserInfo.getId() + "@museum.local");
            // 未知
            newUserMap.put("gender", 0);
            // 启用
            newUserMap.put("status", 1);
            
            Result<UserBasicInfo> createResult = userApiClient.createUser(newUserMap);
            
            if (!createResult.isSuccess() || createResult.getData() == null) {
                throw new BizException("创建GitHub用户失败");
            }
            
            UserBasicInfo createdUser = createResult.getData();
            
            // 3. 分配默认只读用户角色
            assignDefaultRole(createdUser.getId(), "GitHub");
            
            // 4. 创建OAuth2关联关系
            OAuth2UserProvider newProvider = new OAuth2UserProvider();
            newProvider.setUserId(createdUser.getId());
            newProvider.setProvider("github");
            newProvider.setProviderUserId(String.valueOf(githubUserInfo.getId()));
            newProvider.setProviderUsername(githubUserInfo.getLogin());
            newProvider.setProviderAvatar(githubUserInfo.getAvatarUrl());
            // 已绑定 // 已绑定
            newProvider.setStatus(1);
            newProvider.setBindTime(new Date());
            newProvider.setLastLoginTime(new Date());
            
            providerMapper.insert(newProvider);
            
            log.info("GitHub用户创建成功: userId={}, githubId={}, login={}", 
                    createdUser.getId(), githubUserInfo.getId(), githubUserInfo.getLogin());
            
            return createdUser;
            
        } catch (Exception e) {
            log.error("查询或创建GitHub用户失败", e);
            throw new BizException("GitHub用户处理失败: " + e.getMessage());
        }
    }

    /**
     * 为新创建的第三方用户分配默认只读角色
     * 
     * @param userId 用户ID
     * @param provider 第三方提供商名称（如"微信"、"支付宝"）
     */
    private void assignDefaultRole(Long userId, String provider) {
        // 只读用户角色代码
        String defaultRoleCode = "READONLY_USER";
        try {
            log.debug("查询默认角色: roleCode={}", defaultRoleCode);
            Result<com.lynn.museum.api.user.dto.RoleBasicInfo> roleResult = userApiClient.getRoleByCode(defaultRoleCode);
            
            if (roleResult == null || !roleResult.isSuccess() || roleResult.getData() == null) {
                log.warn("⚠️ 未找到默认角色: roleCode={}, 跳过{}用户角色分配", defaultRoleCode, provider);
            } else {
                Long roleId = roleResult.getData().getId();
                log.debug("找到默认角色: roleCode={}, roleId={}, roleName={}", 
                        defaultRoleCode, roleId, roleResult.getData().getRoleName());
                
                // 分配只读用户角色
                List<Long> roleIds = List.of(roleId);
                Result<Void> assignResult = userApiClient.assignRoles(userId, roleIds);
                if (assignResult != null && assignResult.isSuccess()) {
                    log.info("✅ {}登录用户自动分配只读用户角色: userId={}, roleCode={}, roleId={}", 
                            provider, userId, defaultRoleCode, roleId);
                } else {
                    log.warn("⚠️ 分配{}用户角色失败: userId={}, roleCode={}, roleId={}, message={}", 
                            provider, userId, defaultRoleCode, roleId, 
                            assignResult != null ? assignResult.getMessage() : "未知错误");
                }
            }
        } catch (Exception e) {
            log.error("❌ 分配{}用户角色异常: userId={}, roleCode={}, error={}", 
                    provider, userId, defaultRoleCode, e.getMessage());
            // 不影响登录流程，继续执行
        }
    }

    /**
     * 创建模拟的登录响应
     * 
     * 注意：这是用于开发阶段的模拟实现
     * 生产环境部署前需要实现真实的第三方登录逻辑
     */
    private LoginResponse createMockLoginResponse(String provider, String code) {
        log.warn("⚠️ 使用OAuth2模拟响应 - 生产环境请实现真实第三方登录: provider={}", provider);
        
        // 模拟用户信息（开发环境）
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .userId(1000L + (long) provider.hashCode())
                .username("demo_user_" + provider)
                .nickname("演示用户_" + provider)
                .email("demo_" + provider + "@museum.local")
                .avatar("/assets/avatar/default_" + provider + ".png")
                .gender(1)
                .status(1)
                .build();
        
        return LoginResponse.builder()
                .accessToken("demo_access_token_" + provider + "_" + System.currentTimeMillis())
                .refreshToken("demo_refresh_token_" + provider + "_" + System.currentTimeMillis())
                .tokenType("Bearer")
                .expiresIn(7200L)
                .userInfo(userInfo)
                .build();
    }
    
    /**
     * 查询或创建微信Web扫码登录用户
     */
    private UserBasicInfo findOrCreateWechatWebUser(WechatWebUserInfo wechatUserInfo) {
        log.info("查询或创建微信Web用户: openId={}, nickname={}, unionId={}", 
                wechatUserInfo.getOpenid(), wechatUserInfo.getNickname(), wechatUserInfo.getUnionid());
        
        try {
            // 1. 先查找是否已有绑定关系（优先使用unionid，如果没有则使用openid）
            String providerUserId = StrUtil.isNotBlank(wechatUserInfo.getUnionid()) 
                    ? wechatUserInfo.getUnionid() 
                    : wechatUserInfo.getOpenid();
            
            LambdaQueryWrapper<OAuth2UserProvider> wechatQueryWrapper = new LambdaQueryWrapper<>();
            wechatQueryWrapper.eq(OAuth2UserProvider::getProvider, "wechat")
                             .eq(OAuth2UserProvider::getProviderUserId, providerUserId)
                             .eq(OAuth2UserProvider::getStatus, 1)
                             .orderByDesc(OAuth2UserProvider::getBindTime)
                             .last("LIMIT 1");
            OAuth2UserProvider provider = providerMapper.selectOne(wechatQueryWrapper);
            
            if (provider != null && provider.getStatus() == 1) {
                // 已有绑定，先验证用户是否还存在
                try {
                    Result<UserBasicInfo> userResult = userApiClient.getUserById(provider.getUserId());
                    if (userResult != null && userResult.isSuccess() && userResult.getData() != null) {
                        // 用户存在，更新最后登录时间
                        provider.setLastLoginTime(new Date());
                        providerMapper.updateById(provider);
                        log.info("微信Web用户登录成功: userId={}, openId={}", 
                                provider.getUserId(), wechatUserInfo.getOpenid());
                        return userResult.getData();
                    } else {
                        // 用户不存在，删除无效绑定记录
                        log.warn("用户ID {} 不存在，删除无效的OAuth2绑定记录", provider.getUserId());
                        providerMapper.deleteById(provider.getId());
                    }
                } catch (Exception e) {
                    // API调用失败，也删除绑定记录并重新创建
                    log.warn("查询用户ID {} 失败: {}，删除绑定记录并重新创建", provider.getUserId(), e.getMessage());
                    providerMapper.deleteById(provider.getId());
                }
            }
            
            // 2. 不存在，创建新用户
            log.info("创建新的微信Web用户");
            
            // 为第三方登录用户生成随机密码
            String randomPassword = java.util.UUID.randomUUID().toString().replace("-", "");
            
            Map<String, Object> newUserMap = new HashMap<>();
            newUserMap.put("username", "wechat_web_" + wechatUserInfo.getOpenid().substring(0, Math.min(8, wechatUserInfo.getOpenid().length())));
            newUserMap.put("password", randomPassword);
            newUserMap.put("nickname", StrUtil.isNotBlank(wechatUserInfo.getNickname()) 
                    ? wechatUserInfo.getNickname() 
                    : "微信用户");
            newUserMap.put("gender", wechatUserInfo.getSex() != null ? wechatUserInfo.getSex() : 0);
            newUserMap.put("status", 1);
            
            Result<UserBasicInfo> createResult = userApiClient.createUser(newUserMap);
            
            if (!createResult.isSuccess() || createResult.getData() == null) {
                throw new BizException("创建微信Web用户失败");
            }
            
            UserBasicInfo createdUser = createResult.getData();
            
            // 3. 分配默认只读用户角色
            assignDefaultRole(createdUser.getId(), "微信Web");
            
            // 4. 创建OAuth2关联关系
            OAuth2UserProvider newProvider = new OAuth2UserProvider();
            newProvider.setUserId(createdUser.getId());
            newProvider.setProvider("wechat");
            // 优先使用unionid
            newProvider.setProviderUserId(providerUserId);
            newProvider.setUnionId(wechatUserInfo.getUnionid());
            newProvider.setProviderUsername(createdUser.getUsername());
            newProvider.setStatus(1);
            newProvider.setBindTime(new Date());
            newProvider.setLastLoginTime(new Date());
            
            providerMapper.insert(newProvider);
            
            log.info("微信Web用户创建成功: userId={}, openId={}, nickname={}", 
                    createdUser.getId(), wechatUserInfo.getOpenid(), wechatUserInfo.getNickname());
            
            return createdUser;
            
        } catch (Exception e) {
            log.error("查询或创建微信Web用户失败", e);
            throw new BizException("微信Web用户处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 微信access_token响应DTO
     */
    @lombok.Data
    private static class WechatAccessToken {
        private String accessToken;
        private Integer expiresIn;
        private String refreshToken;
        private String openid;
        private String scope;
        private String unionid;
        private Integer errcode;
        private String errmsg;
        
        // JSON字段映射（下划线转驼峰）
        @com.fasterxml.jackson.annotation.JsonProperty("access_token")
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
        
        @com.fasterxml.jackson.annotation.JsonProperty("expires_in")
        public void setExpiresIn(Integer expiresIn) {
            this.expiresIn = expiresIn;
        }
        
        @com.fasterxml.jackson.annotation.JsonProperty("refresh_token")
        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
    
    /**
     * 微信Web用户信息DTO
     */
    @lombok.Data
    private static class WechatWebUserInfo {
        private String openid;
        private String nickname;
        private Integer sex; // 1=男性，2=女性，0=未知
        private String province;
        private String city;
        private String country;
        private String headimgurl; // 头像URL
        private String unionid;
        private Integer errcode;
        private String errmsg;
    }
    
    /**
     * 支付宝用户信息DTO
     */
    @lombok.Data
    private static class AlipayUserInfo {
        /** 支付宝用户唯一标识 open_id */
        private String openId;
        
        /** 支付宝用户昵称 */
        private String nickName;
        
        /** 支付宝用户头像URL */
        private String avatar;
    }
    
    /**
     * QQ用户信息DTO
     */
    @lombok.Data
    private static class QqUserInfo {
        /** QQ返回码 0表示成功 */
        private Integer ret;
        
        /** 错误消息 */
        private String msg;
        
        /** 用户OpenID（需要手动设置，API不返回） */
        private String openId;
        
        /** 用户昵称 */
        private String nickname;
        
        /** 用户头像URL 30x30 */
        private String figureurl;
        
        /** 用户头像URL 50x50 */
        private String figureurl_1;
        
        /** 用户头像URL 100x100 */
        private String figureurl_2;
        
        /** 用户头像URL 40x40 (QQ头像) */
        private String figureurl_qq_1;
        
        /** 用户头像URL 100x100 (QQ头像) */
        private String figureurl_qq_2;
        
        /** 用户性别：男/女 */
        private String gender;
        
        /** 用户是否为黄钻用户：0-否，1-是 */
        private String is_yellow_vip;
        
        /** 用户是否为会员：0-否，1-是 */
        private String vip;
        
        /** 用户黄钻等级 */
        private String yellow_vip_level;
        
        /** 用户会员等级 */
        private String level;
    }
    
    /**
     * GitHub用户信息DTO
     */
    @lombok.Data
    private static class GithubUserInfo {
        /** GitHub用户ID */
        private Long id;
        
        /** GitHub用户名 (login) */
        private String login;
        
        /** GitHub用户姓名 */
        private String name;
        
        /** GitHub用户邮箱 */
        private String email;
        
        /** GitHub用户头像URL */
        @com.fasterxml.jackson.annotation.JsonProperty("avatar_url")
        private String avatarUrl;
        
        /** GitHub个人主页 */
        @com.fasterxml.jackson.annotation.JsonProperty("html_url")
        private String htmlUrl;
        
        /** 个人简介 */
        private String bio;
        
        /** 公司 */
        private String company;
        
        /** 博客 */
        private String blog;
        
        /** 地址 */
        private String location;
        
        /** 公开仓库数 */
        @com.fasterxml.jackson.annotation.JsonProperty("public_repos")
        private Integer publicRepos;
        
        /** 粉丝数 */
        private Integer followers;
        
        /** 关注数 */
        private Integer following;
    }
}
