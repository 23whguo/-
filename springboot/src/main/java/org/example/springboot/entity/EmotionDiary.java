package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 情绪日记实体类
 * @author system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("emotion_diary")
@Schema(description = "情绪日记实体类")
public class EmotionDiary {

    @TableId(type = IdType.AUTO)
    @Schema(description = "日记ID")
    private Long id;

    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "日记日期")
    @NotNull(message = "日记日期不能为空")

    @TableField("diary_date")
    private LocalDate diaryDate;

    @Schema(description = "情绪评分(1-10)")
    @NotNull(message = "情绪评分不能为空")
    @Min(value = 1, message = "情绪评分不能小于1")
    @Max(value = 10, message = "情绪评分不能大于10")
    @TableField("mood_score")
    private Integer moodScore;

    @Schema(description = "主要情绪")
    @Size(max = 50, message = "主要情绪长度不能超过50个字符")
    @TableField("dominant_emotion")
    private String dominantEmotion;

    @Schema(description = "情绪触发因素")
    @TableField("emotion_triggers")
    private String emotionTriggers;

    @Schema(description = "日记内容")
    @TableField("diary_content")
    private String diaryContent;

    @Schema(description = "睡眠质量(1-5)")
    @Min(value = 1, message = "睡眠质量评分不能小于1")
    @Max(value = 5, message = "睡眠质量评分不能大于5")
    @TableField("sleep_quality")
    private Integer sleepQuality;

    @Schema(description = "压力水平(1-5)")
    @Min(value = 1, message = "压力水平评分不能小于1")
    @Max(value = 5, message = "压力水平评分不能大于5")
    @TableField("stress_level")
    private Integer stressLevel;

    @Schema(description = "AI情绪分析结果(JSON格式)")
    @TableField("ai_emotion_analysis")
    private String aiEmotionAnalysis;

    @Schema(description = "AI分析更新时间")
    @TableField("ai_analysis_updated_at")
    private LocalDateTime aiAnalysisUpdatedAt;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 获取情绪评分的描述
     * @return 情绪评分描述
     */
    public String getMoodScoreDesc() {
        if (moodScore == null) {
            return "未知";
        }
        return switch (moodScore) {
            case 1 -> "非常糟糕";
            case 2 -> "糟糕";
            case 3 -> "不好";
            case 4 -> "略差";
            case 5 -> "一般";
            case 6 -> "还可以";
            case 7 -> "不错";
            case 8 -> "很好";
            case 9 -> "非常好";
            case 10 -> "极好";
            default -> "无效评分";
        };
    }

    /**
     * 获取睡眠质量的描述
     * @return 睡眠质量描述
     */
    public String getSleepQualityDesc() {
        if (sleepQuality == null) {
            return "未记录";
        }
        return switch (sleepQuality) {
            case 1 -> "很差";
            case 2 -> "较差";
            case 3 -> "一般";
            case 4 -> "良好";
            case 5 -> "优秀";
            default -> "无效评分";
        };
    }

    /**
     * 获取压力水平的描述
     * @return 压力水平描述
     */
    public String getStressLevelDesc() {
        if (stressLevel == null) {
            return "未记录";
        }
        return switch (stressLevel) {
            case 1 -> "很低";
            case 2 -> "较低";
            case 3 -> "中等";
            case 4 -> "较高";
            case 5 -> "很高";
            default -> "无效评分";
        };
    }

    /**
     * 判断是否为积极情绪
     * 根据情绪评分判断，7分及以上认为是积极情绪
     * @return true-积极情绪，false-非积极情绪
     */
    public boolean isPositiveMood() {
        return moodScore != null && moodScore >= 7;
    }

    /**
     * 判断是否为消极情绪
     * 根据情绪评分判断，4分及以下认为是消极情绪
     * @return true-消极情绪，false-非消极情绪
     */
    public boolean isNegativeMood() {
        return moodScore != null && moodScore <= 4;
    }

    /**
     * 判断是否有AI情绪分析数据
     * @return true-有AI分析数据，false-无AI分析数据
     */
    public boolean hasAiEmotionAnalysis() {
        return aiEmotionAnalysis != null && !aiEmotionAnalysis.trim().isEmpty();
    }

    /**
     * 判断AI情绪分析数据是否需要更新
     * @param thresholdMinutes 更新阈值（分钟）
     * @return true-需要更新，false-不需要更新
     */
    public boolean needsAiAnalysisUpdate(int thresholdMinutes) {
        if (aiAnalysisUpdatedAt == null) {
            return true;
        }
        return java.time.Duration.between(aiAnalysisUpdatedAt, LocalDateTime.now()).toMinutes() >= thresholdMinutes;
    }

    /**
     * 获取分析内容（用于AI分析）
     * 组合日记内容和情绪触发因素
     * @return 用于AI分析的文本内容
     */
    public String getAnalysisContent() {
        StringBuilder content = new StringBuilder();
        
        if (emotionTriggers != null && !emotionTriggers.trim().isEmpty()) {
            content.append("情绪触发因素：").append(emotionTriggers).append("\n");
        }
        
        if (diaryContent != null && !diaryContent.trim().isEmpty()) {
            content.append("日记内容：").append(diaryContent);
        }
        
        return content.toString().trim();
    }
}

