package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.info.dto.ExhibitionResponse;
import com.lynn.museum.info.dto.MuseumResponse;

/**
 * 用户收藏服务接口
 *
 * @author lynn
 * @since 2024-01-01
 */
public interface MiniAppUserFavoriteService {

    /**
     * 收藏博物馆
     *
     * @param userId 用户ID
     * @param museumId 博物馆ID
     * @return 是否收藏成功
     */
    Boolean favoriteMuseum(Long userId, Long museumId);

    /**
     * 取消收藏博物馆
     *
     * @param userId 用户ID
     * @param museumId 博物馆ID
     * @return 是否取消成功
     */
    Boolean unfavoriteMuseum(Long userId, Long museumId);

    /**
     * 收藏展览
     *
     * @param userId 用户ID
     * @param exhibitionId 展览ID
     * @return 是否收藏成功
     */
    Boolean favoriteExhibition(Long userId, Long exhibitionId);

    /**
     * 取消收藏展览
     *
     * @param userId 用户ID
     * @param exhibitionId 展览ID
     * @return 是否取消成功
     */
    Boolean unfavoriteExhibition(Long userId, Long exhibitionId);

    /**
     * 分页查询用户收藏的博物馆
     *
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 页面大小
     * @param keyword 搜索关键词
     * @param visitStatus 打卡状态
     * @param sortBy 排序方式
     * @return 分页结果
     */
    IPage<MuseumResponse> getUserFavoriteMuseums(Long userId, Integer page, Integer pageSize, 
                                                String keyword, Boolean visitStatus, String sortBy);

    /**
     * 分页查询用户收藏的展览
     *
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 页面大小
     * @param keyword 搜索关键词
     * @param status 展览状态
     * @param sortBy 排序方式
     * @return 分页结果
     */
    IPage<ExhibitionResponse> getUserFavoriteExhibitions(Long userId, Integer page, Integer pageSize,
                                                        String keyword, Integer status, String sortBy);

    /**
     * 检查用户是否收藏了指定博物馆
     *
     * @param userId 用户ID
     * @param museumId 博物馆ID
     * @return 是否收藏
     */
    Boolean isMuseumFavorited(Long userId, Long museumId);

    /**
     * 检查用户是否收藏了指定展览
     *
     * @param userId 用户ID
     * @param exhibitionId 展览ID
     * @return 是否收藏
     */
    Boolean isExhibitionFavorited(Long userId, Long exhibitionId);

    /**
     * 获取用户收藏统计
     *
     * @param userId 用户ID
     * @return 收藏统计信息
     */
    FavoriteStats getUserFavoriteStats(Long userId);

    /**
     * 收藏统计信息
     */
    class FavoriteStats {
        private Integer museumCount;
        private Integer exhibitionCount;
        private Integer totalCount;

        public FavoriteStats(Integer museumCount, Integer exhibitionCount) {
            this.museumCount = museumCount;
            this.exhibitionCount = exhibitionCount;
            this.totalCount = museumCount + exhibitionCount;
        }

        // Getters
        public Integer getMuseumCount() { return museumCount; }
        public Integer getExhibitionCount() { return exhibitionCount; }
        public Integer getTotalCount() { return totalCount; }
    }
}
