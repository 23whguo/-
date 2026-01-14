package org.example.springboot.DTO.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户创建命令DTO（管理员功能）
 * @author system
 */
@Data
@Schema(description = "用户创建命令")
public class UserCreateDTO {

    @Schema(description = "用户名", example = "newuser")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Schema(description = "邮箱", example = "newuser@drone.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在6到50个字符之间")
    private String password;

    @Schema(description = "昵称", example = "新用户")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "性别", example = "男", allowableValues = {"男", "女"})
    private String sex;

    @Schema(description = "个人简介", example = "无人机爱好者，喜欢航拍摄影")
    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String bio;

    @Schema(description = "用户类型", example = "user", allowableValues = {"user", "merchant", "admin"})
    @NotBlank(message = "用户类型不能为空")
    private String userType;

    @Schema(description = "用户状态", example = "active", allowableValues = {"active", "inactive", "banned"})
    private String status = "active";
}
