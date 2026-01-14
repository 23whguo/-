package org.example.springboot.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录响应DTO
 * @author system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录响应")
public class UserLoginResponseDTO {

    @Schema(description = "用户信息")
    private UserDetailResponseDTO userInfo;

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "角色代码")
    private String roleType;

}
