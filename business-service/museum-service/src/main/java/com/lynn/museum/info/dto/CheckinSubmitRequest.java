package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 小程序打卡提交请求
 *
 * @author lynn
 * @since 2024-12-30
 */
@Data
@Schema(description = "打卡提交请求")
public class CheckinSubmitRequest {

    @NotNull(message = "博物馆ID不能为空")
    @Schema(description = "博物馆ID", example = "1")
    private Long museumId;

    @NotNull(message = "博物馆名称不能为空")
    @Schema(description = "博物馆名称", example = "故宫博物院")
    private String museumName;

    @Schema(description = "照片URLs", example = "[\"http://example.com/photo1.jpg\"]")
    private List<String> photos;

    @Schema(description = "打卡感受", example = "今天参观故宫，感受到了中华文化的博大精深")
    private String feeling;

    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    @Schema(description = "评分(1-5)", example = "5")
    private Integer rating;

    @Schema(description = "心情状态", example = "excited")
    private String mood;

    @Schema(description = "天气状况", example = "sunny")
    private String weather;

    @Schema(description = "同行伙伴", example = "[\"小明\", \"小红\"]")
    private List<String> companions;

    @Schema(description = "标签", example = "[\"历史\", \"文化\", \"建筑\"]")
    private List<String> tags;

    @Schema(description = "位置信息")
    private LocationInfo location;

    @NotNull(message = "是否暂存标识不能为空")
    @Schema(description = "是否为暂存记录", example = "false")
    private Boolean isDraft;

    @Schema(description = "暂存ID（用于更新暂存记录）", example = "1_1640995200000")
    private String draftId;

    @Data
    @Schema(description = "位置信息")
    public static class LocationInfo {
        @NotNull(message = "经度不能为空")
        @Schema(description = "经度", example = "116.397128")
        private BigDecimal longitude;

        @NotNull(message = "纬度不能为空")
        @Schema(description = "纬度", example = "39.916527")
        private BigDecimal latitude;

        @Schema(description = "地址描述", example = "北京市东城区景山前街4号")
        private String address;
    }
}
