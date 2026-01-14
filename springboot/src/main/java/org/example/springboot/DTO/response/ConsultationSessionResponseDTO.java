package org.example.springboot.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 咨询会话响应DTO
 * @author system
 */
@Data
@Schema(description = "咨询会话响应DTO")
public class ConsultationSessionResponseDTO {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "会话标题")
    private String sessionTitle;

    @Schema(description = "开始时间")
    private LocalDateTime startedAt;

    @Schema(description = "会话持续时间（分钟）")
    private Long durationMinutes;

    @Schema(description = "消息总数")
    private Integer messageCount;

    @Schema(description = "最后一条消息内容（预览）")
    private String lastMessageContent;

    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "情绪标签列表")
    private List<String> emotionTags;

    @Schema(description = "主要情绪")
    private String primaryEmotion;
}
