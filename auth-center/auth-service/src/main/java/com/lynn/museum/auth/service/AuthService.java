package com.lynn.museum.auth.service;

import com.lynn.museum.auth.dto.LoginRequest;
import com.lynn.museum.auth.dto.LoginResponse;
import com.lynn.museum.auth.dto.RegisterRequest;
import com.lynn.museum.auth.dto.ResetPasswordRequest;

/**
 * 认证服务接口
 * 
 * @author lynn
 * @since 2024-01-01
 */
public interface AuthService {

    /**
     * 用户注册
     * 
     * 注册新用户账号，验证邮箱验证码，创建用户信息。
     * 
     * @param request 注册请求对象
     * @return 登录响应对象（注册成功后自动登录）
     * @throws com.lynn.museum.common.exception.BusinessException 注册失败时抛出异常
     * @since 2025-01-01
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 用户登录认证
     * 
     * 验证用户名和密码，成功后生成JWT访问令牌和刷新令牌。
     * 支持登录失败次数限制和账户锁定机制。
     * 
     * @param request 登录请求对象，包含用户名和密码
     *                - username: 用户名，长度3-20个字符，不能为空
     *                - password: 密码，长度6-20个字符，不能为空
     * @return 登录响应对象，包含访问令牌、刷新令牌、用户信息等
     *         - accessToken: JWT访问令牌，有效期1小时
     *         - refreshToken: 刷新令牌，有效期7天
     *         - userInfo: 用户基础信息
     *         - expiresIn: 令牌过期时间（秒）
     * @throws com.lynn.museum.common.exception.BusinessException 登录失败时抛出异常
     *         - 用户名不存在
     *         - 密码错误
     *         - 账户被锁定
     *         - 账户被禁用
     * @since 1.0.0
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新访问令牌
     * 
     * 使用刷新令牌获取新的访问令牌，延长用户会话时间。
     * 刷新成功后会生成新的访问令牌和刷新令牌。
     * 
     * @param refreshToken 刷新令牌字符串，不能为空
     *                    必须是有效的、未过期的、未被撤销的刷新令牌
     * @return 新的登录响应对象，包含新的访问令牌和刷新令牌
     *         - accessToken: 新的JWT访问令牌，有效期1小时
     *         - refreshToken: 新的刷新令牌，有效期7天
     *         - userInfo: 用户基础信息
     *         - expiresIn: 令牌过期时间（秒）
     * @throws com.lynn.museum.common.exception.BusinessException 刷新失败时抛出异常
     *         - 刷新令牌无效
     *         - 刷新令牌已过期
     *         - 刷新令牌已被撤销
     *         - 用户不存在或被禁用
     * @since 1.0.0
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 用户登出
     * 
     * 清除用户的认证状态，撤销相关令牌。
     * 会将访问令牌加入黑名单，清除缓存中的用户信息。
     * 
     * 注意：此方法需要在有认证上下文的情况下调用，
     * 即用户必须先通过认证才能执行登出操作。
     * 
     * @throws com.lynn.museum.common.exception.BusinessException 登出失败时抛出异常
     *         - 用户未登录
     *         - 令牌无效
     * @since 1.0.0
     */
    void logout();

    /**
     * 通过邮箱重置密码（忘记密码功能）
     * 
     * 用户通过邮箱验证码重置密码，用于忘记密码场景。
     * 验证邮箱验证码后，更新用户密码。
     * 
     * @param request 重置密码请求对象
     *                - email: 用户邮箱，必须是已注册的邮箱
     *                - code: 邮箱验证码，6位数字
     *                - newPassword: 新密码，长度6-20个字符
     *                - confirmPassword: 确认密码，必须与新密码一致
     * @throws com.lynn.museum.common.exception.BusinessException 重置失败时抛出异常
     *         - 邮箱未注册
     *         - 验证码错误或已过期
     *         - 两次密码不一致
     * @since 1.0.0
     */
    void resetPasswordByEmail(ResetPasswordRequest request);
}