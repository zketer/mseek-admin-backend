package com.lynn.museum.info.service;

import com.lynn.museum.info.dto.AmapGeocodeResponse;

/**
 * 高德地图地理编码服务接口
 *
 * @author lynn
 * @since 2025-09-26
 */
public interface AmapGeocodeService {

    /**
     * 逆地理编码 - 根据经纬度获取地址信息
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 地址信息
     */
    AmapGeocodeResponse reverseGeocode(Double longitude, Double latitude);

    /**
     * 根据经纬度获取城市名称
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 城市名称
     */
    String getCityName(Double longitude, Double latitude);

    /**
     * 根据经纬度获取详细地址
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 详细地址
     */
    String getFormattedAddress(Double longitude, Double latitude);
}
