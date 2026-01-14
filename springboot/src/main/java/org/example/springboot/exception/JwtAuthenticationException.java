package org.example.springboot.exception;

/**
 * JWT认证异常类
 * 
 * 用于处理JWT认证过程中的各种异常情况：
 * - Token缺失
 * - Token格式错误
 * - Token过期
 * - Token验证失败
 * - 用户信息不存在
 */
public class JwtAuthenticationException extends RuntimeException {
    
    private final int errorCode;
    
    public JwtAuthenticationException(String message) {
        super(message);
        this.errorCode = 401;
    }
    
    public JwtAuthenticationException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 401;
    }
    
    public JwtAuthenticationException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
} 