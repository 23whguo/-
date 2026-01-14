package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI分析任务实体类
 * @author system
 */
@Data
@TableName("ai_analysis_task")
@Schema(description = "AI分析任务实体类")
public class AiAnalysisTask {

    @TableId(type = IdType.AUTO)
    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "日记ID")
    @TableField("diary_id")
    private Long diaryId;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "任务状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成，FAILED-失败")
    @TableField("status")
    private String status;

    @Schema(description = "任务类型：AUTO-自动触发，MANUAL-手动触发，ADMIN-管理员触发，BATCH-批量触发")
    @TableField("task_type")
    private String taskType;

    @Schema(description = "优先级：1-低，2-正常，3-高，4-紧急")
    @TableField("priority")
    private Integer priority;

    @Schema(description = "重试次数")
    @TableField("retry_count")
    private Integer retryCount;

    @Schema(description = "最大重试次数")
    @TableField("max_retry_count")
    private Integer maxRetryCount;

    @Schema(description = "错误信息")
    @TableField("error_message")
    private String errorMessage;

    @Schema(description = "处理开始时间")
    @TableField("started_at")
    private LocalDateTime startedAt;

    @Schema(description = "处理完成时间")
    @TableField("completed_at")
    private LocalDateTime completedAt;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 判断任务是否可以重试
     */
    public boolean canRetry() {
        return retryCount < maxRetryCount && "FAILED".equals(status);
    }

    /**
     * 判断任务是否正在处理
     */
    public boolean isProcessing() {
        return "PROCESSING".equals(status);
    }

    /**
     * 判断任务是否已完成
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    /**
     * 判断任务是否失败
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }
}


