package org.example.springboot.enumClass;

/**
 * AI分析任务状态枚举
 * @author system
 */
public enum AiTaskStatus {
    PENDING("PENDING", "待处理"),
    PROCESSING("PROCESSING", "处理中"),
    COMPLETED("COMPLETED", "已完成"),
    FAILED("FAILED", "失败");

    private final String code;
    private final String description;

    AiTaskStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AiTaskStatus fromCode(String code) {
        for (AiTaskStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的任务状态: " + code);
    }
}


