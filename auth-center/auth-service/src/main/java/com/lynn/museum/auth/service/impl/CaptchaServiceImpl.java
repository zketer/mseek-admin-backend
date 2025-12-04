package com.lynn.museum.auth.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.lynn.museum.auth.dto.CaptchaResponse;
import com.lynn.museum.auth.service.CaptchaService;
import com.lynn.museum.common.utils.RedisKeyBuilder;
import com.lynn.museum.common.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 验证码服务实现
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {
    
    private final RedisUtils redisUtils;
    
    /**
     * 验证码过期时间（秒），默认5分钟
     */
    @Value("${captcha.expire-time:300}")
    private long captchaExpireTime;
    
    /**
     * 验证码图片宽度
     */
    @Value("${captcha.width:130}")
    private int captchaWidth;
    
    /**
     * 验证码图片高度
     */
    @Value("${captcha.height:48}")
    private int captchaHeight;
    
    /**
     * 验证码字符个数
     */
    @Value("${captcha.code-count:4}")
    private int captchaCodeCount;
    
    /**
     * 干扰线数量
     */
    @Value("${captcha.line-count:100}")
    private int captchaLineCount;
    
    @Override
    public CaptchaResponse generateCaptcha() {
        // 生成唯一的验证码key
        String captchaKey = IdUtil.simpleUUID();
        
        // 使用Hutool生成线段干扰的验证码
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(
            captchaWidth, 
            captchaHeight, 
            captchaCodeCount, 
            captchaLineCount
        );
        
        // 获取验证码文本（不区分大小写）
        String code = lineCaptcha.getCode();
        
        // 获取验证码图片的Base64编码
        String imageBase64 = lineCaptcha.getImageBase64();
        
        // 将验证码存储到Redis
        String redisKey = RedisKeyBuilder.buildAuthCaptchaKey(captchaKey);
        redisUtils.set(redisKey, code.toLowerCase(), captchaExpireTime);
        
        log.info("🎨 生成验证码成功: key={}, code={}, 过期时间={}秒", captchaKey, code, captchaExpireTime);
        
        // 返回验证码响应
        return CaptchaResponse.builder()
                .captchaKey(captchaKey)
                .captchaImage(imageBase64)
                .expiresIn(captchaExpireTime)
                .build();
    }
    
    @Override
    public boolean verifyCaptcha(String captchaKey, String captchaCode) {
        // 参数校验
        if (StrUtil.isBlank(captchaKey) || StrUtil.isBlank(captchaCode)) {
            log.warn("❌ 验证码验证失败: 参数为空");
            return false;
        }
        
        // 从Redis获取验证码
        String redisKey = RedisKeyBuilder.buildAuthCaptchaKey(captchaKey);
        Object storedCodeObj = redisUtils.get(redisKey);
        String storedCode = storedCodeObj != null ? storedCodeObj.toString() : null;
        
        // 验证码不存在或已过期
        if (StrUtil.isBlank(storedCode)) {
            log.warn("❌ 验证码验证失败: 验证码不存在或已过期, key={}", captchaKey);
            return false;
        }
        
        // 检查验证失败次数（防止暴力破解）
        String failKey = RedisKeyBuilder.buildAuthCaptchaFailKey(captchaKey);
        Object failCountObj = redisUtils.get(failKey);
        int failCount = failCountObj != null ? Integer.parseInt(failCountObj.toString()) : 0;
        
        if (failCount >= 3) {
            // 失败次数过多，删除验证码和失败计数
            redisUtils.del(redisKey, failKey);
            log.warn("❌ 验证码验证失败: 尝试次数过多({}次), key={}", failCount, captchaKey);
            return false;
        }
        
        // 验证码比对（不区分大小写）
        boolean isValid = storedCode.equalsIgnoreCase(captchaCode.trim());
        
        if (isValid) {
            // 验证成功后删除验证码和失败计数
            redisUtils.del(redisKey, failKey);
            log.info("✅ 验证码验证成功: key={}, code={}", captchaKey, captchaCode);
        } else {
            // 验证失败，增加失败次数
            long newFailCount = redisUtils.incr(failKey, 1);
            redisUtils.expire(failKey, captchaExpireTime);
            log.warn("❌ 验证码验证失败: 验证码错误, key={}, 输入={}, 正确={}, 失败次数={}/3", 
                    captchaKey, captchaCode, storedCode, newFailCount);
            
            // 如果失败3次，删除验证码（强制刷新）
            if (newFailCount >= 3) {
                redisUtils.del(redisKey, failKey);
                log.warn("⚠️ 验证码已失效: 失败次数达到上限, key={}", captchaKey);
            }
        }
        
        return isValid;
    }
}

