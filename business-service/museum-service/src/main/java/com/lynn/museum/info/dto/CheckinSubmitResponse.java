package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 打卡提交响应
 *
 * @author lynn
 * @since 2024-12-30
 */
@Data
@Schema(description = "打卡提交响应")
public class CheckinSubmitResponse {

    @Schema(description = "记录ID", example = "123")
    private Long id;

    @Schema(description = "打卡时间", example = "2024-01-01T10:30:00")
    private Date checkinTime;

    @Schema(description = "操作是否成功", example = "true")
    private Boolean success;

    @Schema(description = "消息", example = "打卡成功")
    private String message;

    public static CheckinSubmitResponse success(Long id, Date checkinTime, String message) {
        CheckinSubmitResponse response = new CheckinSubmitResponse();
        response.setId(id);
        response.setCheckinTime(checkinTime);
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    public static CheckinSubmitResponse error(String message) {
        CheckinSubmitResponse response = new CheckinSubmitResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
