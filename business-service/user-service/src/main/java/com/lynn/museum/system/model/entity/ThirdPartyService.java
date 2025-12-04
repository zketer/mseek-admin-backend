package com.lynn.museum.system.model.entity;

import com.lynn.museum.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 第三方服务接入实体类
 *
 * @author lynn
 * @since 2025-01-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("third_party_service")
@Schema(description = "第三方服务接入")
public class ThirdPartyService extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "服务名称", example = "高德地图API", requiredMode = Schema.RequiredMode.REQUIRED)
    private String serviceName;

    @Schema(description = "服务类型", example = "api")
    private String serviceType;

    @Schema(description = "状态", example = "connected", allowableValues = {"connected", "planned", "deprecated"})
    private String status;

    @Schema(description = "状态标签颜色", example = "green")
    private String statusTagColor;

    @Schema(description = "服务描述", example = "行政区划数据、博物馆地理信息")
    private String description;

    @Schema(description = "排序索引", example = "0")
    private Integer orderIndex;

    @TableLogic
    @Schema(description = "删除标志", example = "0")
    private Integer deleted;
}

