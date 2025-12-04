package com.lynn.museum.info.service.impl;

import com.lynn.museum.info.dto.AutoAuditRequest;
import com.lynn.museum.info.dto.AutoAuditResponse;
import com.lynn.museum.info.service.AutoAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 自动审核服务实现
 * 
 * 当前为模拟实现，后续可以对接真实的AI审核系统
 *
 * @author lynn
 * @since 2024-12-16
 */
@Slf4j
@Service
public class AutoAuditServiceImpl implements AutoAuditService {

    @Value("${app.auto-audit.enabled:false}")
    private boolean autoAuditEnabled;

    @Value("${app.auto-audit.api-url:http://localhost:8080/api/audit}")
    private String autoAuditApiUrl;

    @Value("${app.auto-audit.timeout:5000}")
    private int timeoutMs;

    // 注入RestTemplate用于HTTP调用（需要在配置类中定义Bean）
    // private final RestTemplate restTemplate;

    @Override
    public AutoAuditResponse performAutoAudit(AutoAuditRequest request) {

        if (!autoAuditEnabled) {
            return AutoAuditResponse.approved("自动审核系统未启用，默认通过");
        }

        try {
            // TODO: 实际对接自动审核系统的调用
            // 当前为模拟实现，可以根据实际需求修改
            return performMockAudit(request);

            // 真实实现示例：
            // HttpHeaders headers = new HttpHeaders();
            // headers.setContentType(MediaType.APPLICATION_JSON);
            // HttpEntity<AutoAuditRequest> entity = new HttpEntity<>(request, headers);
            // 
            // AutoAuditResponse response = restTemplate.postForObject(
            //     autoAuditApiUrl, entity, AutoAuditResponse.class);
            // 
            // log.info("自动审核完成，结果: {}", response.getAuditStatus());
            // return response;

        } catch (Exception e) {
            log.error("自动审核系统调用失败，打卡记录ID: {}, 错误: {}", request.getCheckinId(), e.getMessage());
            // 审核系统异常时，默认标记为需要人工审核
            return AutoAuditResponse.anomaly("自动审核系统异常：" + e.getMessage(), "system_error");
        }
    }

    @Override
    public boolean isAutoAuditEnabled() {
        return autoAuditEnabled;
    }

    @Override
    public String getAutoAuditSystemStatus() {
        if (!autoAuditEnabled) {
            return "自动审核系统已禁用";
        }

        try {
            // TODO: 实际检查审核系统健康状态
            // String healthUrl = autoAuditApiUrl + "/health";
            // ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            // return response.getStatusCode().is2xxSuccessful() ? "正常" : "异常";
            
            return "模拟状态：正常";
        } catch (Exception e) {
            return "异常：" + e.getMessage();
        }
    }

    /**
     * 模拟自动审核逻辑
     * 后续替换为真实的AI审核系统调用
     * 
     * 当前策略：宽松审核，只拒绝明显违规内容，其他都通过
     */
    private AutoAuditResponse performMockAudit(AutoAuditRequest request) {

        // 模拟审核逻辑：基于简单规则进行判断
        
        // 1. 检查备注内容是否包含敏感词（严重违规才拒绝）
        if (request.getRemark() != null && containsSensitiveWords(request.getRemark())) {
            return AutoAuditResponse.rejected("包含不当内容", "content_violation");
        }

        // 2. 其他情况都默认通过
        // 注意：照片和位置信息不作为拒绝理由，因为用户可能在不同场景下打卡
        
        String auditMessage = "自动审核通过";
        
//        // 添加提示信息（不影响审核结果）
//        if (request.getPhotoUrls() == null || request.getPhotoUrls().trim().isEmpty()) {
//            auditMessage += "（建议添加打卡照片）";
//        }
//        if (request.getLatitude() == null || request.getLongitude() == null) {
//            auditMessage += "（未获取到位置信息）";
//        }

        // 默认通过
        return AutoAuditResponse.approved(auditMessage);
    }

    /**
     * 敏感词检测（模拟实现）
     */
    private boolean containsSensitiveWords(String content) {
        // 模拟敏感词列表
        String[] sensitiveWords = {"广告", "推广", "联系方式", "微信", "QQ"};
        
        for (String word : sensitiveWords) {
            if (content.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
