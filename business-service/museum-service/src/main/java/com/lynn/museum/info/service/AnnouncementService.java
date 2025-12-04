package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.info.dto.AnnouncementCreateRequest;
import com.lynn.museum.info.dto.AnnouncementQueryRequest;
import com.lynn.museum.info.dto.AnnouncementResponse;

import java.util.List;

/**
 * 公告服务接口
 */
public interface AnnouncementService {

    /**
     * 分页查询公告
     */
    IPage<AnnouncementResponse> getAnnouncementList(AnnouncementQueryRequest request);

    /**
     * 获取公告详情
     */
    AnnouncementResponse getAnnouncementDetail(Long id);

    /**
     * 创建公告
     */
    Long createAnnouncement(AnnouncementCreateRequest request);

    /**
     * 更新公告
     */
    boolean updateAnnouncement(Long id, AnnouncementCreateRequest request);

    /**
     * 删除公告
     */
    boolean deleteAnnouncement(Long id);

    /**
     * 更新公告状态
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 发布公告
     */
    boolean publishAnnouncement(Long id);

    /**
     * 下线公告
     */
    boolean offlineAnnouncement(Long id);

    /**
     * 更新启用状态
     * @param id 公告ID
     * @param enabled 启用状态：0-禁用，1-启用
     * @return 更新是否成功
     */
    boolean updateEnabled(Long id, Integer enabled);

    /**
     * 获取启用的公告列表（管理端查询）
     * @param request 查询请求
     * @return 分页结果
     */
    IPage<AnnouncementResponse> getEnabledAnnouncementList(AnnouncementQueryRequest request);

    /**
     * 获取有效公告列表（小程序端）
     */
    List<AnnouncementResponse> getActiveAnnouncements(Integer limit);

    /**
     * 增加阅读次数
     */
    boolean incrementReadCount(Long id);
}
