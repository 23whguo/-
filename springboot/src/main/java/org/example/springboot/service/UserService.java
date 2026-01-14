package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map;
import java.util.HashMap;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.example.springboot.entity.User;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.DTO.command.*;
import org.example.springboot.DTO.query.*;
import org.example.springboot.DTO.response.*;
import org.example.springboot.enumClass.UserType;
import org.example.springboot.enumClass.UserStatus;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.util.JwtTokenUtils;
import jakarta.annotation.Resource;
import org.example.springboot.service.convert.UserConvert;

/**
 * 用户业务逻辑层
 * @author system
 */
@Slf4j
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录
     * @param loginDTO 登录命令
     * @return 登录响应
     */
    public UserLoginResponseDTO login(UserLoginCommandDTO loginDTO) {
        try {
            // 根据用户名或邮箱查找用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, loginDTO.getUsername())
                    .or()
                    .eq(User::getEmail, loginDTO.getUsername());
            User user = userMapper.selectOne(queryWrapper);


            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 验证密码 - 对输入密码进行trim处理
            String inputPassword = loginDTO.getPassword().trim();
            if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
                throw new BusinessException("用户名或密码错误");
            }

            // 检查用户状态
            if (!user.isActive()) {
                if (user.isDisabled()) {
                    throw new BusinessException("账号已被禁用，请联系管理员");
                }
            }

            // 生成JWT token
            String token = JwtTokenUtils.generateToken(user.getId(), user.getUsername(), user.getUserType());

            // 构建响应
            UserDetailResponseDTO userInfo = UserConvert.entityToDetailResponse(user);
            return UserConvert.buildLoginResponse(token, userInfo);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户登录失败", e);
            throw new ServiceException("登录失败，请稍后重试");
        }
    }

    /**
     * 用户注册
     * @param registerDTO 注册命令
     * @return 用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDetailResponseDTO register(UserRegisterCommandDTO registerDTO) {
        try {
            // 验证密码确认 - 对输入密码进行trim处理
            String password = registerDTO.getPassword().trim();
            String confirmPassword = registerDTO.getConfirmPassword().trim();
            if (!password.equals(confirmPassword)) {
                throw new BusinessException("两次输入的密码不一致");
            }

            // 检查用户名是否存在
            LambdaQueryWrapper<User> usernameQuery = new LambdaQueryWrapper<>();
            usernameQuery.eq(User::getUsername, registerDTO.getUsername());
            if (userMapper.selectCount(usernameQuery) > 0) {
                throw new BusinessException("用户名已存在");
            }

            // 检查邮箱是否存在
            LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
            emailQuery.eq(User::getEmail, registerDTO.getEmail());
            if (userMapper.selectCount(emailQuery) > 0) {
                throw new BusinessException("邮箱已被注册");
            }

            // 验证用户类型
            if (!UserType.isValidCode(registerDTO.getUserType())) {
                throw new BusinessException("无效的用户类型");
            }

            // 创建用户
            String encodedPassword = passwordEncoder.encode(password);
            User user = UserConvert.registerCommandToEntity(registerDTO, encodedPassword);

            userMapper.insert(user);
            log.info("用户注册成功: {}", user.getUsername());

            return UserConvert.entityToDetailResponse(user);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户注册失败", e);
            throw new ServiceException("注册失败，请稍后重试");
        }
    }

    /**
     * 根据ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    public UserDetailResponseDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserConvert.entityToDetailResponse(user);
    }

    /**
     * 用户更新自己的个人信息
     * @param userId 用户ID
     * @param updateDTO 更新数据
     * @return 更新后的用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDetailResponseDTO updateUserProfile(Long userId, UserUpdateCommandDTO updateDTO) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 检查邮箱是否已存在（排除当前用户）
            if (StringUtils.hasText(updateDTO.getEmail()) && !updateDTO.getEmail().equals(user.getEmail())) {
                LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
                emailWrapper.eq(User::getEmail, updateDTO.getEmail())
                           .ne(User::getId, userId);
                if (userMapper.selectCount(emailWrapper) > 0) {
                    throw new BusinessException("邮箱已存在");
                }
            }

            // 检查手机号是否已存在（排除当前用户）
            if (StringUtils.hasText(updateDTO.getPhone()) && !updateDTO.getPhone().equals(user.getPhone())) {
                LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
                phoneWrapper.eq(User::getPhone, updateDTO.getPhone())
                           .ne(User::getId, userId);
                if (userMapper.selectCount(phoneWrapper) > 0) {
                    throw new BusinessException("手机号已存在");
                }
            }

            // 更新用户信息（普通用户不能修改用户类型和状态）
            User updateUser = new User();
            updateUser.setId(userId);
            updateUser.setNickname(updateDTO.getNickname());
            updateUser.setEmail(updateDTO.getEmail());
            updateUser.setPhone(updateDTO.getPhone());
            updateUser.setGender(updateDTO.getGender());
            updateUser.setBirthday(updateDTO.getBirthday());
            updateUser.setAvatar(updateDTO.getAvatar());
            updateUser.setUpdatedAt(LocalDateTime.now());
            
            userMapper.updateById(updateUser);
            log.info("用户个人信息更新成功: {}", user.getUsername());

            // 返回更新后的用户信息
            return getUserById(userId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户个人信息失败", e);
            throw new ServiceException("更新个人信息失败，请稍后重试");
        }
    }

    /**
     * 用户修改自己的密码
     * @param userId 用户ID
     * @param passwordUpdateDTO 密码更新数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeUserPassword(Long userId, PasswordUpdateDTO passwordUpdateDTO) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 验证旧密码
            if (!passwordEncoder.matches(passwordUpdateDTO.getOldPassword(), user.getPassword())) {
                throw new BusinessException("原密码错误");
            }

            // 检查新密码是否与旧密码相同
            if (passwordEncoder.matches(passwordUpdateDTO.getNewPassword(), user.getPassword())) {
                throw new BusinessException("新密码不能与原密码相同");
            }

            // 更新密码
            User updateUser = new User();
            updateUser.setId(userId);
            updateUser.setPassword(passwordEncoder.encode(passwordUpdateDTO.getNewPassword()));
            updateUser.setUpdatedAt(LocalDateTime.now());
            
            userMapper.updateById(updateUser);
            log.info("用户密码修改成功: {}", user.getUsername());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改用户密码失败", e);
            throw new ServiceException("修改密码失败，请稍后重试");
        }
    }




    /**
     * 分页查询用户列表
     * @param queryDTO 查询条件
     * @return 用户分页列表
     */
    public Page<UserDetailResponseDTO> getUserPage(UserListQueryDTO queryDTO) {
        try {
            Page<User> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getSize());

            // 构建查询条件
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            
            // 处理模糊搜索：如果多个字段都有相同的关键词，使用OR查询
            boolean hasKeywordSearch = StringUtils.hasText(queryDTO.getUsername()) && 
                                     StringUtils.hasText(queryDTO.getEmail()) && 
                                     StringUtils.hasText(queryDTO.getNickname()) && 
                                     StringUtils.hasText(queryDTO.getPhone()) &&
                                     queryDTO.getUsername().equals(queryDTO.getEmail()) &&
                                     queryDTO.getEmail().equals(queryDTO.getNickname()) &&
                                     queryDTO.getNickname().equals(queryDTO.getPhone());
            
            if (hasKeywordSearch) {
                // 全局搜索：在所有字段中搜索关键词
                String keyword = queryDTO.getUsername();
                queryWrapper.and(wrapper -> wrapper
                    .like(User::getUsername, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword)
                );
            } else {
                // 分别处理各个字段的搜索
                if (StringUtils.hasText(queryDTO.getUsername())) {
                    queryWrapper.like(User::getUsername, queryDTO.getUsername());
                }
                if (StringUtils.hasText(queryDTO.getEmail())) {
                    queryWrapper.like(User::getEmail, queryDTO.getEmail());
                }
                if (StringUtils.hasText(queryDTO.getNickname())) {
                    queryWrapper.like(User::getNickname, queryDTO.getNickname());
                }
                if (StringUtils.hasText(queryDTO.getPhone())) {
                    queryWrapper.like(User::getPhone, queryDTO.getPhone());
                }
            }
            
            if (queryDTO.getUserType() != null) {
                queryWrapper.eq(User::getUserType, queryDTO.getUserType());
            }
            if (queryDTO.getStatus() != null) {
                queryWrapper.eq(User::getStatus, queryDTO.getStatus());
            }
            queryWrapper.orderByDesc(User::getCreatedAt);

            Page<User> userPage = userMapper.selectPage(page, queryWrapper);

            // 转换为响应DTO
            Page<UserDetailResponseDTO> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
            List<UserDetailResponseDTO> records = userPage.getRecords().stream()
                    .map(UserConvert::entityToDetailResponse)
                    .toList();
            resultPage.setRecords(records);

            return resultPage;

        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            throw new ServiceException("查询失败，请稍后重试");
        }
    }

    /**
     * 删除用户
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 检查是否为管理员（防止删除管理员）
            if (user.isAdmin()) {
                throw new BusinessException("不能删除管理员账号");
            }

            userMapper.deleteById(userId);
            log.info("用户删除成功: {}", user.getUsername());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户删除失败", e);
            throw new ServiceException("删除失败，请稍后重试");
        }
    }

    /**
     * 获取用户统计数据
     * @return 统计数据
     */
    public Map<String, Object> getUserStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 查询总用户数
            Long totalUsers = userMapper.selectCount(null);
            
            // 查询活跃用户数（状态为正常）
            LambdaQueryWrapper<User> activeWrapper = new LambdaQueryWrapper<>();
            activeWrapper.eq(User::getStatus, UserStatus.NORMAL.getCode());
            Long activeUsers = userMapper.selectCount(activeWrapper);
            
            // 查询风险用户数（状态为禁用）
            LambdaQueryWrapper<User> riskWrapper = new LambdaQueryWrapper<>();
            riskWrapper.eq(User::getStatus, UserStatus.DISABLED.getCode());
            Long riskUsers = userMapper.selectCount(riskWrapper);
            
            // 查询今日新增用户数
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LambdaQueryWrapper<User> newWrapper = new LambdaQueryWrapper<>();
            newWrapper.ge(User::getCreatedAt, today);
            Long newUsers = userMapper.selectCount(newWrapper);
            
            statistics.put("totalUsers", totalUsers);
            statistics.put("activeUsers", activeUsers);
            statistics.put("riskUsers", riskUsers);
            statistics.put("newUsers", newUsers);
            
            // 移除模拟增长率数据，使用真实计算或暂时不提供
            // statistics.put("totalGrowth", 0.0);
            // statistics.put("activeGrowth", 0.0);  
            // statistics.put("newGrowth", 0.0);
            
            return statistics;
            
        } catch (Exception e) {
            log.error("获取用户统计数据失败", e);
            throw new ServiceException("获取统计数据失败，请稍后重试");
        }
    }

    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param updateDTO 更新数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, UserUpdateCommandDTO updateDTO) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 检查邮箱是否已存在（排除当前用户）
            if (StringUtils.hasText(updateDTO.getEmail()) && !updateDTO.getEmail().equals(user.getEmail())) {
                LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
                emailWrapper.eq(User::getEmail, updateDTO.getEmail())
                           .ne(User::getId, userId);
                if (userMapper.selectCount(emailWrapper) > 0) {
                    throw new BusinessException("邮箱已存在");
                }
            }

            // 检查手机号是否已存在（排除当前用户）
            if (StringUtils.hasText(updateDTO.getPhone()) && !updateDTO.getPhone().equals(user.getPhone())) {
                LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
                phoneWrapper.eq(User::getPhone, updateDTO.getPhone())
                           .ne(User::getId, userId);
                if (userMapper.selectCount(phoneWrapper) > 0) {
                    throw new BusinessException("手机号已存在");
                }
            }

            // 更新用户信息
            User updateUser = UserConvert.updateCommandToEntity(updateDTO);
            updateUser.setId(userId);
            updateUser.setUpdatedAt(LocalDateTime.now());
            
            userMapper.updateById(updateUser);
            log.info("用户信息更新成功: {}", user.getUsername());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            throw new ServiceException("更新失败，请稍后重试");
        }
    }

    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 新状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 验证状态值
            if (!UserStatus.isValidCode(status)) {
                throw new BusinessException("无效的用户状态");
            }

            // 检查是否为管理员（防止禁用管理员）
            if (user.isAdmin() && status.equals(UserStatus.DISABLED.getCode())) {
                throw new BusinessException("不能禁用管理员账号");
            }

            // 更新用户状态
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getId, userId)
                        .set(User::getStatus, status)
                        .set(User::getUpdatedAt, LocalDateTime.now());
            
            userMapper.update(null, updateWrapper);
            log.info("用户状态更新成功: {} -> {}", user.getUsername(), status);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            throw new ServiceException("状态更新失败，请稍后重试");
        }
    }





    /**
     * 通过邮箱重置密码
     * @param email 邮箱
     * @param newPassword 新密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByEmail(String email, String newPassword) {
        try {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail, email);
            User user = userMapper.selectOne(queryWrapper);

            if (user == null) {
                throw new BusinessException("邮箱不存在");
            }

            // 重置密码
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);

            log.info("用户密码重置成功: {}", user.getUsername());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("密码重置失败", e);
            throw new ServiceException("密码重置失败，请稍后重试");
        }
    }


}
