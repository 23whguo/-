package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 咨询消息实体类
 * @author system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("consultation_message")
@Schema(description = "咨询消息实体类")
public class ConsultationMessage {

    @TableId(type = IdType.AUTO)
    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "会话ID")
    @NotNull(message = "会话ID不能为空")
    @TableField("session_id")
    private Long sessionId;

    @Schema(description = "发送者类型 1:用户 2:AI助手")
    @NotNull(message = "发送者类型不能为空")
    @TableField("sender_type")
    private Integer senderType;

    @Schema(description = "消息类型 1:文本")
    @TableField("message_type")
    private Integer messageType;

    @Schema(description = "消息内容")
    @NotBlank(message = "消息内容不能为空")
    private String content;

    @Schema(description = "情绪标签")
    @Size(max = 50, message = "情绪标签长度不能超过50个字符")
    @TableField("emotion_tag")
    private String emotionTag;

    @Schema(description = "使用的AI模型")
    @Size(max = 50, message = "AI模型名称长度不能超过50个字符")
    @TableField("ai_model")
    private String aiModel;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 判断是否为用户消息
     */
    public boolean isUserMessage() {
        return senderType != null && senderType == 1;
    }

    /**
     * 判断是否为AI消息
     */
    public boolean isAiMessage() {
        return senderType != null && senderType == 2;
    }

    /**
     * 获取发送者类型描述
     */
    public String getSenderTypeDesc() {
        if (senderType == null) {
            return "未知";
        }
        return switch (senderType) {
            case 1 -> "用户";
            case 2 -> "AI助手";
            default -> "未知";
        };
    }

    /**
     * 获取消息类型描述
     */
    public String getMessageTypeDesc() {
        if (messageType == null) {
            return "未知";
        }
        return switch (messageType) {
            case 1 -> "文本";
            default -> "未知";
        };
    }
}
