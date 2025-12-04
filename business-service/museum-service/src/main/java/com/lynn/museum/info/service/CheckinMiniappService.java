package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.info.dto.*;
import com.lynn.museum.info.model.entity.CheckinRecord;

/**
 * 小程序打卡服务接口
 *
 * @author lynn
 * @since 2024-12-30
 */
public interface CheckinMiniappService {

    /**
     * 提交打卡/暂存
     */
    CheckinSubmitResponse submitCheckin(CheckinSubmitRequest request, Long userId);

    /**
     * 获取打卡记录列表
     */
    IPage<CheckinRecord> getCheckinRecords(CheckinRecordQueryRequest query);

    /**
     * 获取打卡详情
     */
    CheckinRecord getCheckinDetail(Long checkinId, Long userId);

    /**
     * 删除暂存记录
     */
    Boolean deleteDraft(String draftId, Long userId);

    /**
     * 获取打卡统计
     */
    CheckinStatsResponse getCheckinStats(Long userId);

    /**
     * 将暂存转为正式打卡
     */
    CheckinSubmitResponse convertDraftToCheckin(String draftId, Long userId);

    /**
     * 删除打卡记录（正式打卡）
     */
    Boolean deleteCheckinRecord(Long checkinId, Long userId);

    /**
     * 获取省份打卡统计
     */
    ProvinceCheckinStatsResponse getProvinceStats(Long userId);

    /**
     * 获取省份博物馆详情
     */
    ProvinceMuseumDetailResponse getProvinceMuseumDetail(String provinceCode, Long userId);
}
