package com.lynn.museum.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Token验证结果DTO
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 是否有效
     */
    private boolean valid;
    
    /**
     * 错误信息（验证失败时）
     */
    private String errorMessage;
    
    /**
     * 创建成功的验证结果
     */
    public static TokenValidationResult success(Long userId, String username) {
        return new TokenValidationResult(userId, username, true, null);
    }
    
    /**
     * 创建失败的验证结果
     */
    public static TokenValidationResult failure(String errorMessage) {
        return new TokenValidationResult(null, null, false, errorMessage);
    }
}
