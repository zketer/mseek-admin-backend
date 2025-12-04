package com.lynn.museum.system.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户Excel导入导出DTO
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户Excel导入导出DTO")
public class UserExcelDto {

    @ExcelProperty(value = "用户名", index = 0)
    @Schema(description = "用户名")
    private String username;

    @ExcelProperty(value = "昵称", index = 1)
    @Schema(description = "昵称")
    private String nickname;

    @ExcelProperty(value = "邮箱", index = 2)
    @Schema(description = "邮箱")
    private String email;

    @ExcelProperty(value = "手机号", index = 3)
    @Schema(description = "手机号")
    private String phone;

    @ExcelProperty(value = "性别", index = 4)
    @Schema(description = "性别：保密、男、女")
    private String genderName;

    @ExcelProperty(value = "生日", index = 5)
    @DateTimeFormat("yyyy-MM-dd")
    @Schema(description = "生日")
    private LocalDate birthday;

    @ExcelProperty(value = "状态", index = 6)
    @Schema(description = "状态：启用、禁用")
    private String statusName;

    @ExcelProperty(value = "备注", index = 7)
    @Schema(description = "备注")
    private String remark;

    @ExcelProperty(value = "创建时间", index = 8)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private String createTime;

    // 导入时使用的字段（不显示在Excel中）
    @Schema(description = "性别：0-保密，1-男，2-女")
    private Integer gender;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "密码（导入时使用默认密码）")
    private String password = "123456";
}
