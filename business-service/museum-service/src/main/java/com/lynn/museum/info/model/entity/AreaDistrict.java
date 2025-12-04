package com.lynn.museum.info.model.entity;

import com.lynn.museum.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 区县区域实体
 *
 * @author lynn
 */
@Data
@TableName("area_districts")
@Schema(description = "区县区域实体")
public class AreaDistrict {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "区域代码")
    private String adcode;

    @Schema(description = "区县名称")
    private String name;

    @Schema(description = "所属城市代码")
    private String cityAdcode;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;
}
