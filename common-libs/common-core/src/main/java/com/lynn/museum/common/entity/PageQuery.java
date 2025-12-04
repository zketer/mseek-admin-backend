package com.lynn.museum.common.entity;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分页查询基类
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码，从1开始
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小值为1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小最小值为1")
    @Max(value = 100, message = "每页大小最大值为100")
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方向：asc-升序，desc-降序
     */
    private String orderDirection = "desc";

    /**
     * 计算偏移量
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }

}