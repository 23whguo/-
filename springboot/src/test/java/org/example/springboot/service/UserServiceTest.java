package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.springboot.DTO.command.UserLoginCommandDTO;
import org.example.springboot.DTO.command.UserRegisterCommandDTO;
import org.example.springboot.DTO.query.UserListQueryDTO;
import org.example.springboot.DTO.response.UserDetailResponseDTO;
import org.example.springboot.DTO.response.UserLoginResponseDTO;
import org.example.springboot.entity.User;
import org.example.springboot.enumClass.UserStatus;
import org.example.springboot.enumClass.UserType;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 * @author system
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User adminUser;
    private User bannedUser;
    private UserLoginCommandDTO loginCommandDTO;
    private UserRegisterCommandDTO registerCommandDTO;
    private UserListQueryDTO queryDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("$2a$10$K8K8K8K8K8K8K8K8K8K8K.K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8")
                .nickname("测试用户")
                .phone("13800138000")
                .userType(UserType.USER.getCode())
                .status(UserStatus.NORMAL.getCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("admin")
                .email("admin@example.com")
                .password("$2a$10$encodedPassword")
                .userType(UserType.ADMIN.getCode())
                .status(UserStatus.NORMAL.getCode())
                .createdAt(LocalDateTime.now())
                .build();

        bannedUser = User.builder()
                .id(3L)
                .username("banneduser")
                .email("banned@example.com")
                .password("$2a$10$encodedPassword")
                .userType(UserType.USER.getCode())
                .status(UserStatus.DISABLED.getCode())
                .createdAt(LocalDateTime.now())
                .build();

        loginCommandDTO = new UserLoginCommandDTO();
        loginCommandDTO.setUsername("testuser");
        loginCommandDTO.setPassword("123456");

        registerCommandDTO = new UserRegisterCommandDTO();
        registerCommandDTO.setUsername("newuser");
        registerCommandDTO.setEmail("newuser@example.com");
        registerCommandDTO.setPassword("123456");
        registerCommandDTO.setConfirmPassword("123456");
        registerCommandDTO.setNickname("新用户");
        registerCommandDTO.setPhone("13900139000");
        registerCommandDTO.setUserType(UserType.USER.getCode());

        queryDTO = new UserListQueryDTO();
        queryDTO.setCurrentPage(1);
        queryDTO.setSize(10);
    }

    @Test
    @DisplayName("用户登录 - 用户不存在")
    void login_UserNotFound() {
        // 准备测试数据
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.login(loginCommandDTO));
        
        assertEquals("用户不存在", exception.getMessage());
        verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("用户登录 - 账号被禁用")
    void login_UserDisabled() {
        // 准备测试数据 - 设置密码匹配但用户被禁用
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(bannedUser);

        // 使用反射设置密码编码器，让密码验证通过
        ReflectionTestUtils.setField(userService, "passwordEncoder", 
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder() {
                @Override
                public boolean matches(CharSequence rawPassword, String encodedPassword) {
                    return true; // 模拟密码匹配成功
                }
            });

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.login(loginCommandDTO));
        
        assertEquals("账号已被禁用，请联系管理员", exception.getMessage());
        verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("用户注册 - 密码确认不一致")
    void register_PasswordMismatch() {
        // 准备测试数据
        registerCommandDTO.setConfirmPassword("different-password");

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.register(registerCommandDTO));
        
        assertEquals("两次输入的密码不一致", exception.getMessage());
        // 验证没有调用数据库操作
        verify(userMapper, never()).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("用户注册 - 用户名已存在")
    void register_UsernameExists() {
        // 准备测试数据 - 用户名查询返回大于0
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.register(registerCommandDTO));
        
        assertEquals("用户名已存在", exception.getMessage());
        verify(userMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("用户注册 - 邮箱已被注册")
    void register_EmailExists() {
        // 准备测试数据
        when(userMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L) // 第一次查询用户名不存在
                .thenReturn(1L); // 第二次查询邮箱已存在

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.register(registerCommandDTO));
        
        assertEquals("邮箱已被注册", exception.getMessage());
        verify(userMapper, times(2)).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("用户注册 - 无效的用户类型")
    void register_InvalidUserType() {
        // 准备测试数据
        registerCommandDTO.setUserType(999);
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.register(registerCommandDTO));
        
        assertEquals("无效的用户类型", exception.getMessage());
        verify(userMapper, times(2)).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID获取用户信息 - 成功获取")
    void getUserById_Success() {
        // 准备测试数据
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行测试
        UserDetailResponseDTO result = userService.getUserById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userMapper).selectById(1L);
    }

    @Test
    @DisplayName("根据ID获取用户信息 - 用户不存在")
    void getUserById_UserNotFound() {
        // 准备测试数据
        when(userMapper.selectById(1L)).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.getUserById(1L));
        
        assertEquals("用户不存在", exception.getMessage());
        verify(userMapper).selectById(1L);
    }

    @Test
    @DisplayName("分页查询用户列表 - 成功查询")
    void getUserPage_Success() {
        // 准备测试数据
        List<User> userList = Arrays.asList(testUser);
        Page<User> userPage = new Page<>(1, 10, 1);
        userPage.setRecords(userList);
        
        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(userPage);

        // 执行测试
        Page<UserDetailResponseDTO> result = userService.getUserPage(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals(testUser.getUsername(), result.getRecords().get(0).getUsername());
        verify(userMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询用户列表 - 带查询条件")
    void getUserPage_WithConditions() {
        // 准备测试数据
        queryDTO.setUsername("test");
        queryDTO.setEmail("test@");
        queryDTO.setNickname("测试");
        queryDTO.setUserType(UserType.USER.getCode());
        queryDTO.setStatus(UserStatus.NORMAL.getCode());

        List<User> userList = Arrays.asList(testUser);
        Page<User> userPage = new Page<>(1, 10, 1);
        userPage.setRecords(userList);
        
        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(userPage);

        // 执行测试
        Page<UserDetailResponseDTO> result = userService.getUserPage(queryDTO);

        // 验证结果
        assertNotNull(result);
        verify(userMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询用户列表 - 异常处理")
    void getUserPage_Exception() {
        // 准备测试数据 - 模拟数据库异常
        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试并验证异常
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> userService.getUserPage(queryDTO));
        
        assertEquals("查询失败，请稍后重试", exception.getMessage());
    }

    @Test
    @DisplayName("删除用户 - 成功删除")
    void deleteUser_Success() {
        // 准备测试数据
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.deleteById(1L)).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> userService.deleteUser(1L));

        // 验证结果
        verify(userMapper).selectById(1L);
        verify(userMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除用户 - 用户不存在")
    void deleteUser_UserNotFound() {
        // 准备测试数据
        when(userMapper.selectById(1L)).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.deleteUser(1L));
        
        assertEquals("用户不存在", exception.getMessage());
        verify(userMapper).selectById(1L);
        verify(userMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("删除用户 - 不能删除管理员")
    void deleteUser_CannotDeleteAdmin() {
        // 准备测试数据
        when(userMapper.selectById(2L)).thenReturn(adminUser);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.deleteUser(2L));
        
        assertEquals("不能删除管理员账号", exception.getMessage());
        verify(userMapper).selectById(2L);
        verify(userMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("通过邮箱重置密码 - 成功重置")
    void resetPasswordByEmail_Success() {
        // 准备测试数据
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> userService.resetPasswordByEmail("test@example.com", "newpassword"));

        // 验证结果
        verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    @DisplayName("通过邮箱重置密码 - 邮箱不存在")
    void resetPasswordByEmail_EmailNotFound() {
        // 准备测试数据
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.resetPasswordByEmail("nonexistent@example.com", "newpassword"));
        
        assertEquals("邮箱不存在", exception.getMessage());
        verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("通过邮箱重置密码 - 异常处理")
    void resetPasswordByEmail_Exception() {
        // 准备测试数据 - 模拟数据库异常
        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试并验证异常
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> userService.resetPasswordByEmail("test@example.com", "newpassword"));
        
        assertEquals("密码重置失败，请稍后重试", exception.getMessage());
    }

    @Test
    @DisplayName("登录数据库异常处理测试")
    void login_DatabaseException() {
        // 准备测试数据 - 模拟数据库异常
        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试并验证异常
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> userService.login(loginCommandDTO));
        
        assertEquals("登录失败，请稍后重试", exception.getMessage());
    }

    @Test
    @DisplayName("注册数据库异常处理测试")
    void register_DatabaseException() {
        // 准备测试数据 - 用户名和邮箱都不存在，但插入时出现异常
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试并验证异常
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> userService.register(registerCommandDTO));
        
        assertEquals("注册失败，请稍后重试", exception.getMessage());
    }

    @Test
    @DisplayName("删除用户数据库异常处理测试")
    void deleteUser_DatabaseException() {
        // 准备测试数据
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.deleteById(1L)).thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试并验证异常
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> userService.deleteUser(1L));
        
        assertEquals("删除失败，请稍后重试", exception.getMessage());
    }
} 
