package org.example.springboot.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.response.UserDetailResponseDTO;
import org.example.springboot.enumClass.UserStatus;
import org.example.springboot.service.UserService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT认证过滤器
 * 
 * 核心职责：
 * 1. 从HTTP请求中提取JWT token
 * 2. 验证token有效性和完整性
 * 3. 设置Spring Security认证上下文
 * 
 * 设计原则：
 * - 继承OncePerRequestFilter确保每个请求只执行一次
 * - 与Spring Security深度集成
 * - 完善的异常处理和安全验证
 * - 统一从token获取用户信息，不依赖request属性
 * 
 * @author system
 * @date 2025-01-13
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        log.debug("JWT认证过滤器处理请求：{} {}", method, requestUri);

        try {
            // 1. 提取JWT token
            String token = JwtTokenUtils.extractTokenFromRequest(request);

            if (StringUtils.hasText(token)) {
                log.debug("成功提取token，长度：{}", token.length());

                // 2. 验证token并获取用户信息
                JwtTokenUtils.TokenValidationResult validationResult = JwtTokenUtils.validateToken(token);

                if (validationResult != null && validationResult.isValid()) {
                    // 3. 检查token是否过期
                    if (JwtTokenUtils.isTokenExpired(token)) {
                        log.warn("JWT token已过期，用户ID：{}，用户名：{}", 
                            validationResult.getUserId(), validationResult.getUsername());
                        clearSecurityContext();
                        filterChain.doFilter(request, response);
                        return;
                    }

                    // 4. 查询用户信息验证用户状态
                    UserDetailResponseDTO user = userService.getUserById(validationResult.getUserId());

                    if (user != null && UserStatus.NORMAL.getCode().equals(user.getStatus())) {
                        // 5. 创建Spring Security认证对象
                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + validationResult.getRoleType())
                        );

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        validationResult.getUsername(),
                                        null,
                                        authorities
                                );

                        // 6. 设置认证信息到Spring Security上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // 7. 将token存储到请求属性中供后续使用
                        request.setAttribute("jwtToken", token);

                        log.debug("JWT认证成功，用户ID：{}，用户名：{}，角色：{}", 
                            validationResult.getUserId(), validationResult.getUsername(), validationResult.getRoleType());
                    } else {
                        log.warn("JWT验证失败：用户不存在或已被禁用，用户ID：{}，用户名：{}", 
                            validationResult.getUserId(), validationResult.getUsername());
                        clearSecurityContext();
                    }
                } else {
                    log.warn("JWT验证失败：token格式错误或内容不完整");
                    clearSecurityContext();
                }
            } else {
                log.debug("未找到token，跳过JWT认证");
            }
        } catch (JWTVerificationException e) {
            log.warn("JWT验证失败：{}，清理认证上下文", e.getMessage());
            clearSecurityContext();
        } catch (Exception e) {
            log.error("JWT认证过程中发生异常，请求：{} {}，异常：{}，清理认证上下文", 
                method, requestUri, e.getMessage(), e);
            clearSecurityContext();
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 清理Spring Security认证上下文
     * 确保在认证失败时不会保留任何认证信息
     */
    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}