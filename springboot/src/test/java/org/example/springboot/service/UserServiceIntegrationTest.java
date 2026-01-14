package org.example.springboot.service;

import org.example.springboot.DTO.command.UserRegisterCommandDTO;
import org.example.springboot.DTO.response.UserDetailResponseDTO;
import org.example.springboot.entity.User;
import org.example.springboot.enumClass.UserType;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService 集成测试
 * 测试真实的数据库交互和事务处理
 * @author system
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("用户服务集成测试")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("用户注册集成测试 - 成功注册")
    void register_Integration_Success() {
        // 准备测试数据
        UserRegisterCommandDTO registerDTO = new UserRegisterCommandDTO();
        registerDTO.setUsername("integrationtest");
        registerDTO.setEmail("integration@test.com");
        registerDTO.setPassword("123456");
        registerDTO.setConfirmPassword("123456");
        registerDTO.setNickname("集成测试用户");
        registerDTO.setPhone("13900139999");
        registerDTO.setUserType(UserType.USER.getCode());

        // 执行测试
        UserDetailResponseDTO result = userService.register(registerDTO);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("integrationtest", result.getUsername());
        assertEquals("integration@test.com", result.getEmail());
        assertEquals("集成测试用户", result.getNickname());
        assertEquals(UserType.USER.getCode(), result.getUserType());

        // 验证数据库中确实插入了数据
        User savedUser = userMapper.selectById(result.getId());
        assertNotNull(savedUser);
        assertEquals("integrationtest", savedUser.getUsername());
        assertEquals("integration@test.com", savedUser.getEmail());
    }

    @Test
    @DisplayName("用户注册集成测试 - 用户名重复")
    void register_Integration_DuplicateUsername() {
        // 使用测试数据中已存在的用户名
        UserRegisterCommandDTO registerDTO = new UserRegisterCommandDTO();
        registerDTO.setUsername("testuser"); // 这个用户名在测试数据中已存在
        registerDTO.setEmail("new@test.com");
        registerDTO.setPassword("123456");
        registerDTO.setConfirmPassword("123456");
        registerDTO.setNickname("重复用户名测试");
        registerDTO.setUserType(UserType.USER.getCode());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class,
            () -> userService.register(registerDTO));

        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    @DisplayName("获取用户信息集成测试 - 成功获取")
    void getUserById_Integration_Success() {
        // 使用测试数据中的用户ID
        Long userId = 2L; // testuser 的ID

        // 执行测试
        UserDetailResponseDTO result = userService.getUserById(userId);

        // 验证结果
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("测试用户", result.getNickname());
        assertEquals(UserType.USER.getCode(), result.getUserType());
    }

    @Test
    @DisplayName("删除用户集成测试 - 成功删除")
    void deleteUser_Integration_Success() {
        // 先创建一个测试用户
        UserRegisterCommandDTO registerDTO = new UserRegisterCommandDTO();
        registerDTO.setUsername("todelete");
        registerDTO.setEmail("todelete@test.com");
        registerDTO.setPassword("123456");
        registerDTO.setConfirmPassword("123456");
        registerDTO.setNickname("待删除用户");
        registerDTO.setUserType(UserType.USER.getCode());

        UserDetailResponseDTO created = userService.register(registerDTO);
        Long userId = created.getId();

        // 验证用户存在
        assertNotNull(userService.getUserById(userId));

        // 执行删除
        userService.deleteUser(userId);

        // 验证用户已被删除
        BusinessException exception = assertThrows(BusinessException.class,
            () -> userService.getUserById(userId));
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("重置密码集成测试 - 成功重置")
    void resetPasswordByEmail_Integration_Success() {
        // 使用测试数据中的邮箱
        String email = "test@example.com";
        String newPassword = "newpassword123";

        // 获取重置前的用户信息
        User userBefore = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
        );
        assertNotNull(userBefore);
        String oldPassword = userBefore.getPassword();

        // 执行密码重置
        userService.resetPasswordByEmail(email, newPassword);

        // 获取重置后的用户信息
        User userAfter = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
        );
        assertNotNull(userAfter);

        // 验证密码已更改
        assertNotEquals(oldPassword, userAfter.getPassword());
        assertNotNull(userAfter.getUpdatedAt());
    }
} 
