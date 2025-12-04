package com.lynn.museum.auth.service.impl;

import com.lynn.museum.auth.config.EmailConfig;
import com.lynn.museum.auth.service.EmailService;
import com.lynn.museum.common.utils.RedisKeyBuilder;
import com.lynn.museum.common.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * 邮件服务实现类
 * 
 * @author lynn
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailConfig emailConfig;
    private final RedisUtils redisUtils;


    @Override
    public String sendRegisterCode(String email) {
        try {
            // 生成6位数字验证码
            String code = generateCode();
            
            // 存储到Redis，设置5分钟过期（转换为秒）
            String key = RedisKeyBuilder.buildAuthEmailCodeKey(email);
            long expireSeconds = emailConfig.getCodeExpireMinutes() * 60;
            redisUtils.set(key, code, expireSeconds);
            
            // 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailConfig.getFrom() != null ? emailConfig.getFrom() : emailConfig.getUsername());
            message.setTo(email);
            message.setSubject("【文博探索】注册验证码");
            message.setText(String.format(
                "您好！\n\n" +
                "您正在注册文博探索系统账号，验证码为：%s\n\n" +
                "验证码有效期为%d分钟，请尽快完成注册。\n\n" +
                "如非本人操作，请忽略此邮件。\n\n" +
                "文博探索团队\n" +
                "%s",
                code,
                emailConfig.getCodeExpireMinutes(),
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ));
            
            mailSender.send(message);
            log.info("📧 注册验证码已发送至邮箱: {}", email);
            
            return code;
        } catch (Exception e) {
            log.error("❌ 发送注册验证码失败: {}", email, e);
            throw new RuntimeException("发送验证码失败，请稍后重试");
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        try {
            // 检查邮箱验证码（统一的键格式，适用于注册和密码重置）
            String emailCodeKey = RedisKeyBuilder.buildAuthEmailCodeKey(email);
            String storedCode = (String) redisUtils.get(emailCodeKey);

            if (storedCode != null && storedCode.equals(code)) {
                // 验证成功后删除验证码
                redisUtils.del(emailCodeKey);
                log.info("✅ 邮箱验证码验证成功: {}", email);
                return true;
            }
            
            log.warn("⚠️ 验证码验证失败: {} - 验证码错误或已过期", email);
            return false;
        } catch (Exception e) {
            log.error("❌ 验证码验证异常: {}", email, e);
            return false;
        }
    }

    @Override
    public String sendPasswordResetCode(String email) {
        try {
            // 生成6位数字验证码
            String code = generateCode();
            
            // 存储到Redis，设置5分钟过期（转换为秒）
            String key = RedisKeyBuilder.buildAuthEmailCodeKey(email);
            long expireSeconds = emailConfig.getCodeExpireMinutes() * 60;
            redisUtils.set(key, code, expireSeconds);
            
            // 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailConfig.getFrom() != null ? emailConfig.getFrom() : emailConfig.getUsername());
            message.setTo(email);
            message.setSubject("【文博探索】密码重置验证码");
            message.setText(String.format(
                "您好！\n\n" +
                "您正在重置文博探索系统账号密码，验证码为：%s\n\n" +
                "验证码有效期为%d分钟，请尽快完成密码重置。\n\n" +
                "如非本人操作，请立即修改密码并联系管理员。\n\n" +
                "文博探索团队\n" +
                "%s",
                code,
                emailConfig.getCodeExpireMinutes(),
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ));
            
            mailSender.send(message);
            log.info("📧 密码重置验证码已发送至邮箱: {}", email);
            
            return code;
        } catch (Exception e) {
            log.error("❌ 发送密码重置验证码失败: {}", email, e);
            throw new RuntimeException("发送验证码失败，请稍后重试");
        }
    }

    /**
     * 生成6位数字验证码
     * 
     * @return 验证码
     */
    private String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}

