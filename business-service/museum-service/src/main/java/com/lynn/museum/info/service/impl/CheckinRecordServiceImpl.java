package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lynn.museum.info.dto.AutoAuditRequest;
import com.lynn.museum.info.dto.AutoAuditResponse;
import com.lynn.museum.info.dto.CheckinAuditRequest;
import com.lynn.museum.info.dto.CheckinRecordQueryRequest;
import com.lynn.museum.info.dto.CheckinRecordResponse;
import com.lynn.museum.info.mapper.CheckinRecordMapper;
import com.lynn.museum.info.model.entity.CheckinRecord;
import com.lynn.museum.info.service.AutoAuditService;
import com.lynn.museum.info.service.CheckinRecordService;
import com.lynn.museum.api.user.client.UserApiClient;
import com.lynn.museum.api.user.dto.UserBasicInfo;
import com.lynn.museum.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 打卡记录服务实现
 *
 * @author lynn
 * @since 2024-12-16
 */
@Slf4j
@Service
public class CheckinRecordServiceImpl extends ServiceImpl<CheckinRecordMapper, CheckinRecord> implements CheckinRecordService {

    private final AutoAuditService autoAuditService;
    private final UserApiClient userApiClient;

    public CheckinRecordServiceImpl(AutoAuditService autoAuditService, UserApiClient userApiClient) {
        this.autoAuditService = autoAuditService;
        this.userApiClient = userApiClient;
    }

    @Override
    public IPage<CheckinRecordResponse> getCheckinRecords(CheckinRecordQueryRequest query) {
        Page<CheckinRecordResponse> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<CheckinRecordResponse> result = baseMapper.selectCheckinRecordsWithDetails(page, query);
        
        // 填充用户信息
        result.getRecords().forEach(record -> {
            if (record.getUserId() != null) {
                try {
                    Result<UserBasicInfo> userResult = userApiClient.getUserById(record.getUserId());
                    if (userResult != null && userResult.isSuccess() && userResult.getData() != null) {
                        UserBasicInfo user = userResult.getData();
                        record.setUserName(user.getUsername());
                        record.setUserNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    }
                } catch (Exception e) {
                    log.warn("获取用户信息失败，userId: {}, error: {}", record.getUserId(), e.getMessage());
                }
            }
        });
        
        return result;
    }

    @Override
    public CheckinRecordResponse getCheckinRecordDetail(Long id) {
        CheckinRecord checkinRecord = getById(id);
        if (checkinRecord == null) {
            throw new RuntimeException("打卡记录不存在");
        }

        CheckinRecordResponse response = new CheckinRecordResponse();
        BeanUtils.copyProperties(checkinRecord, response);
        
        // TODO: 这里可以关联查询用户和博物馆信息
        return response;
    }

    @Override
    @Transactional
    public void auditCheckinRecord(Long id, CheckinAuditRequest request) {
        CheckinRecord checkinRecord = getById(id);
        if (checkinRecord == null) {
            throw new RuntimeException("打卡记录不存在");
        }

        LambdaUpdateWrapper<CheckinRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CheckinRecord::getId, id)
                .set(CheckinRecord::getAuditStatus, request.getAuditStatus())
                .set(CheckinRecord::getAuditTime, new Date())
                .set(CheckinRecord::getAuditRemark, request.getAuditRemark());
        // TODO: 设置审核人ID，从当前登录用户获取
        // .set(CheckinRecord::getAuditUserId, getCurrentUserId())

        update(updateWrapper);
        log.info("审核打卡: id={}, status={}", id, request.getAuditStatus());
    }

    @Override
    @Transactional
    public void batchAuditCheckinRecords(List<Long> ids, CheckinAuditRequest request) {
        LambdaUpdateWrapper<CheckinRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(CheckinRecord::getId, ids)
                .set(CheckinRecord::getAuditStatus, request.getAuditStatus())
                .set(CheckinRecord::getAuditTime, new Date())
                .set(CheckinRecord::getAuditRemark, request.getAuditRemark());
        // TODO: 设置审核人ID
        // .set(CheckinRecord::getAuditUserId, getCurrentUserId())

        update(updateWrapper);
        log.info("批量审核打卡: count={}, status={}", ids.size(), request.getAuditStatus());
    }

    @Override
    @Transactional
    public void deleteCheckinRecord(Long id) {
        removeById(id);
        log.info("删除打卡记录: id={}", id);
    }

    @Override
    public IPage<CheckinRecordResponse> getAnomalyCheckinRecords(CheckinRecordQueryRequest query) {
        // 设置查询异常记录
        query.setAnomalyOnly(true);
        return getCheckinRecords(query);
    }

    @Override
    public void detectAnomalies() {
        // TODO: 实现异常检测逻辑
        // 1. 距离异常检测：距离博物馆超过一定范围
        // 2. 时间异常检测：非开放时间打卡
        // 3. 频率异常检测：同一用户短时间内多次打卡
    }

    @Override
    @Transactional
    public CheckinRecordResponse createCheckinRecord(CheckinRecord checkinRecord) {
        log.info("开始创建打卡记录，用户ID: {}, 博物馆ID: {}", 
                checkinRecord.getUserId(), checkinRecord.getMuseumId());

        // 1. 设置初始审核状态为待审核
        checkinRecord.setAuditStatus(0);
        
        // 2. 保存打卡记录
        save(checkinRecord);
        log.info("打卡记录保存成功，ID: {}", checkinRecord.getId());

        // 3. 执行自动审核
        AutoAuditResponse auditResult = performAutoAuditForRecord(checkinRecord);
        
        // 4. 更新审核结果
        updateAuditResult(checkinRecord.getId(), auditResult);

        // 5. 返回结果
        CheckinRecordResponse response = new CheckinRecordResponse();
        BeanUtils.copyProperties(checkinRecord, response);
        response.setAuditStatus(auditResult.getAuditStatus());
        response.setAuditTime(new Date());
        response.setAuditRemark(auditResult.getAuditRemark());
        
        log.info("打卡记录创建完成，ID: {}, 审核状态: {}", 
                checkinRecord.getId(), auditResult.getAuditStatus());
        
        return response;
    }

    @Override
    @Transactional
    public CheckinRecordResponse triggerAutoAudit(Long id) {
        log.info("手动触发自动审核，打卡记录ID: {}", id);

        CheckinRecord checkinRecord = getById(id);
        if (checkinRecord == null) {
            throw new RuntimeException("打卡记录不存在");
        }

        // 执行自动审核
        AutoAuditResponse auditResult = performAutoAuditForRecord(checkinRecord);
        
        // 更新审核结果
        updateAuditResult(id, auditResult);

        // 返回结果
        CheckinRecordResponse response = new CheckinRecordResponse();
        BeanUtils.copyProperties(checkinRecord, response);
        response.setAuditStatus(auditResult.getAuditStatus());
        response.setAuditTime(new Date());
        response.setAuditRemark(auditResult.getAuditRemark());
        
        log.info("手动审核完成，打卡记录ID: {}, 审核状态: {}", id, auditResult.getAuditStatus());
        
        return response;
    }

    /**
     * 为打卡记录执行自动审核
     */
    private AutoAuditResponse performAutoAuditForRecord(CheckinRecord checkinRecord) {
        try {
            // 构建自动审核请求
            AutoAuditRequest auditRequest = new AutoAuditRequest();
            auditRequest.setCheckinId(checkinRecord.getId());
            auditRequest.setUserId(checkinRecord.getUserId());
            auditRequest.setMuseumId(checkinRecord.getMuseumId());
            auditRequest.setCheckinTime(checkinRecord.getCheckinTime());
            auditRequest.setLatitude(checkinRecord.getLatitude());
            auditRequest.setLongitude(checkinRecord.getLongitude());
            auditRequest.setPhotoUrls(checkinRecord.getPhotoUrls());
            auditRequest.setRemark(checkinRecord.getRemark());
            auditRequest.setMood(checkinRecord.getMood());
            auditRequest.setDeviceInfo(checkinRecord.getDeviceInfo());

            // 调用自动审核服务
            return autoAuditService.performAutoAudit(auditRequest);
            
        } catch (Exception e) {
            log.error("自动审核执行失败，打卡记录ID: {}, 错误: {}", checkinRecord.getId(), e.getMessage());
            // 审核失败时，标记为异常，需要人工审核
            return AutoAuditResponse.anomaly("自动审核系统异常：" + e.getMessage(), "system_error");
        }
    }

    /**
     * 更新审核结果
     */
    private void updateAuditResult(Long checkinId, AutoAuditResponse auditResult) {
        LambdaUpdateWrapper<CheckinRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CheckinRecord::getId, checkinId)
                .set(CheckinRecord::getAuditStatus, auditResult.getAuditStatus())
                .set(CheckinRecord::getAuditTime, new Date())
                .set(CheckinRecord::getAuditRemark, auditResult.getAuditRemark())
                .set(CheckinRecord::getAnomalyType, auditResult.getAnomalyType())
                // -1 表示系统自动审核
                .set(CheckinRecord::getAuditUserId, -1L);

        update(updateWrapper);
    }
}
