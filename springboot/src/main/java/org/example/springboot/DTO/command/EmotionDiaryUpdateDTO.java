package org.example.springboot.DTO.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 情绪日记更新DTO
 * @author system
 */
@Data
@Schema(description = "情绪日记更新DTO")
public class EmotionDiaryUpdateDTO {

    @Schema(description = "日记ID")
    @NotNull(message = "日记ID不能为空")
    private Long id;

    @Schema(description = "情绪评分(1-10)")
    @Min(value = 1, message = "情绪评分不能小于1")
    @Max(value = 10, message = "情绪评分不能大于10")
    private Integer moodScore;

    @Schema(description = "主要情绪")
    @Size(max = 50, message = "主要情绪长度不能超过50个字符")
    private String dominantEmotion;

    @Schema(description = "情绪触发因素")
    @Size(max = 1000, message = "情绪触发因素长度不能超过1000个字符")
    private String emotionTriggers;

    @Schema(description = "日记内容")
    @Size(max = 2000, message = "日记内容长度不能超过2000个字符")
    private String diaryContent;

    @Schema(description = "睡眠质量(1-5)")
    @Min(value = 1, message = "睡眠质量评分不能小于1")
    @Max(value = 5, message = "睡眠质量评分不能大于5")
    private Integer sleepQuality;

    @Schema(description = "压力水平(1-5)")
    @Min(value = 1, message = "压力水平评分不能小于1")
    @Max(value = 5, message = "压力水平评分不能大于5")
    private Integer stressLevel;
}

