package com.lynn.museum.info.service;

import com.lynn.museum.info.dto.AutoAuditRequest;
import com.lynn.museum.info.dto.AutoAuditResponse;

/**
 * 自动审核服务接口
 * 
 * 此接口为自动审核系统预留，用于与外部AI审核系统对接
 *
 * @author lynn
 * @since 2024-12-16
 */
public interface AutoAuditService {

    /**
     * 执行自动审核
     * 
     * 调用外部自动审核系统，对打卡记录进行智能审核
     * 
     * @param request 自动审核请求
     * @return 审核结果
     */
    AutoAuditResponse performAutoAudit(AutoAuditRequest request);

    /**
     * 检查自动审核系统是否可用
     * 
     * @return true-可用，false-不可用
     */
    boolean isAutoAuditEnabled();

    /**
     * 获取审核系统状态
     * 
     * @return 状态信息
     */
    String getAutoAuditSystemStatus();
}
