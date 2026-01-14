package org.example.springboot.DTO.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI分析任务查询DTO
 * @author system
 */
@Data
@Schema(description = "AI分析任务查询DTO")
public class AiAnalysisTaskQueryDTO {

    @Schema(description = "当前页码", example = "1")
    private Integer current = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer size = 20;

    @Schema(description = "任务状态")
    private String status;

    @Schema(description = "任务类型")
    private String taskType;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "是否只显示失败任务")
    private Boolean failedOnly;

    @Schema(description = "是否只显示可重试任务")
    private Boolean retryableOnly;
}

