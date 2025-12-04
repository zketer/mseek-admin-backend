package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lynn.museum.info.dto.CheckinAuditRequest;
import com.lynn.museum.info.dto.CheckinRecordQueryRequest;
import com.lynn.museum.info.dto.CheckinRecordResponse;
import com.lynn.museum.info.model.entity.CheckinRecord;

import java.util.List;

/**
 * 打卡记录服务接口
 *
 * @author lynn
 * @since 2024-12-16
 */
public interface CheckinRecordService extends IService<CheckinRecord> {

    /**
     * 分页查询打卡记录
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<CheckinRecordResponse> getCheckinRecords(CheckinRecordQueryRequest query);

    /**
     * 获取打卡记录详情
     *
     * @param id 打卡记录ID
     * @return 打卡记录详情
     */
    CheckinRecordResponse getCheckinRecordDetail(Long id);

    /**
     * 审核打卡记录
     *
     * @param id 打卡记录ID
     * @param request 审核请求
     */
    void auditCheckinRecord(Long id, CheckinAuditRequest request);

    /**
     * 批量审核打卡记录
     *
     * @param ids 打卡记录ID列表
     * @param request 审核请求
     */
    void batchAuditCheckinRecords(List<Long> ids, CheckinAuditRequest request);

    /**
     * 删除打卡记录
     *
     * @param id 打卡记录ID
     */
    void deleteCheckinRecord(Long id);

    /**
     * 获取异常打卡记录
     *
     * @param query 查询条件
     * @return 异常记录列表
     */
    IPage<CheckinRecordResponse> getAnomalyCheckinRecords(CheckinRecordQueryRequest query);

    /**
     * 异常检测
     */
    void detectAnomalies();

    /**
     * 创建打卡记录（集成自动审核）
     *
     * @param checkinRecord 打卡记录
     * @return 创建的打卡记录（包含审核结果）
     */
    CheckinRecordResponse createCheckinRecord(CheckinRecord checkinRecord);

    /**
     * 手动触发自动审核
     *
     * @param id 打卡记录ID
     * @return 审核结果
     */
    CheckinRecordResponse triggerAutoAudit(Long id);
}
