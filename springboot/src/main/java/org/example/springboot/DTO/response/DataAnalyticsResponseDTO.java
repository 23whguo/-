package org.example.springboot.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据分析响应DTO
 * @author system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "数据分析响应DTO")
public class DataAnalyticsResponseDTO {

    @Schema(description = "系统概览数据")
    private SystemOverview systemOverview;

    @Schema(description = "情绪热力图数据")
    private EmotionHeatmapData emotionHeatmap;

    @Schema(description = "情绪趋势数据")
    private List<EmotionTrendData> emotionTrend;

    @Schema(description = "咨询会话统计")
    private ConsultationStatistics consultationStats;

    @Schema(description = "用户活跃度数据")
    private List<UserActivityData> userActivity;

    /**
     * 系统概览数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "系统概览数据")
    public static class SystemOverview {
        @Schema(description = "总用户数")
        private Long totalUsers;

        @Schema(description = "活跃用户数")
        private Long activeUsers;

        @Schema(description = "情绪日记总数")
        private Long totalDiaries;

        @Schema(description = "咨询会话总数")
        private Long totalSessions;

        @Schema(description = "平均情绪评分")
        private BigDecimal avgMoodScore;

        @Schema(description = "今日新增用户")
        private Long todayNewUsers;

        @Schema(description = "今日新增日记")
        private Long todayNewDiaries;

        @Schema(description = "今日新增会话")
        private Long todayNewSessions;
    }

    /**
     * 情绪热力图数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "情绪热力图数据")
    public static class EmotionHeatmapData {
        @Schema(description = "热力图网格数据 - 按小时和星期分布")
        private List<List<HeatmapPoint>> gridData;

        @Schema(description = "情绪分布统计")
        private Map<String, Integer> emotionDistribution;

        @Schema(description = "最高情绪活跃时段")
        private String peakEmotionTime;

        @Schema(description = "数据统计时间范围")
        private String dateRange;
    }

    /**
     * 热力图点数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "热力图点数据")
    public static class HeatmapPoint {
        @Schema(description = "X坐标(小时: 0-23)")
        private Integer x;

        @Schema(description = "Y坐标(星期: 0-6, 0为周日)")
        private Integer y;

        @Schema(description = "数值强度(记录数量)")
        private Integer value;

        @Schema(description = "平均情绪评分")
        private BigDecimal avgMoodScore;

        @Schema(description = "主要情绪类型")
        private String dominantEmotion;
    }

    /**
     * 情绪趋势数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "情绪趋势数据")
    public static class EmotionTrendData {
        @Schema(description = "日期")
        private LocalDate date;

        @Schema(description = "平均情绪评分")
        private BigDecimal avgMoodScore;

        @Schema(description = "记录数量")
        private Integer recordCount;

        @Schema(description = "正面情绪占比")
        private BigDecimal positiveRatio;

        @Schema(description = "负面情绪占比")
        private BigDecimal negativeRatio;

        @Schema(description = "主要情绪类型")
        private String dominantEmotion;
    }

    /**
     * 咨询会话统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "咨询会话统计")
    public static class ConsultationStatistics {
        @Schema(description = "总会话数")
        private Long totalSessions;

        @Schema(description = "平均会话时长(分钟)")
        private BigDecimal avgDurationMinutes;

        @Schema(description = "情绪改善会话数")
        private Long improvedSessions;

        @Schema(description = "情绪改善率")
        private BigDecimal improvementRate;

        @Schema(description = "每日会话数趋势")
        private List<DailySessionCount> dailyTrend;

        @Schema(description = "高频情绪标签")
        private Map<String, Integer> topEmotionTags;
    }

    /**
     * 每日会话数统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "每日会话数统计")
    public static class DailySessionCount {
        @Schema(description = "日期")
        private LocalDate date;

        @Schema(description = "会话数量")
        private Integer sessionCount;

        @Schema(description = "参与用户数")
        private Integer userCount;
    }

    /**
     * 用户活跃度数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户活跃度数据")
    public static class UserActivityData {
        @Schema(description = "日期")
        private LocalDate date;

        @Schema(description = "活跃用户数")
        private Integer activeUsers;

        @Schema(description = "新增用户数")
        private Integer newUsers;

        @Schema(description = "日记记录用户数")
        private Integer diaryUsers;

        @Schema(description = "咨询用户数")
        private Integer consultationUsers;
    }
}
