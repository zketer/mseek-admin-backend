package com.lynn.museum.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.system.dto.OperationLogQueryRequest;
import com.lynn.museum.system.dto.OperationLogResponse;
import com.lynn.museum.system.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 操作日志管理控制器
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "OperationLogController", description = "操作日志管理相关接口")
@RestController
@RequestMapping("/operation-logs")
@RequiredArgsConstructor
@Validated
public class OperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "根据ID查询操作日志")
    @GetMapping("/{id}")
    public Result<OperationLogResponse> getOperateLogById(
            @Parameter(description = "日志ID") @PathVariable @NotNull Long id) {
        OperationLogResponse log = operationLogService.getById(id);
        return Result.success(log);
    }

    @Operation(summary = "分页查询操作日志列表")
    @GetMapping
    public Result<IPage<OperationLogResponse>> getOperateLogPage(@Valid OperationLogQueryRequest query) {
        IPage<OperationLogResponse> page = operationLogService.getPage(query);
        return Result.success(page);
    }

    @Operation(summary = "获取操作日志统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> statistics = operationLogService.getStatistics(days);
        return Result.success(statistics);
    }

    @Operation(summary = "获取热门操作排行")
    @GetMapping("/popular-operations")
    public Result<List<Map<String, Object>>> getPopularOperations(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") Integer days,
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> operations = operationLogService.getPopularOperations(days, limit);
        return Result.success(operations);
    }

    @Operation(summary = "获取用户操作统计")
    @GetMapping("/user-statistics")
    public Result<List<Map<String, Object>>> getUserOperationStatistics(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") Integer days,
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> statistics = operationLogService.getUserOperationStatistics(days, limit);
        return Result.success(statistics);
    }

    @Operation(summary = "删除操作日志")
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(
            @Parameter(description = "日志ID") @PathVariable @NotNull Long id) {
        operationLogService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "批量删除操作日志")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(
            @Parameter(description = "日志ID列表") @RequestBody @NotEmpty List<Long> ids) {
        operationLogService.deleteBatch(ids);
        return Result.success();
    }

    @Operation(summary = "清理指定天数前的日志")
    @DeleteMapping("/clean")
    public Result<Integer> cleanLogs(
            @Parameter(description = "保留天数") @RequestParam Integer days) {
        int count = operationLogService.cleanLogs(days);
        return Result.success(count);
    }

    @Operation(summary = "导出操作日志")
    @PostMapping("/export")
    public void exportLogs(
            @Valid @RequestBody OperationLogQueryRequest query,
            HttpServletResponse response) {
        operationLogService.exportLogs(query, response);
    }

}
