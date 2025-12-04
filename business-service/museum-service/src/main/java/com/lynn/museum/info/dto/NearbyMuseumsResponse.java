package com.lynn.museum.info.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 附近博物馆响应DTO
 *
 * @author lynn
 * @since 2025-09-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyMuseumsResponse {

    /**
     * 当前位置信息
     */
    private LocationInfo location;

    /**
     * 博物馆分页数据
     */
    private IPage<MuseumResponse> museums;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo {
        
        /**
         * 纬度
         */
        private Double latitude;

        /**
         * 经度
         */
        private Double longitude;

        /**
         * 城市名称
         */
        private String cityName;

        /**
         * 城市编码
         */
        private String cityCode;

        /**
         * 详细地址
         */
        private String formattedAddress;

        /**
         * 省份
         */
        private String province;

        /**
         * 区县
         */
        private String district;
    }
}
