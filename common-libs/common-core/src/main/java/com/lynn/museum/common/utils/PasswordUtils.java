package com.lynn.museum.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import lombok.extern.slf4j.Slf4j;

/**
 * 密码工具类
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
public class PasswordUtils {

    /**
     * 默认盐值轮数
     */
    private static final int DEFAULT_ROUNDS = 10;

    /**
     * 加密密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        if (StrUtil.isBlank(rawPassword)) {
            throw new IllegalArgumentException("密码不能为空");
        }
        try {
            return BCrypt.hashpw(rawPassword, BCrypt.gensalt(DEFAULT_ROUNDS));
        } catch (Exception e) {
            log.error("密码加密失败", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 验证密码
     *
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (StrUtil.isBlank(rawPassword) || StrUtil.isBlank(encodedPassword)) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (Exception e) {
            log.error("密码验证失败", e);
            return false;
        }
    }

    /**
     * 生成随机密码
     *
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        if (length < 6) {
            throw new IllegalArgumentException("密码长度不能小于6位");
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        
        return password.toString();
    }

    /**
     * 生成8位随机密码
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(8);
    }

    /**
     * 验证密码强度
     *
     * @param password 密码
     * @return 强度等级：1-弱，2-中，3-强
     */
    public static int checkPasswordStrength(String password) {
        if (StrUtil.isBlank(password)) {
            return 0;
        }
        
        int score = 0;
        
        // 长度检查
        if (password.length() >= 8) {
            score++;
        }
        
        // 包含数字
        if (password.matches(".*\\d.*")) {
            score++;
        }
        
        // 包含小写字母
        if (password.matches(".*[a-z].*")) {
            score++;
        }
        
        // 包含大写字母
        if (password.matches(".*[A-Z].*")) {
            score++;
        }
        
        // 包含特殊字符
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':,.<>?].*")) {
            score++;
        }
        
        // 根据得分返回强度等级  弱、中、强
        if (score <= 2) {
            return 1;
        } else if (score <= 3) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * 验证密码是否符合要求
     *
     * @param password 密码
     * @return 是否符合要求
     */
    public static boolean isValidPassword(String password) {
        if (StrUtil.isBlank(password)) {
            return false;
        }
        
        // 长度至少6位
        if (password.length() < 6) {
            return false;
        }
        
        // 不能全是数字
        if (password.matches("^\\d+$")) {
            return false;
        }
        
        // 不能全是字母
        if (password.matches("^[a-zA-Z]+$")) {
            return false;
        }
        
        return true;
    }

}
