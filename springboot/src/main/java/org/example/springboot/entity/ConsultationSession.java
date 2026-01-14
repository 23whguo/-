package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 咨询会话实体类
 * @author system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("consultation_session")
@Schema(description = "咨询会话实体类")
public class ConsultationSession {

    @TableId(type = IdType.AUTO)
    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "会话标题")
    @Size(max = 200, message = "会话标题长度不能超过200个字符")
    @TableField("session_title")
    private String sessionTitle;

    @Schema(description = "开始时间")
    @TableField("started_at")
    private LocalDateTime startedAt;

    @Schema(description = "最后一次情绪分析结果(JSON格式)")
    @TableField("last_emotion_analysis")
    private String lastEmotionAnalysis;

    @Schema(description = "最后一次情绪分析更新时间")
    @TableField("last_emotion_updated_at")
    private LocalDateTime lastEmotionUpdatedAt;

    /**
     * 计算会话持续时间（分钟）
     * 从开始时间到现在的持续时间
     */
    public Long getDurationMinutes() {
        if (startedAt == null) {
            return null;
        }
        LocalDateTime endTime = LocalDateTime.now();
        return java.time.Duration.between(startedAt, endTime).toMinutes();
    }

    /**
     * 判断是否有情绪分析数据
     */
    public boolean hasEmotionAnalysis() {
        return lastEmotionAnalysis != null && !lastEmotionAnalysis.trim().isEmpty();
    }

    /**
     * 判断情绪分析数据是否需要更新
     * @param thresholdMinutes 更新阈值（分钟）
     */
    public boolean needsEmotionAnalysisUpdate(int thresholdMinutes) {
        if (lastEmotionUpdatedAt == null) {
            return true;
        }
        return java.time.Duration.between(lastEmotionUpdatedAt, LocalDateTime.now()).toMinutes() >= thresholdMinutes;
    }
}
