package com.lynn.museum.auth.service;

import com.lynn.museum.auth.dto.CaptchaResponse;

/**
 * 验证码服务接口
 * 
 * @author lynn
 * @since 2024-01-01
 */
public interface CaptchaService {
    
    /**
     * 生成图形验证码
     * 
     * @return 验证码响应（包含key和图片）
     */
    CaptchaResponse generateCaptcha();
    
    /**
     * 验证图形验证码
     * 
     * @param captchaKey 验证码key
     * @param captchaCode 用户输入的验证码
     * @return 是否验证通过
     */
    boolean verifyCaptcha(String captchaKey, String captchaCode);
}

