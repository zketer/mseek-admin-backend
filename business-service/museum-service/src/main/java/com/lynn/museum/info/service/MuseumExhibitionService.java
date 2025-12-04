package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lynn.museum.info.dto.ExhibitionCreateRequest;
import com.lynn.museum.info.dto.ExhibitionQueryRequest;
import com.lynn.museum.info.dto.ExhibitionResponse;
import com.lynn.museum.info.dto.ExhibitionUpdateRequest;
import com.lynn.museum.info.model.entity.MuseumExhibition;

/**
 * 博物馆展览服务接口
 *
 * @author lynn
 * @since 2024-01-01
 */
public interface MuseumExhibitionService extends IService<MuseumExhibition> {

    /**
     * 分页查询展览列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<ExhibitionResponse> getExhibitionPage(ExhibitionQueryRequest query);

    /**
     * 获取展览详情
     *
     * @param id 展览ID
     * @return 展览详情
     */
    ExhibitionResponse getExhibitionById(Long id);

    /**
     * 创建展览
     *
     * @param request 创建请求
     * @return 展览ID
     */
    Long createExhibition(ExhibitionCreateRequest request);

    /**
     * 更新展览
     *
     * @param request 更新请求
     */
    void updateExhibition(ExhibitionUpdateRequest request);

    /**
     * 删除展览
     *
     * @param id 展览ID
     */
    void deleteExhibition(Long id);

    /**
     * 更新展览状态
     *
     * @param id     展览ID
     * @param status 状态：0-已结束，1-进行中，2-未开始
     */
    void updateStatus(Long id, Integer status);

    /**
     * 分页获取最新展览列表
     * 条件：开始时间 >= 当前时间 && 当前时间 <= 结束时间
     * 排序：按开始时间从小到大排序
     *
     * @param page     当前页
     * @param pageSize 页面大小
     * @return 分页结果
     */
    IPage<ExhibitionResponse> getLatestExhibitions(Integer page, Integer pageSize);

    /**
     * 分页获取所有展览列表（支持过滤条件）
     *
     * @param page        当前页
     * @param pageSize    页面大小
     * @param museumId    博物馆ID（可选）
     * @param title       展览标题搜索（可选）
     * @param status      状态过滤（可选）
     * @param isPermanent 是否常设展览（可选）
     * @return 分页结果
     */
    IPage<ExhibitionResponse> getAllExhibitions(Integer page, Integer pageSize, Long museumId, String title, Integer status, Integer isPermanent);
}
