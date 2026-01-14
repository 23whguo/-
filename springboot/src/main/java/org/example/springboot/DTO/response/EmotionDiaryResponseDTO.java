package org.example.springboot.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 情绪日记响应DTO
 * @author system
 */
@Data
@Schema(description = "情绪日记响应DTO")
public class EmotionDiaryResponseDTO {

    @Schema(description = "日记ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "日记日期")
    private LocalDate diaryDate;

    @Schema(description = "情绪评分(1-10)")
    private Integer moodScore;

    @Schema(description = "情绪评分描述")
    private String moodScoreDesc;

    @Schema(description = "主要情绪")
    private String dominantEmotion;

    @Schema(description = "情绪触发因素")
    private String emotionTriggers;

    @Schema(description = "日记内容")
    private String diaryContent;

    @Schema(description = "日记内容预览")
    private String diaryContentPreview;

    @Schema(description = "睡眠质量(1-5)")
    private Integer sleepQuality;

    @Schema(description = "睡眠质量描述")
    private String sleepQualityDesc;

    @Schema(description = "压力水平(1-5)")
    private Integer stressLevel;

    @Schema(description = "压力水平描述")
    private String stressLevelDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "是否为积极情绪")
    private Boolean isPositiveMood;

    @Schema(description = "是否为消极情绪")
    private Boolean isNegativeMood;

    @Schema(description = "AI情绪分析结果(JSON格式)")
    private String aiEmotionAnalysis;

    @Schema(description = "AI分析更新时间")
    private LocalDateTime aiAnalysisUpdatedAt;

    @Schema(description = "是否有AI情绪分析")
    private Boolean hasAiEmotionAnalysis;

    @Schema(description = "AI分析状态：PENDING-分析中，COMPLETED-已完成，FAILED-分析失败")
    private String aiAnalysisStatus;

    /**
     * 获取日记内容预览（截取前100个字符）
     */
    public String getDiaryContentPreview() {
        if (diaryContent == null) {
            return "";
        }
        return diaryContent.length() > 100 ? diaryContent.substring(0, 100) + "..." : diaryContent;
    }

    /**
     * 计算内容长度
     */
    public int getContentLength() {
        return diaryContent != null ? diaryContent.length() : 0;
    }

    /**
     * 判断是否包含情绪触发因素
     */
    public boolean hasEmotionTriggers() {
        return emotionTriggers != null && !emotionTriggers.trim().isEmpty();
    }
}

