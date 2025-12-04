package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lynn.museum.info.dto.BannerCreateRequest;
import com.lynn.museum.info.dto.BannerQueryRequest;
import com.lynn.museum.info.dto.BannerResponse;
import com.lynn.museum.info.model.entity.Banner;

import java.util.List;

/**
 * 轮播图服务接口
 *
 * @author lynn
 * @since 2024-12-16
 */
public interface BannerService extends IService<Banner> {

    /**
     * 分页查询轮播图
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<BannerResponse> getBanners(BannerQueryRequest query);

    /**
     * 创建轮播图
     *
     * @param request 创建请求
     * @return 轮播图ID
     */
    Long createBanner(BannerCreateRequest request);

    /**
     * 更新轮播图
     *
     * @param id 轮播图ID
     * @param request 更新请求
     */
    void updateBanner(Long id, BannerCreateRequest request);

    /**
     * 删除轮播图
     *
     * @param id 轮播图ID
     */
    void deleteBanner(Long id);

    /**
     * 更新轮播图状态
     *
     * @param id 轮播图ID
     * @param status 状态
     */
    void updateBannerStatus(Long id, Integer status);

    /**
     * 获取生效的轮播图
     *
     * @param limit 限制数量
     * @return 轮播图列表
     */
    List<BannerResponse> getActiveBanners(Integer limit);

    /**
     * 记录轮播图点击
     *
     * @param id 轮播图ID
     * @return 是否成功
     */
    boolean incrementClickCount(Long id);
}
