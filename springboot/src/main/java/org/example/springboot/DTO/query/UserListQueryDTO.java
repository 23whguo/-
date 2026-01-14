package org.example.springboot.DTO.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户列表查询DTO
 * @author system
 */
@Data
@Schema(description = "用户列表查询")
public class UserListQueryDTO {

    @Schema(description = "用户名（模糊查询）", example = "admin")
    private String username;

    @Schema(description = "邮箱（模糊查询）", example = "admin@drone.com")
    private String email;

    @Schema(description = "昵称（模糊查询）", example = "管理员")
    private String nickname;

    @Schema(description = "手机号（模糊查询）", example = "13800138000")
    private String phone;

    @Schema(description = "用户类型 1:普通用户 2:管理员", example = "1")
    private Integer userType;

    @Schema(description = "用户状态 0:禁用 1:正常", example = "1")
    private Integer status;

    @Schema(description = "当前页码", example = "1")
    private Integer currentPage = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;
}
