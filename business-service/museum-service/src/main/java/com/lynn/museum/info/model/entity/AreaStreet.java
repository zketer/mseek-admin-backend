package com.lynn.museum.info.model.entity;

import com.lynn.museum.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 街道实体类
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@TableName("area_streets")
@Schema(description = "街道信息")
public class AreaStreet {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    /**
     * 街道代码
     */
    @Schema(description = "街道代码")
    private String adcode;

    /**
     * 街道名称
     */
    @Schema(description = "街道名称")
    private String name;

    /**
     * 所属区县代码
     */
    @Schema(description = "所属区县代码")
    private String districtAdcode;

    /**
     * 经度
     */
    @Schema(description = "经度")
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @Schema(description = "纬度")
    private BigDecimal latitude;
}
