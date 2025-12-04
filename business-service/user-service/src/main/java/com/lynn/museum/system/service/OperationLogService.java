package com.lynn.museum.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.system.dto.OperationLogQueryRequest;
import com.lynn.museum.system.dto.OperationLogResponse;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 操作日志服务接口
 * 
 * @author lynn
 * @since 2024-01-01
 */
public interface OperationLogService {

    /**
     * 根据ID查询操作日志
     */
    OperationLogResponse getById(Long id);

    /**
     * 分页查询操作日志列表
     */
    IPage<OperationLogResponse> getPage(OperationLogQueryRequest query);

    /**
     * 获取操作日志统计
     */
    Map<String, Object> getStatistics(Integer days);

    /**
     * 获取热门操作排行
     */
    List<Map<String, Object>> getPopularOperations(Integer days, Integer limit);

    /**
     * 获取用户操作统计
     */
    List<Map<String, Object>> getUserOperationStatistics(Integer days, Integer limit);

    /**
     * 删除操作日志
     */
    void deleteById(Long id);

    /**
     * 批量删除操作日志
     */
    void deleteBatch(List<Long> ids);

    /**
     * 清理指定天数前的日志
     */
    int cleanLogs(Integer days);

    /**
     * 导出操作日志
     */
    void exportLogs(OperationLogQueryRequest query, HttpServletResponse response);

}
