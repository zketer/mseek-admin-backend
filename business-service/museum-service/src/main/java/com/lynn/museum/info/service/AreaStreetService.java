package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.info.dto.StreetCreateRequest;
import com.lynn.museum.info.dto.StreetResponse;
import com.lynn.museum.info.dto.StreetUpdateRequest;

import java.util.List;

/**
 * 街道服务接口
 *
 * @author lynn
 */
public interface AreaStreetService {

    /**
     * 分页查询所有街道
     *
     * @param current 当前页
     * @param pageSize 每页大小
     * @param keyword 关键词搜索
     * @param adcode 区域代码搜索
     * @return 分页结果
     */
    IPage<StreetResponse> getStreetsPage(Integer current, Integer pageSize, String keyword, String adcode);

    /**
     * 根据区县代码获取街道列表
     *
     * @param districtCode 区县代码
     * @return 街道列表
     */
    List<StreetResponse> getStreetsByDistrict(String districtCode);

    /**
     * 根据ID获取街道详情
     *
     * @param id 街道ID
     * @return 街道详情
     */
    StreetResponse getStreetById(Integer id);

    /**
     * 根据区域代码获取街道详情
     *
     * @param adcode 区域代码
     * @return 街道详情
     */
    StreetResponse getStreetByAdcode(String adcode);

    /**
     * 创建街道
     *
     * @param request 创建请求
     * @return 街道详情
     */
    StreetResponse createStreet(StreetCreateRequest request);

    /**
     * 更新街道
     *
     * @param id 街道ID
     * @param request 更新请求
     * @return 街道详情
     */
    StreetResponse updateStreet(Integer id, StreetUpdateRequest request);

    /**
     * 删除街道
     *
     * @param id 街道ID
     */
    void deleteStreet(Integer id);

    /**
     * 批量删除街道
     *
     * @param ids 街道ID列表
     */
    void deleteStreets(List<Integer> ids);
}
