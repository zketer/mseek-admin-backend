package com.lynn.museum.info.controller;

import com.lynn.museum.common.result.Result;
import com.lynn.museum.common.result.ResultUtils;
import com.lynn.museum.common.result.ResultCode;
import com.lynn.museum.info.entity.FileBusinessRelation;
import com.lynn.museum.info.enums.BusinessTypeEnum;
import com.lynn.museum.info.enums.RelationTypeEnum;
import com.lynn.museum.info.service.FileBusinessRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文件业务关联控制器
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/file-relations")
@RequiredArgsConstructor
@Validated
@Tag(name = "文件业务关联", description = "文件与业务实体关联管理接口")
public class FileBusinessRelationController {

    private final FileBusinessRelationService fileBusinessRelationService;

    @Operation(summary = "根据业务实体查询文件关联")
    @GetMapping("/business")
    public Result<List<FileBusinessRelation>> getByBusiness(
            @Parameter(description = "业务实体ID") @RequestParam Long businessId,
            @Parameter(description = "业务类型") @RequestParam String businessType,
            @Parameter(description = "关系类型", required = false) @RequestParam(required = false) String relationType) {
        
        log.info("查询业务实体文件关联：businessId={}, businessType={}, relationType={}", businessId, businessType, relationType);
        
        BusinessTypeEnum businessTypeEnum = BusinessTypeEnum.fromCode(businessType);
        if (businessTypeEnum == null) {
            return ResultUtils.error(ResultCode.PARAM_INVALID);
        }
        
        List<FileBusinessRelation> relations;
        if (relationType != null) {
            RelationTypeEnum relationTypeEnum = RelationTypeEnum.fromCode(relationType);
            if (relationTypeEnum == null) {
                return ResultUtils.error(ResultCode.PARAM_INVALID, "无效的关系类型：" + relationType);
            }
            relations = fileBusinessRelationService.getByBusinessAndRelation(businessId, businessTypeEnum, relationTypeEnum);
        } else {
            relations = fileBusinessRelationService.getByBusiness(businessId, businessTypeEnum);
        }
        
        return ResultUtils.success(relations);
    }

    @Operation(summary = "根据文件ID查询业务关联")
    @GetMapping("/file/{fileId}")
    public Result<List<FileBusinessRelation>> getByFileId(
            @Parameter(description = "文件ID") @PathVariable Long fileId) {
        
        log.info("查询文件业务关联：fileId={}", fileId);
        
        List<FileBusinessRelation> relations = fileBusinessRelationService.getByFileId(fileId);
        return ResultUtils.success(relations);
    }

    @Operation(summary = "获取业务实体的主图文件ID")
    @GetMapping("/business/{businessId}/main-image")
    public Result<Long> getMainImageFileId(
            @Parameter(description = "业务实体ID") @PathVariable Long businessId,
            @Parameter(description = "业务类型") @RequestParam String businessType) {
        
        log.info("获取业务实体主图：businessId={}, businessType={}", businessId, businessType);
        
        BusinessTypeEnum businessTypeEnum = BusinessTypeEnum.fromCode(businessType);
        if (businessTypeEnum == null) {
            return ResultUtils.error(ResultCode.PARAM_INVALID, "无效的业务类型：" + businessType);
        }
        
        Long fileId = fileBusinessRelationService.getMainImageFileId(businessId, businessTypeEnum);
        return ResultUtils.success(fileId);
    }

    @Operation(summary = "获取业务实体的文件ID列表")
    @GetMapping("/business/{businessId}/files")
    public Result<List<Long>> getBusinessFileIds(
            @Parameter(description = "业务实体ID") @PathVariable Long businessId,
            @Parameter(description = "业务类型") @RequestParam String businessType,
            @Parameter(description = "关系类型", required = false) @RequestParam(required = false) String relationType) {
        
        log.info("获取业务实体文件ID列表：businessId={}, businessType={}, relationType={}", businessId, businessType, relationType);
        
        BusinessTypeEnum businessTypeEnum = BusinessTypeEnum.fromCode(businessType);
        if (businessTypeEnum == null) {
            return ResultUtils.error(ResultCode.PARAM_INVALID, "无效的业务类型：" + businessType);
        }
        
        RelationTypeEnum relationTypeEnum = null;
        if (relationType != null) {
            relationTypeEnum = RelationTypeEnum.fromCode(relationType);
            if (relationTypeEnum == null) {
                return ResultUtils.error(ResultCode.PARAM_INVALID, "无效的关系类型：" + relationType);
            }
        }
        
        List<Long> fileIds = fileBusinessRelationService.getBusinessFileIds(businessId, businessTypeEnum, relationTypeEnum);
        return ResultUtils.success(fileIds);
    }

    @Operation(summary = "创建文件业务关联")
    @PostMapping
    public Result<FileBusinessRelation> createRelation(@RequestBody @Validated CreateRelationRequest request) {
        
        log.info("创建文件业务关联：{}", request);
        
        BusinessTypeEnum businessTypeEnum = BusinessTypeEnum.fromCode(request.getBusinessType());
        if (businessTypeEnum == null) {
            return ResultUtils.badRequest("无效的业务类型：" + request.getBusinessType());
        }
        
        RelationTypeEnum relationTypeEnum = RelationTypeEnum.fromCode(request.getRelationType());
        if (relationTypeEnum == null) {
            return ResultUtils.badRequest("无效的关系类型：" + request.getRelationType());
        }
        
        FileBusinessRelation relation = fileBusinessRelationService.createRelation(
                request.getFileId(), 
                request.getBusinessId(), 
                businessTypeEnum, 
                relationTypeEnum, 
                request.getSortOrder(), 
                request.getRemark(), 
                request.getCreateBy()
        );
        
        return ResultUtils.success(relation);
    }

    @Operation(summary = "批量创建文件业务关联")
    @PostMapping("/batch")
    public Result<List<FileBusinessRelation>> batchCreateRelation(@RequestBody @Validated BatchCreateRelationRequest request) {
        
        log.info("批量创建文件业务关联：{}", request);
        
        BusinessTypeEnum businessTypeEnum = BusinessTypeEnum.fromCode(request.getBusinessType());
        if (businessTypeEnum == null) {
            return ResultUtils.badRequest("无效的业务类型：" + request.getBusinessType());
        }
        
        RelationTypeEnum relationTypeEnum = RelationTypeEnum.fromCode(request.getRelationType());
        if (relationTypeEnum == null) {
            return ResultUtils.badRequest("无效的关系类型：" + request.getRelationType());
        }
        
        List<FileBusinessRelation> relations = fileBusinessRelationService.batchCreateRelation(
                request.getFileIds(), 
                request.getBusinessId(), 
                businessTypeEnum, 
                relationTypeEnum, 
                request.getCreateBy()
        );
        
        return ResultUtils.success(relations);
    }

    @Operation(summary = "设置业务实体的主图")
    @PutMapping("/business/{businessId}/main-image")
    public Result<FileBusinessRelation> setMainImage(
            @Parameter(description = "业务实体ID") @PathVariable Long businessId,
            @Parameter(description = "业务类型") @RequestParam String businessType,
            @Parameter(description = "文件ID") @RequestParam Long fileId,
            @Parameter(description = "更新者ID", required = false) @RequestParam(required = false) Long updateBy) {
        
        log.info("设置业务实体主图：businessId={}, businessType={}, fileId={}", businessId, businessType, fileId);
        
        BusinessTypeEnum businessTypeEnum = BusinessTypeEnum.fromCode(businessType);
        if (businessTypeEnum == null) {
            return ResultUtils.error(ResultCode.PARAM_INVALID, "无效的业务类型：" + businessType);
        }
        
        FileBusinessRelation relation = fileBusinessRelationService.setMainImage(fileId, businessId, businessTypeEnum, updateBy);
        return ResultUtils.success(relation);
    }

    @Operation(summary = "更新文件关联排序")
    @PutMapping("/{id}/sort")
    public Result<Void> updateSortOrder(
            @Parameter(description = "关联ID") @PathVariable Long id,
            @Parameter(description = "排序值") @RequestParam Integer sortOrder) {
        
        log.info("更新文件关联排序：id={}, sortOrder={}", id, sortOrder);
        
        boolean success = fileBusinessRelationService.updateSortOrder(id, sortOrder);
        if (success) {
            return ResultUtils.success();
        } else {
            return ResultUtils.error(ResultCode.OPERATION_FAILED, "更新排序失败");
        }
    }

    @Operation(summary = "删除业务实体的所有文件关联")
    @DeleteMapping("/business")
    public Result<Void> deleteByBusiness(
            @Parameter(description = "业务实体ID") @RequestParam Long businessId,
            @Parameter(description = "业务类型") @RequestParam String businessType,
            @Parameter(description = "关系类型", required = false) @RequestParam(required = false) String relationType) {
        
        log.info("删除业务实体文件关联：businessId={}, businessType={}, relationType={}", businessId, businessType, relationType);
        
        BusinessTypeEnum businessTypeEnum = BusinessTypeEnum.fromCode(businessType);
        if (businessTypeEnum == null) {
            return ResultUtils.error(ResultCode.PARAM_INVALID, "无效的业务类型：" + businessType);
        }
        
        boolean success;
        if (relationType != null) {
            RelationTypeEnum relationTypeEnum = RelationTypeEnum.fromCode(relationType);
            if (relationTypeEnum == null) {
                return ResultUtils.error(ResultCode.PARAM_INVALID, "无效的关系类型：" + relationType);
            }
            success = fileBusinessRelationService.deleteByBusinessAndRelation(businessId, businessTypeEnum, relationTypeEnum);
        } else {
            success = fileBusinessRelationService.deleteByBusiness(businessId, businessTypeEnum);
        }
        
        if (success) {
            return ResultUtils.success();
        } else {
            return ResultUtils.error(ResultCode.OPERATION_FAILED, "删除失败");
        }
    }

    @Operation(summary = "删除文件的所有业务关联")
    @DeleteMapping("/file/{fileId}")
    public Result<Void> deleteByFileId(
            @Parameter(description = "文件ID") @PathVariable Long fileId) {
        
        log.info("删除文件的所有业务关联：fileId={}", fileId);
        
        boolean success = fileBusinessRelationService.deleteByFileId(fileId);
        if (success) {
            return ResultUtils.success();
        } else {
            return ResultUtils.error(ResultCode.OPERATION_FAILED);
        }
    }

    @Operation(summary = "检查文件是否被使用")
    @GetMapping("/file/{fileId}/in-use")
    public Result<Boolean> isFileInUse(
            @Parameter(description = "文件ID") @PathVariable Long fileId,
            @Parameter(description = "业务实体ID", required = false) @RequestParam(required = false) Long businessId,
            @Parameter(description = "业务类型", required = false) @RequestParam(required = false) String businessType) {
        
        log.info("检查文件是否被使用：fileId={}, businessId={}, businessType={}", fileId, businessId, businessType);
        
        BusinessTypeEnum businessTypeEnum = null;
        if (businessType != null) {
            businessTypeEnum = BusinessTypeEnum.fromCode(businessType);
            if (businessTypeEnum == null) {
                return ResultUtils.error(ResultCode.PARAM_INVALID);
            }
        }
        
        boolean inUse = fileBusinessRelationService.isFileInUse(fileId, businessId, businessTypeEnum);
        return ResultUtils.success(inUse);
    }

    @Operation(summary = "获取枚举值列表")
    @GetMapping("/enums")
    public Result<Map<String, Object>> getEnums() {
        return ResultUtils.success(Map.of(
                "businessTypes", BusinessTypeEnum.values(),
                "relationTypes", RelationTypeEnum.values()
        ));
    }

    /**
     * 创建关联请求对象
     */
    public static class CreateRelationRequest {
        private Long fileId;
        private Long businessId;
        private String businessType;
        private String relationType;
        private Integer sortOrder;
        private String remark;
        private Long createBy;

        // Getters and Setters
        public Long getFileId() { return fileId; }
        public void setFileId(Long fileId) { this.fileId = fileId; }
        public Long getBusinessId() { return businessId; }
        public void setBusinessId(Long businessId) { this.businessId = businessId; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public String getRelationType() { return relationType; }
        public void setRelationType(String relationType) { this.relationType = relationType; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public Long getCreateBy() { return createBy; }
        public void setCreateBy(Long createBy) { this.createBy = createBy; }

        @Override
        public String toString() {
            return "CreateRelationRequest{" +
                    "fileId=" + fileId +
                    ", businessId=" + businessId +
                    ", businessType='" + businessType + '\'' +
                    ", relationType='" + relationType + '\'' +
                    ", sortOrder=" + sortOrder +
                    ", remark='" + remark + '\'' +
                    ", createBy=" + createBy +
                    '}';
        }
    }

    /**
     * 批量创建关联请求对象
     */
    public static class BatchCreateRelationRequest {
        private List<Long> fileIds;
        private Long businessId;
        private String businessType;
        private String relationType;
        private Long createBy;

        // Getters and Setters
        public List<Long> getFileIds() { return fileIds; }
        public void setFileIds(List<Long> fileIds) { this.fileIds = fileIds; }
        public Long getBusinessId() { return businessId; }
        public void setBusinessId(Long businessId) { this.businessId = businessId; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public String getRelationType() { return relationType; }
        public void setRelationType(String relationType) { this.relationType = relationType; }
        public Long getCreateBy() { return createBy; }
        public void setCreateBy(Long createBy) { this.createBy = createBy; }

        @Override
        public String toString() {
            return "BatchCreateRelationRequest{" +
                    "fileIds=" + fileIds +
                    ", businessId=" + businessId +
                    ", businessType='" + businessType + '\'' +
                    ", relationType='" + relationType + '\'' +
                    ", createBy=" + createBy +
                    '}';
        }
    }
}
