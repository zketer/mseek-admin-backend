package com.lynn.museum.info.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.lynn.museum.info.dto.request.FeedbackRequest;
import com.lynn.museum.info.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现
 * @author lynn
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Value("${mail.host:smtp.163.com}")
    private String mailHost;

    @Value("${mail.port:465}")
    private Integer mailPort;

    @Value("${mail.from:}")
    private String mailFrom;

    @Value("${mail.user:}")
    private String mailUser;

    @Value("${mail.pass:}")
    private String mailPass;

    @Value("${mail.ssl:true}")
    private Boolean mailSsl;

    @Override
    public boolean sendFeedbackEmail(FeedbackRequest feedbackRequest) {
        try {
            // 检查邮件配置
            if (StrUtil.isBlank(mailFrom) || StrUtil.isBlank(mailUser) || StrUtil.isBlank(mailPass)) {
                log.warn("邮件配置不完整，无法发送邮件");
                return false;
            }

            // 配置邮件账户
            MailAccount account = new MailAccount();
            account.setHost(mailHost);
            account.setPort(mailPort);
            account.setAuth(true);
            account.setFrom(mailFrom);
            account.setUser(mailUser);
            account.setPass(mailPass);
            account.setSslEnable(mailSsl);

            // 构建邮件内容
            String emailContent = buildEmailContent(feedbackRequest);

            // 发送邮件
            MailUtil.send(
                account,
                feedbackRequest.getTo(),
                feedbackRequest.getSubject(),
                emailContent,
                false
            );

            log.info("发送反馈邮件: receiver={}, type={}", 
                feedbackRequest.getTo(), feedbackRequest.getTypeName());
            return true;

        } catch (Exception e) {
            log.error("发送反馈邮件失败：", e);
            return false;
        }
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(FeedbackRequest request) {
        StringBuilder content = new StringBuilder();
        content.append("【文博探索 - 用户反馈】\n\n");
        content.append("反馈类型：").append(request.getTypeName()).append("\n");
        content.append("提交时间：").append(request.getTimestamp()).append("\n");
        content.append("联系方式：").append(StrUtil.isNotBlank(request.getContact()) ? request.getContact() : "未提供").append("\n");
        content.append("用户环境：").append(request.getUserAgent()).append("\n");
        content.append("\n");
        content.append("反馈内容：\n");
        content.append("----------------------------------------\n");
        content.append(request.getContent());
        content.append("\n----------------------------------------\n");
        content.append("\n");
        content.append("此邮件由文博探索小程序自动发送，请及时处理用户反馈。\n");
        content.append("\n");
        content.append("文博探索团队\n");
        content.append("https://museum.example.com");
        
        return content.toString();
    }
}
