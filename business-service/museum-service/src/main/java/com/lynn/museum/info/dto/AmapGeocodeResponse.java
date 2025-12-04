package com.lynn.museum.info.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 高德地图逆地理编码响应实体
 *
 * @author lynn
 * @since 2025-09-26
 */
@Data
public class AmapGeocodeResponse {

    /**
     * 状态码：1-成功，0-失败
     */
    private String status;

    /**
     * 状态信息
     */
    private String info;

    /**
     * 状态说明
     */
    private String infocode;

    /**
     * 逆地理编码信息
     */
    private Regeocode regeocode;

    @Data
    public static class Regeocode {
        
        /**
         * 地址元素信息
         */
        private AddressComponent addressComponent;

        /**
         * 格式化地址信息
         */
        private String formattedAddress;

        /**
         * 周边POI信息
         */
        private List<Poi> pois;

        /**
         * 道路信息
         */
        private List<Road> roads;

        /**
         * 道路交叉口信息
         */
        private List<Roadinter> roadinters;

        /**
         * AOI信息
         */
        private List<Aoi> aois;
    }

    @Data
    public static class AddressComponent {
        
        /**
         * 国家
         */
        private String country;

        /**
         * 省份
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 城市编码
         */
        private String citycode;

        /**
         * 区县
         */
        private String district;

        /**
         * 区县编码
         */
        private String adcode;

        /**
         * 乡镇
         */
        private String township;

        /**
         * 街道
         */
        @JsonProperty("streetNumber")
        private StreetNumber streetNumber;

        /**
         * 商圈
         */
        private String businessAreas;

        /**
         * 楼信息
         */
        private Building building;

        /**
         * 社区信息
         */
        private Neighborhood neighborhood;

        /**
         * 海域信息
         */
        private SeaArea seaArea;
    }

    @Data
    public static class StreetNumber {
        
        /**
         * 街道名称
         */
        private String street;

        /**
         * 门牌号
         */
        private String number;

        /**
         * 坐标点
         */
        private String location;

        /**
         * 方向
         */
        private String direction;

        /**
         * 距离
         */
        private String distance;
    }

    @Data
    public static class Building {
        
        /**
         * 建筑名称
         */
        private String name;

        /**
         * 建筑类型
         */
        private String type;
    }

    @Data
    public static class Neighborhood {
        
        /**
         * 社区名称
         */
        private String name;

        /**
         * 社区类型
         */
        private String type;
    }

    @Data
    public static class SeaArea {
        
        /**
         * 海域名称
         */
        private String name;

        /**
         * 海域类型
         */
        private String type;
    }

    @Data
    public static class Poi {
        
        /**
         * POI的id
         */
        private String id;

        /**
         * POI名称
         */
        private String name;

        /**
         * POI类型
         */
        private String type;

        /**
         * POI电话
         */
        private String tel;

        /**
         * 距离
         */
        private String distance;

        /**
         * 方向
         */
        private String direction;

        /**
         * 地址
         */
        private String address;

        /**
         * 坐标点
         */
        private String location;

        /**
         * 商圈名称
         */
        private String businessarea;
    }

    @Data
    public static class Road {
        
        /**
         * 道路id
         */
        private String id;

        /**
         * 道路名称
         */
        private String name;

        /**
         * 距离
         */
        private String distance;

        /**
         * 方向
         */
        private String direction;

        /**
         * 坐标点
         */
        private String location;
    }

    @Data
    public static class Roadinter {
        
        /**
         * 交叉路口到请求坐标的距离
         */
        private String distance;

        /**
         * 方向
         */
        private String direction;

        /**
         * 坐标点
         */
        private String location;

        /**
         * 第一条道路名称
         */
        @JsonProperty("first_name")
        private String firstName;

        /**
         * 第二条道路名称
         */
        @JsonProperty("second_name")
        private String secondName;
    }

    @Data
    public static class Aoi {
        
        /**
         * AOI的id
         */
        private String id;

        /**
         * AOI名称
         */
        private String name;

        /**
         * AOI所在区域编码
         */
        private String adcode;

        /**
         * AOI中心点坐标
         */
        private String location;

        /**
         * AOI点面积
         */
        private String area;

        /**
         * 输入经纬度是否在AOI面之中
         */
        private String distance;

        /**
         * AOI类型
         */
        private String type;
    }
}
