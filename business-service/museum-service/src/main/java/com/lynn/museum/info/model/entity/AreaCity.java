package com.lynn.museum.info.model.entity;

import com.lynn.museum.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 城市区域实体
 *
 * @author lynn
 */
@Data
@TableName("area_cities")
@Schema(description = "城市区域实体")
public class AreaCity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "区域代码")
    private String adcode;

    @Schema(description = "城市名称")
    private String name;

    @Schema(description = "所属省份代码")
    private String provinceAdcode;

    @Schema(description = "城市编码")
    private String citycode;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;
}
