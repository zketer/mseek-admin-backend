package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lynn.museum.info.dto.MuseumCreateRequest;
import com.lynn.museum.info.dto.MuseumQueryRequest;
import com.lynn.museum.info.dto.MuseumResponse;
import com.lynn.museum.info.dto.MuseumUpdateRequest;
import com.lynn.museum.info.model.entity.MuseumInfo;

import java.util.List;

/**
 * 博物馆信息服务接口
 *
 * @author lynn
 * @since 2024-01-01
 */
public interface MuseumInfoService extends IService<MuseumInfo> {

    /**
     * 分页查询博物馆列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<MuseumResponse> getMuseumPage(MuseumQueryRequest query);

    /**
     * 获取博物馆详情
     *
     * @param id 博物馆ID
     * @return 博物馆详情
     */
    MuseumResponse getMuseumById(Long id);

    /**
     * 创建博物馆
     *
     * @param request 创建请求
     * @return 博物馆ID
     */
    Long createMuseum(MuseumCreateRequest request);

    /**
     * 更新博物馆
     *
     * @param request 更新请求
     */
    void updateMuseum(MuseumUpdateRequest request);

    /**
     * 删除博物馆
     *
     * @param id 博物馆ID
     */
    void deleteMuseum(Long id);

    /**
     * 更新博物馆状态
     *
     * @param id     博物馆ID
     * @param status 状态：0-关闭，1-开放
     */
    void updateStatus(Long id, Integer status);

    /**
     * 检查博物馆编码是否存在
     *
     * @param code      博物馆编码
     * @param excludeId 排除的博物馆ID（可选）
     * @return 是否存在
     */
    boolean existsByCode(String code, Long excludeId);

    /**
     * 根据推荐类型获取推荐博物馆
     *
     * @param type  推荐类型
     * @param limit 限制数量
     * @return 博物馆列表
     */
    List<MuseumInfo> getRecommendedMuseums(String type, Integer limit);

    /**
     * 获取附近博物馆
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @param radius    搜索半径(km)
     * @param limit     限制数量
     * @return 博物馆列表
     */
    List<MuseumInfo> getNearbyMuseums(Double latitude, Double longitude, Integer radius, Integer limit);
}
