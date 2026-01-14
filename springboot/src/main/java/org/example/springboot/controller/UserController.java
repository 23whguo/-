package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.command.*;
import org.example.springboot.DTO.query.UserListQueryDTO;
import org.example.springboot.DTO.response.UserDetailResponseDTO;
import org.example.springboot.DTO.response.UserLoginResponseDTO;
import org.example.springboot.common.Result;
import org.example.springboot.enumClass.UserType;
import org.example.springboot.service.UserService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * @author system
 */
@Tag(name = "用户管理")
@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginCommandDTO loginDTO) {
        log.info("用户登录请求: {}", loginDTO.getUsername());
        UserLoginResponseDTO response = userService.login(loginDTO);
        return Result.success("登录成功", response);
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/add")
    public Result<UserDetailResponseDTO> register(@Valid @RequestBody UserRegisterCommandDTO registerDTO) {
        log.info("用户注册请求: {}", registerDTO.getUsername());
        UserDetailResponseDTO response = userService.register(registerDTO);
        return Result.success("注册成功", response);
    }




    /**
     * 获取当前登录用户信息
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/current")
    public Result<UserDetailResponseDTO> getCurrentUser() {
        try {
            Long currentUserId = JwtTokenUtils.getCurrentUserId();
            log.info("获取当前用户信息请求: userId={}", currentUserId);
            
            UserDetailResponseDTO userInfo = userService.getUserById(currentUserId);
            return Result.success(userInfo);
            
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 用户更新自己的个人信息
     */
    @Operation(summary = "更新个人信息")
    @PutMapping("/profile")
    public Result<UserDetailResponseDTO> updateProfile(@Valid @RequestBody UserUpdateCommandDTO updateDTO) {
        try {
            Long currentUserId = JwtTokenUtils.getCurrentUserId();
            log.info("用户更新个人信息请求: userId={}", currentUserId);
            
            UserDetailResponseDTO userInfo = userService.updateUserProfile(currentUserId, updateDTO);
            return Result.success("个人信息更新成功", userInfo);
            
        } catch (Exception e) {
            log.error("更新个人信息失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 用户修改自己的密码
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        try {
            Long currentUserId = JwtTokenUtils.getCurrentUserId();
            log.info("用户修改密码请求: userId={}", currentUserId);
            
            userService.changeUserPassword(currentUserId, passwordUpdateDTO);
            return Result.success();
            
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 忘记密码
     */
    @Operation(summary = "忘记密码")
    @GetMapping("/forget")
    public Result<Void> forgetPassword(
            @Parameter(description = "邮箱") @RequestParam String email,
            @Parameter(description = "新密码") @RequestParam String newPassword) {

        log.info("忘记密码请求: email={}", email);
        userService.resetPasswordByEmail(email, newPassword);
        return Result.success();
    }

    /**
     * 用户退出登录
     */
    @Operation(summary = "用户退出登录")
    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        try {
            Long currentUserId = JwtTokenUtils.getCurrentUserId();
            log.info("用户退出登录请求: userId={}", currentUserId);
            
            
            return Result.success("退出登录成功");
            
        } catch (Exception e) {
            log.error("退出登录处理失败", e);
            // 即使处理失败，也返回成功，因为前端会清除本地状态
            return Result.success("退出登录成功");
        }
    }




    /**
     * 分页查询用户列表（管理员功能）
     */
    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    public Result<Page<UserDetailResponseDTO>> getUserPage(
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "邮箱") @RequestParam(required = false) String email,
            @Parameter(description = "昵称") @RequestParam(required = false) String nickname,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "用户类型 1:普通用户 2:管理员") @RequestParam(required = false) Integer userType,
            @Parameter(description = "用户状态 0:禁用 1:正常") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size
           ) {

        // 权限检查：只有管理员可以查看用户列表
        Integer currentUserRole = JwtTokenUtils.getCurrentUserRole();
        if (!UserType.ADMIN.getCode().equals(currentUserRole)) {
            return Result.error("权限不足，当前角色：" + currentUserRole + "，需要管理员权限");
        }

        UserListQueryDTO queryDTO = new UserListQueryDTO();
        queryDTO.setUsername(username);
        queryDTO.setEmail(email);
        queryDTO.setNickname(nickname);
        queryDTO.setPhone(phone);
        queryDTO.setUserType(userType);
        queryDTO.setStatus(status);
        queryDTO.setCurrentPage(currentPage);
        queryDTO.setSize(size);

        log.info("管理员查询用户列表: page={}, size={}", currentPage, size);
        Page<UserDetailResponseDTO> response = userService.getUserPage(queryDTO);
        return Result.success(response);
    }

    /**
     * 获取用户统计数据（管理员功能）
     */
    @Operation(summary = "获取用户统计数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getUserStatistics(HttpServletRequest request) {
        // 权限检查：只有管理员可以查看统计数据
        Integer currentUserRole = JwtTokenUtils.getCurrentUserRole();
        if (!UserType.ADMIN.getCode().equals(currentUserRole)) {
            return Result.error("权限不足");
        }

        log.info("管理员查询用户统计数据");
        Map<String, Object> statistics = userService.getUserStatistics();
        return Result.success(statistics);
    }

    /**
     * 更新用户信息（管理员功能）
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<Void> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateCommandDTO updateDTO,
            HttpServletRequest request) {
        
        // 权限检查：只有管理员可以更新用户信息
        Integer currentUserRole = JwtTokenUtils.getCurrentUserRole();
        if (!UserType.ADMIN.getCode().equals(currentUserRole)) {
            return Result.error("权限不足");
        }

        log.info("管理员更新用户信息: userId={}", id);
        userService.updateUser(id, updateDTO);
        return Result.success();
    }

    /**
     * 更新用户状态（管理员功能）
     */
    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "用户状态 0:禁用 1:正常") @RequestParam Integer status,
            HttpServletRequest request) {
        
        // 权限检查：只有管理员可以更新用户状态
        Integer currentUserRole = JwtTokenUtils.getCurrentUserRole();
        if (!UserType.ADMIN.getCode().equals(currentUserRole)) {
            return Result.error("权限不足");
        }

        log.info("管理员更新用户状态: userId={}, status={}", id, status);
        userService.updateUserStatus(id, status);
        return Result.success();
    }

    /**
     * 删除用户（管理员功能）
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            HttpServletRequest request) {
        
        // 权限检查：只有管理员可以删除用户
        Integer currentUserRole = JwtTokenUtils.getCurrentUserRole();
        if (!UserType.ADMIN.getCode().equals(currentUserRole)) {
            return Result.error("权限不足");
        }

        log.info("管理员删除用户: userId={}", id);
        userService.deleteUser(id);
        return Result.success();
    }




}
