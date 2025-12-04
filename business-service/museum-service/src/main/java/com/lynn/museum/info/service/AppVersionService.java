package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lynn.museum.info.dto.*;
import com.lynn.museum.info.model.entity.AppVersion;

import java.util.Map;

/**
 * 应用版本服务接口
 *
 * @author lynn
 * @since 2025-10-27
 */
public interface AppVersionService extends IService<AppVersion> {

    /**
     * 分页查询版本列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<AppVersionResponse> getAppVersions(AppVersionQueryRequest query);

    /**
     * 获取版本详情
     *
     * @param id 版本ID
     * @return 版本详情
     */
    AppVersionResponse getAppVersionDetail(Long id);

    /**
     * 创建版本
     *
     * @param request 创建请求
     * @return 版本ID
     */
    Long createAppVersion(AppVersionCreateRequest request);

    /**
     * 更新版本
     *
     * @param id 版本ID
     * @param request 更新请求
     */
    void updateAppVersion(Long id, AppVersionUpdateRequest request);

    /**
     * 删除版本
     *
     * @param id 版本ID
     */
    void deleteAppVersion(Long id);

    /**
     * 获取版本统计信息
     *
     * @return 统计信息
     */
    AppVersionStatsResponse getAppVersionStats();

    /**
     * 更新下载次数
     *
     * @param id 版本ID
     */
    void updateDownloadCount(Long id);

    /**
     * 标记为最新版本
     *
     * @param id 版本ID
     */
    void markAsLatest(Long id);

    /**
     * 发布版本
     *
     * @param id 版本ID
     */
    void publishVersion(Long id);

    /**
     * 废弃版本
     *
     * @param id 版本ID
     */
    void deprecateVersion(Long id);

    /**
     * 获取文件下载URL
     *
     * @param id 版本ID
     * @return 下载URL
     */
    String getDownloadUrl(Long id);

    /**
     * 获取各平台最新版本
     *
     * @return Map<平台, 版本信息>
     */
    Map<String, AppVersionResponse> getLatestVersions();
}

