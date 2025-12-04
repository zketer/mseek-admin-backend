package com.lynn.museum.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础实体类
 * 仅包含审计字段，id和deleted由各实体类自行定义
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "基础实体")
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "创建时间", example = "2024-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "更新时间", example = "2024-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "更新人ID")
    private Long updateBy;

}