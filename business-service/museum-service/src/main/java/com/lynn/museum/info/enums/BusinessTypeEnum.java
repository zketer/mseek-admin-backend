package com.lynn.museum.info.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务类型枚举
 * 用于标识文件关联的业务实体类型
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum BusinessTypeEnum {
    
    /**
     * 博物馆
     */
    MUSEUM("MUSEUM", "博物馆"),
    
    /**
     * 展览
     */
    EXHIBITION("EXHIBITION", "展览"),
    
    /**
     * 横幅广告
     */
    BANNER("BANNER", "横幅"),
    
    /**
     * 公告通知
     */
    ANNOUNCEMENT("ANNOUNCEMENT", "公告"),
    
    /**
     * 用户打卡
     */
    CHECKIN("CHECKIN", "打卡"),
    
    /**
     * 用户头像
     */
    USER_AVATAR("USER_AVATAR", "用户头像"),
    
    /**
     * 应用版本
     */
    APP_VERSION("APP_VERSION", "应用版本");

    /**
     * 枚举代码（存储到数据库）
     */
    @EnumValue
    @JsonValue
    private final String code;
    
    /**
     * 枚举描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     * 
     * @param code 枚举代码
     * @return 对应的枚举，未找到返回null
     */
    public static BusinessTypeEnum fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        for (BusinessTypeEnum type : BusinessTypeEnum.values()) {
            if (type.getCode().equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 检查代码是否有效
     * 
     * @param code 枚举代码
     * @return 是否有效
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }
}
