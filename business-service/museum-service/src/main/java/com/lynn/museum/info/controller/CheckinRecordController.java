package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.CheckinAuditRequest;
import com.lynn.museum.info.dto.CheckinRecordQueryRequest;
import com.lynn.museum.info.dto.CheckinRecordResponse;
import com.lynn.museum.info.model.entity.CheckinRecord;
import com.lynn.museum.info.service.AutoAuditService;
import com.lynn.museum.info.service.CheckinRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 打卡记录管理控制器
 *
 * @author lynn
 * @since 2024-12-16
 */
@Slf4j
@RestController
@RequestMapping("/checkin-records")
@RequiredArgsConstructor
@Tag(name = "CheckinRecordController", description = "打卡记录的增删改查、审核等功能")
public class CheckinRecordController {

    private final CheckinRecordService checkinRecordService;
    private final AutoAuditService autoAuditService;

    @GetMapping
    // @PreAuthorize("hasAuthority('checkin:record:query')")
    @Operation(summary = "分页查询打卡记录", description = "根据条件分页查询打卡记录列表")
    public Result<IPage<CheckinRecordResponse>> getCheckinRecords(@Valid CheckinRecordQueryRequest query) {
        log.info("分页查询打卡记录，查询条件: {}", query);
        IPage<CheckinRecordResponse> result = checkinRecordService.getCheckinRecords(query);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    // @PreAuthorize("hasAuthority('checkin:record:query')")
    @Operation(summary = "获取打卡记录详情", description = "根据ID获取打卡记录的详细信息")
    public Result<CheckinRecordResponse> getCheckinRecordDetail(
            @Parameter(description = "打卡记录ID") @PathVariable Long id) {
        log.info("获取打卡记录详情，ID: {}", id);
        CheckinRecordResponse result = checkinRecordService.getCheckinRecordDetail(id);
        return Result.success(result);
    }

    @PutMapping("/{id}/audit")
    // @PreAuthorize("hasAuthority('checkin:record:audit')")
    @Operation(summary = "审核打卡记录", description = "对指定的打卡记录进行审核")
    public Result<Void> auditCheckinRecord(
            @Parameter(description = "打卡记录ID") @PathVariable Long id,
            @Valid @RequestBody CheckinAuditRequest request) {
        log.info("审核打卡记录，ID: {}, 审核请求: {}", id, request);
        checkinRecordService.auditCheckinRecord(id, request);
        return Result.success();
    }

    @PutMapping("/batch-audit")
    // @PreAuthorize("hasAuthority('checkin:record:audit')")
    @Operation(summary = "批量审核打卡记录", description = "批量审核多个打卡记录")
    public Result<Void> batchAuditCheckinRecords(@Valid @RequestBody BatchCheckinAuditRequest request) {
        log.info("批量审核打卡记录，审核请求: {}", request);
        checkinRecordService.batchAuditCheckinRecords(request.getIds(), request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAuthority('checkin:record:delete')")
    @Operation(summary = "删除打卡记录", description = "删除指定的打卡记录")
    public Result<Void> deleteCheckinRecord(
            @Parameter(description = "打卡记录ID") @PathVariable Long id) {
        log.info("删除打卡记录，ID: {}", id);
        checkinRecordService.deleteCheckinRecord(id);
        return Result.success();
    }

    @GetMapping("/anomalies")
    // @PreAuthorize("hasAuthority('checkin:record:anomalies')")
    @Operation(summary = "获取异常打卡记录", description = "获取系统检测到的异常打卡记录")
    public Result<IPage<CheckinRecordResponse>> getAnomalyCheckinRecords(@Valid CheckinRecordQueryRequest query) {
        log.info("获取异常打卡记录，查询条件: {}", query);
        IPage<CheckinRecordResponse> result = checkinRecordService.getAnomalyCheckinRecords(query);
        return Result.success(result);
    }

    @PostMapping("/detect-anomalies")
    // @PreAuthorize("hasAuthority('checkin:record:anomalies')")
    @Operation(summary = "执行异常检测", description = "手动触发异常检测任务")
    public Result<Void> detectAnomalies() {
        log.info("执行异常检测任务");
        checkinRecordService.detectAnomalies();
        return Result.success();
    }

    @PostMapping
    // @PreAuthorize("hasAuthority('checkin:record:create')")
    @Operation(summary = "创建打卡记录", description = "创建新的打卡记录并自动审核")
    public Result<CheckinRecordResponse> createCheckinRecord(@Valid @RequestBody CheckinRecord checkinRecord) {
        log.info("创建打卡记录，用户ID: {}, 博物馆ID: {}", checkinRecord.getUserId(), checkinRecord.getMuseumId());
        CheckinRecordResponse result = checkinRecordService.createCheckinRecord(checkinRecord);
        return Result.success(result);
    }

    @PostMapping("/{id}/auto-audit")
    // @PreAuthorize("hasAuthority('checkin:record:audit')")
    @Operation(summary = "手动触发自动审核", description = "对指定的打卡记录手动触发自动审核")
    public Result<CheckinRecordResponse> triggerAutoAudit(
            @Parameter(description = "打卡记录ID") @PathVariable Long id) {
        log.info("手动触发自动审核，打卡记录ID: {}", id);
        CheckinRecordResponse result = checkinRecordService.triggerAutoAudit(id);
        return Result.success(result);
    }

    @GetMapping("/auto-audit/status")
    // @PreAuthorize("hasAuthority('checkin:record:audit')")
    @Operation(summary = "获取自动审核系统状态", description = "检查自动审核系统的运行状态")
    public Result<String> getAutoAuditStatus() {
        log.info("获取自动审核系统状态");
        String status = autoAuditService.getAutoAuditSystemStatus();
        return Result.success(status);
    }

    /**
     * 批量审核请求
     */
    public static class BatchCheckinAuditRequest extends CheckinAuditRequest {
        @Parameter(description = "打卡记录ID列表")
        private List<Long> ids;

        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }
    }
}
