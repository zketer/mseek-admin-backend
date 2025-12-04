package com.lynn.museum.info.service;

import com.lynn.museum.info.dto.request.FeedbackRequest;

/**
 * 邮件服务接口
 * @author lynn
 */
public interface EmailService {

    /**
     * 发送反馈邮件
     * @param feedbackRequest 反馈请求
     * @return 是否发送成功
     */
    boolean sendFeedbackEmail(FeedbackRequest feedbackRequest);
}
