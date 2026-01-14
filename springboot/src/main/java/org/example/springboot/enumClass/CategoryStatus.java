package org.example.springboot.enumClass;

import lombok.Getter;

/**
 * 知识分类状态枚举
 * @author system
 */
@Getter
public enum CategoryStatus {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer code;
    private final String description;

    CategoryStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static CategoryStatus fromCode(Integer code) {
        for (CategoryStatus status : CategoryStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的分类状态代码: " + code);
    }

    /**
     * 验证分类状态代码是否有效
     */
    public static boolean isValidCode(Integer code) {
        for (CategoryStatus status : CategoryStatus.values()) {
            if (status.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return this == ENABLED;
    }

    /**
     * 是否禁用
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }
}