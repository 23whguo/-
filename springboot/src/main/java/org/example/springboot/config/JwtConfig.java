package org.example.springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置类
 * 
 * @author system
 * @date 2025-01-13
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    /**
     * JWT密钥
     */
    private String secret;
    
    /**
     * Token过期时间（毫秒）
     */
    private long expiration;
    
    /**
     * 刷新Token过期时间（毫秒）
     */
    private long refreshExpiration;
    
    /**
     * Token头部名称
     */
    private String header = "Authorization";
    
    /**
     * Token前缀
     */
    private String tokenPrefix = "Bearer ";
}
