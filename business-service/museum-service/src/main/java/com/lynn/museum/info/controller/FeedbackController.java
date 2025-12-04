package com.lynn.museum.info.controller;

import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.request.FeedbackRequest;
import com.lynn.museum.info.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 反馈建议控制器
 */
@Slf4j
@RestController
@RequestMapping("/feedback")
@Tag(name = "FeedbackController", description = "用户反馈建议相关接口")
public class FeedbackController {

    @Resource
    private EmailService emailService;

    /**
     * 提交反馈建议
     */
    @PostMapping("/submit")
    @Operation(summary = "提交反馈建议", description = "用户提交反馈建议，发送邮件通知")
    public Result<String> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        try {
            log.info("收到用户反馈，类型：{}, 内容长度：{}", request.getTypeName(), request.getContent().length());
            
            // 发送邮件
            boolean success = emailService.sendFeedbackEmail(request);
            
            if (success) {
                log.info("反馈邮件发送成功");
                return Result.success("反馈提交成功，我们会尽快处理您的建议");
            } else {
                log.warn("反馈邮件发送失败");
                return Result.error("反馈提交失败，请稍后重试或直接联系客服");
            }
            
        } catch (Exception e) {
            log.error("处理反馈提交时发生错误：", e);
            return Result.error("系统异常，请稍后重试");
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查反馈服务是否正常")
    public Result<String> health() {
        return Result.success("反馈服务运行正常");
    }
}
