package org.example.springboot.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI分析任务响应DTO
 * @author system
 */
@Data
@Schema(description = "AI分析任务响应DTO")
public class AiAnalysisTaskResponseDTO {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "日记ID")
    private Long diaryId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "日记日期")
    private String diaryDate;

    @Schema(description = "任务状态")
    private String status;

    @Schema(description = "任务状态描述")
    private String statusDescription;

    @Schema(description = "任务类型")
    private String taskType;

    @Schema(description = "任务类型描述")
    private String taskTypeDescription;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "优先级描述")
    private String priorityDescription;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "最大重试次数")
    private Integer maxRetryCount;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "处理开始时间")
    private LocalDateTime startedAt;

    @Schema(description = "处理完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "处理耗时（毫秒）")
    private Long processingTimeMs;

    @Schema(description = "是否可以重试")
    private Boolean canRetry;
}


