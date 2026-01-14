package org.example.springboot.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 情绪日记统计响应DTO
 * @author system
 */
@Data
@Schema(description = "情绪日记统计响应DTO")
public class EmotionDiaryStatisticsDTO {

    @Schema(description = "统计期间总天数")
    private Integer totalDays;

    @Schema(description = "记录天数")
    private Integer recordedDays;

    @Schema(description = "实际记录天数")
    private Integer recordDays;

    @Schema(description = "目标天数")
    private Integer targetDays;

    @Schema(description = "记录完成率")
    private BigDecimal completionRate;

    @Schema(description = "平均情绪评分")
    private BigDecimal averageMoodScore;

    @Schema(description = "最高情绪评分")
    private Integer maxMoodScore;

    @Schema(description = "最低情绪评分")
    private Integer minMoodScore;

    @Schema(description = "积极情绪天数")
    private Integer positiveDays;

    @Schema(description = "消极情绪天数")
    private Integer negativeDays;

    @Schema(description = "中性情绪天数")
    private Integer neutralDays;

    @Schema(description = "平均睡眠质量")
    private BigDecimal averageSleepQuality;

    @Schema(description = "平均压力水平")
    private BigDecimal averageStressLevel;

    @Schema(description = "情绪趋势数据（7天）")
    private List<MoodTrendData> moodTrend;

    @Schema(description = "情绪分布统计")
    private Map<String, Integer> emotionDistribution;

    @Schema(description = "睡眠质量分布")
    private Map<String, Integer> sleepQualityDistribution;

    @Schema(description = "压力水平分布")
    private Map<String, Integer> stressLevelDistribution;

    @Schema(description = "最常见的主要情绪")
    private String mostCommonEmotion;

    @Schema(description = "改善建议")
    private List<String> suggestions;

    /**
     * 情绪趋势数据内部类
     */
    @Data
    @Schema(description = "情绪趋势数据")
    public static class MoodTrendData {
        @Schema(description = "日期标签")
        private String dateLabel;

        @Schema(description = "情绪评分")
        private Integer moodScore;

        @Schema(description = "主要情绪")
        private String dominantEmotion;
    }

    /**
     * 计算积极情绪占比
     */
    public BigDecimal getPositiveRatio() {
        if (recordedDays == null || recordedDays == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(positiveDays != null ? positiveDays : 0)
                .divide(BigDecimal.valueOf(recordedDays), 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 计算消极情绪占比
     */
    public BigDecimal getNegativeRatio() {
        if (recordedDays == null || recordedDays == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(negativeDays != null ? negativeDays : 0)
                .divide(BigDecimal.valueOf(recordedDays), 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 获取情绪稳定性描述
     */
    public String getMoodStabilityDesc() {
        if (maxMoodScore == null || minMoodScore == null) {
            return "数据不足";
        }
        int range = maxMoodScore - minMoodScore;
        if (range <= 2) {
            return "非常稳定";
        } else if (range <= 4) {
            return "较为稳定";
        } else if (range <= 6) {
            return "中等波动";
        } else {
            return "波动较大";
        }
    }
}

