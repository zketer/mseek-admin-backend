package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.info.dto.*;

import java.util.List;

/**
 * 城市管理服务接口
 *
 * @author lynn
 */
public interface AreaCityService {

    /**
     * 分页查询城市列表
     *
     * @param request 查询参数
     * @return 分页结果
     */
    IPage<CityResponse> getCityList(CityQueryRequest request);

    /**
     * 根据省份获取城市列表（不分页）
     *
     * @param provinceAdcode 省份代码
     * @return 城市列表
     */
    List<CityResponse> getCitiesByProvince(String provinceAdcode);

    /**
     * 根据ID获取城市详情
     *
     * @param id 城市ID
     * @return 城市详情
     */
    CityResponse getCityById(Integer id);

    /**
     * 根据区域代码获取城市详情
     *
     * @param adcode 区域代码
     * @return 城市详情
     */
    CityResponse getCityByAdcode(String adcode);

    /**
     * 创建城市
     *
     * @param request 创建请求
     * @return 创建结果
     */
    CityResponse createCity(CityCreateRequest request);

    /**
     * 更新城市
     *
     * @param id 城市ID
     * @param request 更新请求
     * @return 更新结果
     */
    CityResponse updateCity(Integer id, CityUpdateRequest request);

    /**
     * 删除城市
     *
     * @param id 城市ID
     */
    void deleteCity(Integer id);

    /**
     * 批量删除城市
     *
     * @param ids 城市ID列表
     */
    void deleteCities(List<Integer> ids);
}
