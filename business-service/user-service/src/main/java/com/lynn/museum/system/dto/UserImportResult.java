package com.lynn.museum.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户导入结果
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户导入结果")
public class UserImportResult {

    @Schema(description = "总记录数")
    private Integer total;

    @Schema(description = "成功导入数")
    private Integer successCount;

    @Schema(description = "失败导入数")
    private Integer failureCount;

    @Schema(description = "更新记录数")
    private Integer updateCount;

    @Schema(description = "错误信息列表")
    private List<String> errorMessages;

    @Schema(description = "导入是否成功")
    private Boolean success;

    @Schema(description = "导入消息")
    private String message;

}
