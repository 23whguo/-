package org.example.springboot.service.convert;

import org.example.springboot.DTO.command.UserRegisterCommandDTO;
import org.example.springboot.DTO.command.UserUpdateCommandDTO;
import org.example.springboot.DTO.response.UserDetailResponseDTO;
import org.example.springboot.DTO.response.UserLoginResponseDTO;

import org.example.springboot.entity.User;
import org.example.springboot.enumClass.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户转换类
 * @author system
 */
public class UserConvert {

    /**
     * 注册命令DTO转换为User实体
     * @param registerDTO 注册命令DTO
     * @param encodedPassword 加密后的密码
     * @return User实体
     */
    public static User registerCommandToEntity(UserRegisterCommandDTO registerDTO, String encodedPassword) {
        return User.builder()
                .username(registerDTO.getUsername())
                .email(registerDTO.getEmail())
                .password(encodedPassword)
                .nickname(registerDTO.getNickname())
                .phone(registerDTO.getPhone())
                .gender(registerDTO.getGender())
                .birthday(registerDTO.getBirthday())
                .userType(registerDTO.getUserType())
                .status(UserStatus.NORMAL.getCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * User实体转换为详情响应DTO
     * @param user User实体
     * @return 用户详情响应DTO
     */
    public static UserDetailResponseDTO entityToDetailResponse(User user) {
        return UserDetailResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .gender(user.getGender())
                .genderDisplayName(getGenderDisplayName(user.getGender()))
                .birthday(user.getBirthday())
                .userType(user.getUserType())
                .userTypeDisplayName(user.getUserTypeDisplayName())
                .status(user.getStatus())
                .statusDisplayName(user.getStatusDisplayName())
                .displayName(user.getDisplayName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * 构建登录响应DTO
     * @param token JWT令牌
     * @param userInfo 用户信息
     * @return 登录响应DTO
     */
    public static UserLoginResponseDTO buildLoginResponse(String token, UserDetailResponseDTO userInfo) {
        return UserLoginResponseDTO.builder()
                .userInfo(userInfo)
                .token(token)
                .roleType(userInfo.getUserType().toString())
                .build();
    }

    /**
     * 更新命令DTO转换为User实体
     * @param updateDTO 更新命令DTO
     * @return User实体
     */
    public static User updateCommandToEntity(UserUpdateCommandDTO updateDTO) {
        User user = new User();
        user.setEmail(updateDTO.getEmail());
        user.setNickname(updateDTO.getNickname());
        user.setAvatar(updateDTO.getAvatar());
        user.setPhone(updateDTO.getPhone());
        user.setGender(updateDTO.getGender());
        user.setBirthday(updateDTO.getBirthday());
        user.setUserType(updateDTO.getUserType());
        user.setStatus(updateDTO.getStatus());
        return user;
    }

    /**
     * 获取性别显示名称
     * @param gender 性别代码
     * @return 性别显示名称
     */
    private static String getGenderDisplayName(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case 1:
                return "男";
            case 2:
                return "女";
            default:
                return "未知";
        }
    }


}
