package com.lynn.museum.info.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.lynn.museum.info.config.AmapConfig;
import com.lynn.museum.info.dto.AmapGeocodeResponse;
import com.lynn.museum.info.service.AmapGeocodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 高德地图地理编码服务实现
 *
 * @author lynn
 * @since 2025-09-26
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AmapGeocodeServiceImpl implements AmapGeocodeService {

    private final AmapConfig amapConfig;

    @Override
    public AmapGeocodeResponse reverseGeocode(Double longitude, Double latitude) {
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("location", longitude + "," + latitude);
            params.put("key", amapConfig.getKey());
            params.put("radius", amapConfig.getRadius());
            // 返回扩展信息
            params.put("extensions", "base");
            params.put("output", "json");

            // 使用hutool发送HTTP GET请求
            String responseBody = HttpUtil.get(amapConfig.getGeocodeUrl(), params, amapConfig.getTimeout());
            

            if (StrUtil.isNotBlank(responseBody)) {
                // 使用hutool解析JSON响应
                AmapGeocodeResponse result = JSONUtil.toBean(responseBody, AmapGeocodeResponse.class);
                
                if (result != null && "1".equals(result.getStatus())) {
                    return result;
                } else {
                    log.warn("高德地图逆地理编码调用失败 - 位置：{},{}, 状态：{}, 信息：{}", 
                        latitude, longitude, 
                        result != null ? result.getStatus() : "null",
                        result != null ? result.getInfo() : "null"
                    );
                    return null;
                }
            } else {
                log.warn("高德地图API响应为空 - 位置：{},{}", latitude, longitude);
                return null;
            }

        } catch (Exception e) {
            log.error("高德地图逆地理编码调用异常 - 位置：{},{}", latitude, longitude, e);
            return null;
        }
    }

    @Override
    public String getCityName(Double longitude, Double latitude) {
        AmapGeocodeResponse response = reverseGeocode(longitude, latitude);
        if (response != null && response.getRegeocode() != null) {
            AmapGeocodeResponse.Regeocode regeocode = response.getRegeocode();
            if (regeocode.getAddressComponent() != null) {
                String city = regeocode.getAddressComponent().getCity();
                if (StrUtil.isNotBlank(city)) {
                    // 如果city为空或者是"市辖区"等特殊值，尝试使用province
                    if ("[]".equals(city) || "市辖区".equals(city)) {
                        String province = regeocode.getAddressComponent().getProvince();
                        if (StrUtil.isNotBlank(province)) {
                            return province;
                        }
                    }
                    return city;
                }
                
                // 如果城市为空，返回省份
                String province = regeocode.getAddressComponent().getProvince();
                if (StrUtil.isNotBlank(province)) {
                    return province;
                }
            }
        }
        
        // 如果高德API调用失败，回退到本地识别逻辑
        log.warn("高德地图API获取城市失败，回退到本地识别 - 位置：{},{}", latitude, longitude);
        return getLocalCityName(latitude, longitude);
    }

    @Override
    public String getFormattedAddress(Double longitude, Double latitude) {
        AmapGeocodeResponse response = reverseGeocode(longitude, latitude);
        if (response != null && response.getRegeocode() != null) {
            AmapGeocodeResponse.Regeocode regeocode = response.getRegeocode();
            String formattedAddress = regeocode.getFormattedAddress();
            if (StrUtil.isNotBlank(formattedAddress)) {
                return formattedAddress;
            }
        }
        
        // 如果获取不到详细地址，至少返回城市名称
        String cityName = getCityName(longitude, latitude);
        return StrUtil.isNotBlank(cityName) ? cityName : "未知地址";
    }

    /**
     * 本地城市识别逻辑（作为备用方案）
     */
    private String getLocalCityName(Double latitude, Double longitude) {
        // 优化的城市判断逻辑，返回城市名称而不是代码
        if (latitude >= 39.0 && latitude <= 41.5 && longitude >= 115.5 && longitude <= 117.8) {
            return "北京市";
        } else if (latitude >= 30.5 && latitude <= 32.0 && longitude >= 120.8 && longitude <= 122.2) {
            return "上海市";
        } else if (latitude >= 22.4 && latitude <= 23.9 && longitude >= 113.0 && longitude <= 114.8) {
            return "广州市";
        } else if (latitude >= 22.0 && latitude <= 22.8 && longitude >= 113.7 && longitude <= 114.8) {
            return "深圳市";
        } else if (latitude >= 29.0 && latitude <= 30.8 && longitude >= 119.8 && longitude <= 120.5) {
            return "杭州市";
        } else if (latitude >= 30.0 && latitude <= 32.5 && longitude >= 118.0 && longitude <= 119.5) {
            return "南京市";
        } else if (latitude >= 30.0 && latitude <= 31.8 && longitude >= 103.8 && longitude <= 104.8) {
            return "成都市";
        } else if (latitude >= 29.0 && latitude <= 30.2 && longitude >= 106.3 && longitude <= 107.0) {
            return "重庆市";
        } else if (latitude >= 26.0 && latitude <= 26.8 && longitude >= 119.0 && longitude <= 119.8) {
            return "福州市";
        } else if (latitude >= 24.0 && latitude <= 25.0 && longitude >= 118.0 && longitude <= 118.3) {
            return "厦门市";
        } else if (latitude >= 35.5 && latitude <= 37.0 && longitude >= 119.8 && longitude <= 121.0) {
            return "青岛市";
        } else if (latitude >= 36.0 && latitude <= 37.0 && longitude >= 116.8 && longitude <= 117.5) {
            return "济南市";
        } else if (latitude >= 37.5 && latitude <= 38.8 && longitude >= 117.0 && longitude <= 118.2) {
            return "天津市";
        } else if (latitude >= 38.0 && latitude <= 39.0 && longitude >= 114.0 && longitude <= 115.5) {
            return "石家庄市";
        } else if (latitude >= 34.5 && latitude <= 35.0 && longitude >= 108.5 && longitude <= 109.5) {
            return "西安市";
        } else {
            return "未知城市";
        }
    }
}
