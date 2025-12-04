package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.info.dto.AreaDivisionResponse;
import com.lynn.museum.info.dto.DistrictCreateRequest;
import com.lynn.museum.info.dto.DistrictResponse;
import com.lynn.museum.info.dto.DistrictUpdateRequest;
import com.lynn.museum.info.dto.StreetResponse;

import java.util.List;

/**
 * 区域行政区划服务接口
 *
 * @author lynn
 */
public interface AreaDivisionService {

    /**
     * 获取所有省份列表
     *
     * @return 省份列表
     */
    List<AreaDivisionResponse> getAllProvinces();

    /**
     * 根据省份代码获取城市列表
     *
     * @param provinceCode 省份代码
     * @return 城市列表
     */
    List<AreaDivisionResponse> getCitiesByProvince(String provinceCode);

    /**
     * 根据城市代码获取区县列表
     *
     * @param cityCode 城市代码
     * @return 区县列表
     */
    List<AreaDivisionResponse> getDistrictsByCity(String cityCode);

    /**
     * 获取完整的区域行政区划树形结构
     *
     * @return 区域行政区划树
     */
    List<AreaDivisionResponse> getAreaDivisionTree();

    /**
     * 根据代码获取区域行政区划信息
     *
     * @param adcode 区域代码
     * @return 区域行政区划信息
     */
    AreaDivisionResponse getByAdcode(String adcode);

    /**
     * 根据区县代码获取街道列表
     *
     * @param districtCode 区县代码
     * @return 街道列表
     */
    List<StreetResponse> getStreetsByDistrict(String districtCode);

    /**
     * 分页查询所有区县
     *
     * @param current 当前页
     * @param pageSize 每页大小
     * @param keyword 关键词搜索
     * @param adcode 区域代码搜索
     * @return 分页结果
     */
    IPage<AreaDivisionResponse> getDistrictsPage(Integer current, Integer pageSize, String keyword, String adcode);

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
     * 根据ID获取区县详情
     *
     * @param id 区县ID
     * @return 区县详情
     */
    DistrictResponse getDistrictById(Integer id);

    /**
     * 根据区域代码获取区县详情
     *
     * @param adcode 区域代码
     * @return 区县详情
     */
    DistrictResponse getDistrictByAdcode(String adcode);

    /**
     * 创建区县
     *
     * @param request 创建请求
     * @return 区县详情
     */
    DistrictResponse createDistrict(DistrictCreateRequest request);

    /**
     * 更新区县
     *
     * @param id 区县ID
     * @param request 更新请求
     * @return 区县详情
     */
    DistrictResponse updateDistrict(Integer id, DistrictUpdateRequest request);

    /**
     * 删除区县
     *
     * @param id 区县ID
     */
    void deleteDistrict(Integer id);

    /**
     * 批量删除区县
     *
     * @param ids 区县ID列表
     */
    void deleteDistricts(List<Integer> ids);
}
