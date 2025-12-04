package com.lynn.museum.system.model.entity;

import com.lynn.museum.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 功能模块状态实体类
 *
 * @author lynn
 * @since 2025-01-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("feature_module")
@Schema(description = "功能模块状态")
public class FeatureModule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "模块名称", example = "企业级认证系统", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moduleName;

    @Schema(description = "模块类型", example = "completed", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"completed", "developing", "planned"})
    private String moduleType;

    @Schema(description = "模块描述", example = "RS256 JWT + JWKS动态密钥管理，42ms平均响应")
    private String description;

    @Schema(description = "完成进度（百分比）", example = "85")
    private Integer progress;

    @Schema(description = "标签文本", example = "85% 完成")
    private String tagText;

    @Schema(description = "标签颜色", example = "processing")
    private String tagColor;

    @Schema(description = "排序索引", example = "0")
    private Integer orderIndex;

    @TableLogic
    @Schema(description = "删除标志", example = "0")
    private Integer deleted;
}

