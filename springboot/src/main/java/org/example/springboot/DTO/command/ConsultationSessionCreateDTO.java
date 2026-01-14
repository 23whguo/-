package org.example.springboot.DTO.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 咨询会话创建DTO
 * @author system
 */
@Data
@Schema(description = "咨询会话创建DTO")
public class ConsultationSessionCreateDTO {

    @Schema(description = "会话标题")
    @Size(max = 200, message = "会话标题长度不能超过200个字符")
    private String sessionTitle;

    @Schema(description = "初始消息")
    @NotBlank(message = "初始消息不能为空")
    @Size(max = 2000, message = "初始消息长度不能超过2000个字符")
    private String initialMessage;
}
