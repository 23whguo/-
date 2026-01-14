package org.example.springboot.DTO.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

/**
 * 情绪日记查询DTO
 * @author system
 */
@Data
@Schema(description = "情绪日记查询DTO")
public class EmotionDiaryQueryDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "最低情绪评分")
    @Min(value = 1, message = "最低情绪评分不能小于1")
    @Max(value = 10, message = "最低情绪评分不能大于10")
    private Integer minMoodScore;

    @Schema(description = "最高情绪评分")
    @Min(value = 1, message = "最高情绪评分不能小于1")
    @Max(value = 10, message = "最高情绪评分不能大于10")
    private Integer maxMoodScore;

    @Schema(description = "主要情绪")
    private String dominantEmotion;

    @Schema(description = "睡眠质量")
    @Min(value = 1, message = "睡眠质量评分不能小于1")
    @Max(value = 5, message = "睡眠质量评分不能大于5")
    private Integer sleepQuality;

    @Schema(description = "压力水平")
    @Min(value = 1, message = "压力水平评分不能小于1")
    @Max(value = 5, message = "压力水平评分不能大于5")
    private Integer stressLevel;

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Long current = 1L;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 100, message = "每页大小不能大于100")
    private Long size = 10L;
}
