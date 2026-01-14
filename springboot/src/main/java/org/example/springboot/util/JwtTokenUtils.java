package org.example.springboot.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.config.JwtConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

/**
 * JWT工具类 - 统一的JWT token管理
 *
 * 核心功能：
 * 1. 生成JWT token（使用userId、username、roleType作为标识）
 * 2. 验证JWT token有效性和完整性
 * 3. 解析token中的用户信息
 * 4. 从HTTP请求中提取token
 *
 * 设计原则：
 * - 统一从token获取用户信息，避免依赖request属性
 * - 简化token提取逻辑，支持多种Header方式
 * - 完善的异常处理和安全验证
 * - 使用配置文件中的密钥，提高安全性
 *
 * @author system
 * @date 2025-01-13
 */
@Slf4j
@Component
public class JwtTokenUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    
    /**
     * Token发行者 - 固定值
     */
    private static final String ISSUER = "mental-health-assistant";

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        JwtTokenUtils.applicationContext = applicationContext;
    }

    /**
     * 获取JWT配置
     */
    private static JwtConfig getJwtConfig() {
        return applicationContext.getBean(JwtConfig.class);
    }

    /**
     * Authorization Header名称
     */
    private static final String HEADER_NAME = "Authorization";

    /**
     * Token前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 生成JWT token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roleType 角色代码
     * @return JWT token
     * @throws RuntimeException 生成失败时抛出
     */
    public static String generateToken(Long userId, String username, Integer roleType) {
        try {
            JwtConfig config = getJwtConfig();
            Algorithm algorithm = Algorithm.HMAC256(config.getSecret());
            Date expireDate = new Date(System.currentTimeMillis() + config.getExpiration());

            String token = JWT.create()
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withClaim("roleType", roleType)
                    .withExpiresAt(expireDate)
                    .withIssuedAt(new Date())
                    .withIssuer(ISSUER)
                    .sign(algorithm);

            log.debug("JWT token生成成功，用户ID：{}，用户名：{}，角色：{}", userId, username, roleType);
            return token;
        } catch (Exception e) {
            log.error("生成JWT token失败，用户ID：{}，用户名：{}，角色：{}", userId, username, roleType, e);
            throw new RuntimeException("生成JWT token失败", e);
        }
    }

    /**
     * 验证JWT token有效性
     *
     * @param token JWT token
     * @return 解码后的JWT
     * @throws JWTVerificationException token验证失败
     */
    public static DecodedJWT verifyToken(String token) throws JWTVerificationException {
        if (!StringUtils.hasText(token)) {
            throw new JWTVerificationException("Token不能为空");
        }

        JwtConfig config = getJwtConfig();
        Algorithm algorithm = Algorithm.HMAC256(config.getSecret());
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }

    /**
     * 从token中获取用户ID
     *
     * @param token JWT token
     * @return 用户ID，解析失败返回null
     */
    public static Long getUserIdFromToken(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getClaim("userId").asLong();
        } catch (Exception e) {
            log.warn("从token获取用户ID失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从token中获取用户名
     *
     * @param token JWT token
     * @return 用户名，解析失败返回null
     */
    public static String getUsernameFromToken(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getClaim("username").asString();
        } catch (Exception e) {
            log.warn("从token获取用户名失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从token中获取角色代码
     *
     * @param token JWT token
     * @return 角色代码，解析失败返回null
     */
    public static Integer getRoleTypeFromToken(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            // 尝试作为Integer解析
            Integer roleType = jwt.getClaim("roleType").asInt();
            if (roleType != null) {
                return roleType;
            }
            // 如果失败，尝试作为String解析再转换为Integer
            String roleTypeStr = jwt.getClaim("roleType").asString();
            if (StringUtils.hasText(roleTypeStr)) {
                return Integer.valueOf(roleTypeStr);
            }
            return null;
        } catch (Exception e) {
            log.warn("从token获取角色代码失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查token是否过期
     *
     * @param token JWT token
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (Exception e) {
            log.warn("检查token过期状态失败：{}", e.getMessage());
            return true; // 解析失败视为过期
        }
    }

    /**
     * 从请求中提取JWT token（支持多种方式）
     *
     * @param request HTTP请求
     * @return token字符串，未找到或格式不正确返回null
     */
    public static String extractTokenFromRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // 方式1：从Authorization Header提取（标准方式）
        String authHeader = request.getHeader(HEADER_NAME);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }

        // 方式2：从token Header提取（前端使用的方式）
        String tokenHeader = request.getHeader("token");
        if (StringUtils.hasText(tokenHeader)) {
            return tokenHeader;
        }

        return null;
    }


    /**
     * 刷新token（生成新的token）
     *
     * @param oldToken 旧的token
     * @return 新的token，失败返回null
     */
    public static String refreshToken(String oldToken) {
        try {
            Long userId = getUserIdFromToken(oldToken);
            String username = getUsernameFromToken(oldToken);
            Integer roleType = getRoleTypeFromToken(oldToken);

            if (userId != null && username != null && roleType != null) {
                return generateToken(userId, username, roleType);
            }
            log.warn("刷新token失败：无法从旧token中解析完整用户信息");
        } catch (Exception e) {
            log.error("刷新token失败", e);
        }
        return null;
    }

    /**
     * 从token中获取当前用户ID
     *
     * @param token JWT token
     * @return 用户ID，获取失败返回null
     */
    public static Long getCurrentUserId(String token) {
        return getUserIdFromToken(token);
    }

    /**
     * 从token中获取当前用户名
     *
     * @param token JWT token
     * @return 用户名，获取失败返回null
     */
    public static String getCurrentUsername(String token) {
        return getUsernameFromToken(token);
    }

    /**
     * 从token中获取当前用户角色
     *
     * @param token JWT token
     * @return 角色代码，获取失败返回null
     */
    public static Integer getCurrentUserRole(String token) {
        return getRoleTypeFromToken(token);
    }

    /**
     * 从当前请求上下文获取JWT token
     *
     * @return JWT token，获取失败返回null
     */
    public static String getCurrentToken() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                // 优先从请求属性获取（由JwtAuthenticationFilter设置）
                String token = (String) request.getAttribute("jwtToken");
                log.debug("从请求属性获取token: {}", token != null ? "存在" : "不存在");
                if (token != null) {
                    return token;
                }
                // 备用方案：从请求头直接提取
                String headerToken = extractTokenFromRequest(request);
                log.debug("从请求头获取token: {}", headerToken != null ? "存在" : "不存在");
                return headerToken;
            } else {
                log.warn("无法获取ServletRequestAttributes");
            }
        } catch (Exception e) {
            log.error("获取当前token失败", e);
        }
        return null;
    }

    /**
     * 获取当前用户ID（从当前请求上下文）
     *
     * @return 当前用户ID，获取失败返回null
     */
    public static Long getCurrentUserId() {
        String token = getCurrentToken();
        return token != null ? getUserIdFromToken(token) : null;
    }

    /**
     * 获取当前用户名（从当前请求上下文）
     *
     * @return 当前用户名，获取失败返回null
     */
    public static String getCurrentUsername() {
        String token = getCurrentToken();
        return token != null ? getUsernameFromToken(token) : null;
    }

    /**
     * 获取当前用户角色（从当前请求上下文）
     *
     * @return 当前用户角色代码，获取失败返回null
     */
    public static Integer getCurrentUserRole() {
        String token = getCurrentToken();
        if (token == null) {
            log.warn("获取当前用户角色失败：无法获取token");
            return null;
        }
        Integer role = getRoleTypeFromToken(token);
        log.debug("获取当前用户角色: token存在={}, role={}", token != null, role);
        return role;
    }

    /**
     * 验证token的完整性（包括格式和内容）
     *
     * @param token JWT token
     * @return 验证结果，包含用户信息或null
     */
    public static TokenValidationResult validateToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return null;
            }

            DecodedJWT jwt = verifyToken(token);
            Long userId = jwt.getClaim("userId").asLong();
            String username = jwt.getClaim("username").asString();
            
            // 兼容角色类型的多种格式
            Integer roleType = null;
            try {
                roleType = jwt.getClaim("roleType").asInt();
            } catch (Exception e) {
                // 如果作为Integer解析失败，尝试作为String解析
                String roleTypeStr = jwt.getClaim("roleType").asString();
                if (StringUtils.hasText(roleTypeStr)) {
                    roleType = Integer.valueOf(roleTypeStr);
                }
            }

            if (userId != null && StringUtils.hasText(username) && roleType != null) {
                return new TokenValidationResult(userId, username, roleType, true);
            }
        } catch (Exception e) {
            log.warn("Token验证失败：{}", e.getMessage());
        }
        return null;
    }

    /**
     * Token验证结果封装类
     */
    @Getter
    public static class TokenValidationResult {
        private final Long userId;
        private final String username;
        private final Integer roleType;
        private final boolean valid;

        public TokenValidationResult(Long userId, String username, Integer roleType, boolean valid) {
            this.userId = userId;
            this.username = username;
            this.roleType = roleType;
            this.valid = valid;
        }

    }
}