package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.info.dto.CategoryResponse;
import com.lynn.museum.info.dto.MuseumResponse;
import com.lynn.museum.info.dto.NearbyMuseumsResponse;
import com.lynn.museum.info.model.entity.MuseumInfo;

import java.util.List;

/**
 * 小程序博物馆服务接口
 *
 * @author lynn
 * @since 2024-01-01
 */
public interface MiniappMuseumService {

    /**
     * 小程序分页查询博物馆列表
     *
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param cityCode 城市代码
     * @param keyword    搜索关键词
     * @param categoryId 分类ID
     * @param sortBy     排序方式
     * @return 分页结果
     */
    IPage<MuseumResponse> getMuseumPage(Integer page, Integer pageSize, String cityCode, String keyword, Integer categoryId, String sortBy);

    /**
     * 根据城市获取博物馆列表
     *
     * @param cityCode 城市代码
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键词
     * @param sortBy   排序方式
     * @return 分页结果
     */
    IPage<MuseumResponse> getMuseumsByCity(String cityCode, Integer page, Integer pageSize, String keyword, String sortBy);

    /**
     * 搜索博物馆
     *
     * @param keyword   搜索关键词
     * @param page      当前页码
     * @param pageSize  每页大小
     * @param cityCode  城市代码（可选）
     * @param sortBy    排序方式
     * @return 分页结果
     */
    IPage<MuseumResponse> searchMuseums(String keyword, Integer page, Integer pageSize, String cityCode, String sortBy);

    /**
     * 获取博物馆详情
     *
     * @param id 博物馆ID
     * @return 博物馆详情
     */
    MuseumResponse getMuseumDetail(Long id);

    /**
     * 根据位置获取附近博物馆
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @param radius    搜索半径(km)
     * @param limit     限制数量
     * @return 博物馆列表
     */
    List<MuseumInfo> getNearbyMuseums(Double latitude, Double longitude, Integer radius, Integer limit);


    /**
     * 分页获取热门博物馆列表
     * 根据用户打卡次数统计最热门的博物馆
     *
     * @param page     当前页
     * @param pageSize 页面大小
     * @param name     博物馆名称（可选）
     * @return 分页结果
     */
    IPage<MuseumResponse> getHotMuseums(Integer page, Integer pageSize, String name);

    /**
     * 获取博物馆分类列表
     * 
     * @return 分类列表
     */
    List<CategoryResponse> getCategories();

    /**
     * 分页获取附近博物馆
     */
    IPage<MuseumResponse> getNearbyMuseumsPage(Double latitude, Double longitude, Integer radius, Integer page, Integer pageSize);

    /**
     * 获取附近博物馆（包含位置信息）
     * 
     * @param latitude 纬度
     * @param longitude 经度
     * @param radius 搜索半径（公里）
     * @param page 页码
     * @param pageSize 页面大小
     * @param name 博物馆名称搜索
     * @param cityCode 可选的城市编码
     * @param cityName 可选的城市名称
     * @return 包含位置信息和博物馆列表的响应
     */
    NearbyMuseumsResponse getNearbyMuseumsWithLocation(Double latitude, Double longitude, Integer radius, Integer page, Integer pageSize, String name, String cityCode, String cityName);
}
