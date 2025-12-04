package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 打卡统计响应
 *
 * @author lynn
 * @since 2024-12-30
 */
@Data
@Schema(description = "打卡统计响应")
public class CheckinStatsResponse {

    @Schema(description = "总打卡次数", example = "25")
    private Integer totalCheckins;

    @Schema(description = "本月打卡次数", example = "3")
    private Integer thisMonthCheckins;

    @Schema(description = "已访问博物馆数量", example = "15")
    private Integer visitedMuseums;

    @Schema(description = "总照片数量", example = "48")
    private Integer totalPhotos;
}
