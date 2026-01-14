package org.example.springboot.DTO.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 密码更新命令DTO
 * @author system
 */
@Data
@Schema(description = "密码更新命令")
public class PasswordUpdateDTO {

    @Schema(description = "旧密码", example = "123456")
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码", example = "newpassword123")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "新密码长度必须在6到50个字符之间")
    private String newPassword;
}
