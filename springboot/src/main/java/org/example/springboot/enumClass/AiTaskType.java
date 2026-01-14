package org.example.springboot.enumClass;

/**
 * AI分析任务类型枚举
 * @author system
 */
public enum AiTaskType {
    AUTO("AUTO", "自动触发"),
    MANUAL("MANUAL", "手动触发"),
    ADMIN("ADMIN", "管理员触发"),
    BATCH("BATCH", "批量触发");

    private final String code;
    private final String description;

    AiTaskType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AiTaskType fromCode(String code) {
        for (AiTaskType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的任务类型: " + code);
    }
}


