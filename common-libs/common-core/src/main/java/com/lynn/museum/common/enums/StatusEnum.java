package com.lynn.museum.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态枚举
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {
    
    /**
     * 启用
     */
    ENABLED(1, "启用"),
    
    /**
     * 禁用
     */
    DISABLED(0, "禁用");
    
    private final Integer code;
    private final String desc;
    
    /**
     * 根据code获取枚举
     */
    public static StatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}