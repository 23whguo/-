package org.example.springboot.DTO.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户信息更新命令DTO
 * @author system
 */
@Data
@Schema(description = "用户信息更新命令")
public class UserUpdateCommandDTO {

    @Schema(description = "邮箱", example = "test@drone.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Schema(description = "昵称", example = "测试用户")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Schema(description = "头像", example = "/avatars/user.jpg")
    @Size(max = 255, message = "头像路径长度不能超过255个字符")
    private String avatar;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "性别 0:未知 1:男 2:女", example = "1")
    private Integer gender;

    @Schema(description = "生日", example = "1990-01-01")
    private LocalDate birthday;

    @Schema(description = "用户类型 1:普通用户 2:管理员", example = "1")
    private Integer userType;

    @Schema(description = "用户状态 0:禁用 1:正常", example = "1")
    private Integer status;
}
