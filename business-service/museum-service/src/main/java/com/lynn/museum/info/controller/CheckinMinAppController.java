package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.*;
import com.lynn.museum.info.model.entity.CheckinRecord;
import com.lynn.museum.info.service.CheckinMiniappService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序打卡控制器
 *
 * @author lynn
 * @since 2024-12-30
 */
@Slf4j
@RestController
@RequestMapping("/miniapp/checkin")
@RequiredArgsConstructor
@Tag(name = "CheckinMiniAppController", description = "小程序打卡功能")
public class CheckinMinAppController {

    private final CheckinMiniappService checkinMiniappService;

    @PostMapping("/submit")
    @Operation(summary = "提交打卡/暂存", description = "提交打卡记录或保存为暂存")
    public Result<CheckinSubmitResponse> submitCheckin(
            @Valid @RequestBody CheckinSubmitRequest request,
            @RequestHeader("userId") Long userId) {
        log.info("用户{}提交打卡，博物馆ID: {}, 是否暂存: {}", userId, request.getMuseumId(), request.getIsDraft());
        
        CheckinSubmitResponse response = checkinMiniappService.submitCheckin(request, userId);
        return Result.success(response);
    }

    @GetMapping("/records")
    @Operation(summary = "获取打卡记录列表", description = "分页获取用户的打卡记录，支持搜索和筛选")
    public Result<IPage<CheckinRecord>> getCheckinRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long museumId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Boolean isDraft,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filterType,
            @RequestHeader("userId") Long userId) {
        log.info("用户{}获取打卡记录，页码: {}, 页大小: {}, 关键词: {}, 筛选类型: {}", 
                userId, page, pageSize, keyword, filterType);
        
        CheckinRecordQueryRequest query = new CheckinRecordQueryRequest();
        query.setPage(page);
        query.setPageSize(pageSize);
        query.setUserId(userId);
        query.setMuseumId(museumId);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setIsDraft(isDraft);
        query.setKeyword(keyword);
        query.setFilterType(filterType);
        
        IPage<CheckinRecord> result = checkinMiniappService.getCheckinRecords(query);
        return Result.success(result);
    }

    @GetMapping("/{checkinId}")
    @Operation(summary = "获取打卡详情", description = "获取指定打卡记录的详细信息")
    public Result<CheckinRecord> getCheckinDetail(
            @Parameter(description = "打卡记录ID") @PathVariable Long checkinId,
            @RequestHeader("userId") Long userId) {
        log.info("用户{}获取打卡详情，记录ID: {}", userId, checkinId);
        
        CheckinRecord result = checkinMiniappService.getCheckinDetail(checkinId, userId);
        return Result.success(result);
    }

    @DeleteMapping("/draft/{draftId}")
    @Operation(summary = "删除暂存记录", description = "删除指定的暂存记录")
    public Result<Boolean> deleteDraft(
            @Parameter(description = "暂存ID") @PathVariable String draftId,
            @RequestHeader("userId") Long userId) {
        log.info("用户{}删除暂存记录，暂存ID: {}", userId, draftId);
        
        Boolean result = checkinMiniappService.deleteDraft(draftId, userId);
        return Result.success(result);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取打卡统计", description = "获取用户的打卡统计信息")
    public Result<CheckinStatsResponse> getCheckinStats(
            @RequestHeader("userId") Long userId) {
        log.info("用户{}获取打卡统计", userId);
        
        CheckinStatsResponse result = checkinMiniappService.getCheckinStats(userId);
        return Result.success(result);
    }

    @PostMapping("/draft/{draftId}/convert")
    @Operation(summary = "将暂存转为正式打卡", description = "将暂存记录转换为正式打卡记录")
    public Result<CheckinSubmitResponse> convertDraftToCheckin(
            @Parameter(description = "暂存ID") @PathVariable String draftId,
            @RequestHeader("userId") Long userId) {
        log.info("用户{}将暂存转为正式打卡，暂存ID: {}", userId, draftId);
        
        CheckinSubmitResponse result = checkinMiniappService.convertDraftToCheckin(draftId, userId);
        return Result.success(result);
    }

    @DeleteMapping("/{checkinId}")
    @Operation(summary = "删除打卡记录", description = "删除指定的正式打卡记录")
    public Result<Boolean> deleteCheckinRecord(
            @Parameter(description = "打卡记录ID") @PathVariable Long checkinId,
            @RequestHeader("userId") Long userId) {
        log.info("用户{}删除打卡记录，记录ID: {}", userId, checkinId);
        
        Boolean result = checkinMiniappService.deleteCheckinRecord(checkinId, userId);
        return Result.success(result);
    }

    @GetMapping("/stats/provinces")
    @Operation(summary = "获取省份打卡统计", description = "获取用户的省份足迹地图统计数据")
    public Result<ProvinceCheckinStatsResponse> getProvinceStats(
            @RequestHeader("userId") Long userId) {
        log.info("用户{}获取省份打卡统计", userId);
        
        ProvinceCheckinStatsResponse result = checkinMiniappService.getProvinceStats(userId);
        return Result.success(result);
    }

    @GetMapping("/provinces/{provinceCode}/museums")
    @Operation(summary = "获取省份博物馆详情", description = "获取指定省份的博物馆列表及用户访问状态")
    public Result<ProvinceMuseumDetailResponse> getProvinceMuseumDetail(
            @Parameter(description = "省份编码") @PathVariable String provinceCode,
            @RequestHeader("userId") Long userId) {
        log.info("用户{}获取省份{}的博物馆详情", userId, provinceCode);
        
        ProvinceMuseumDetailResponse result = checkinMiniappService.getProvinceMuseumDetail(provinceCode, userId);
        return Result.success(result);
    }
}
