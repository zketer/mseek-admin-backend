package com.lynn.museum.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应DTO
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaResponse {
    
    /**
     * 验证码Key（UUID）
     */
    private String captchaKey;
    
    /**
     * 验证码图片（Base64编码）
     */
    private String captchaImage;
    
    /**
     * 过期时间（秒）
     */
    private Long expiresIn;
}

