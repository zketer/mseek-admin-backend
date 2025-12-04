package com.lynn.museum.info.model.entity;

import com.lynn.museum.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 省份区域实体
 *
 * @author lynn
 */
@Data
@TableName("area_provinces")
@Schema(description = "省份区域实体")
public class AreaProvince {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "区域代码")
    private String adcode;

    @Schema(description = "省份名称")
    private String name;

    @Schema(description = "国家代码")
    private String countryAdcode;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;
}
