package org.example.springboot.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 咨询消息响应DTO
 * @author system
 */
@Data
@Schema(description = "咨询消息响应DTO")
public class ConsultationMessageResponseDTO {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "发送者类型 1:用户 2:AI助手")
    private Integer senderType;

    @Schema(description = "发送者类型描述")
    private String senderTypeDesc;

    @Schema(description = "消息类型 1:文本")
    private Integer messageType;

    @Schema(description = "消息类型描述")
    private String messageTypeDesc;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "情绪标签")
    private String emotionTag;

    @Schema(description = "使用的AI模型")
    private String aiModel;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "消息长度")
    private Integer contentLength;

    /**
     * 计算消息长度
     */
    public void calculateContentLength() {
        this.contentLength = content != null ? content.length() : 0;
    }

    /**
     * 获取消息预览（截取前100个字符）
     */
    public String getContentPreview() {
        if (content == null) {
            return "";
        }
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }
}
