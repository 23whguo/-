package org.example.springboot.util;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 */
@Component
@Slf4j
public class RedisUtils {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 设置缓存
     * 
     * @param key 键
     * @param value 值
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis设置缓存失败，key: {}, error: {}", key, e.getMessage(), e);
        }
    }
    
    /**
     * 设置缓存并指定过期时间
     * 
     * @param key 键
     * @param value 值
     * @param timeout 过期时间（秒）
     */
    public void set(String key, Object value, long timeout) {
        try {
            if (timeout > 0) {
                redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
        } catch (Exception e) {
            log.error("Redis设置缓存失败，key: {}, timeout: {}, error: {}", key, timeout, e.getMessage(), e);
        }
    }
    
    /**
     * 获取缓存
     * 
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        try {
            return key == null ? null : redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis获取缓存失败，key: {}, error: {}", key, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 获取缓存并转换为指定类型
     * 
     * @param key 键
     * @param clazz 目标类型
     * @param <T> 泛型
     * @return 转换后的值
     */
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = get(key);
            if (value == null) {
                return null;
            }
            
            if (clazz.isInstance(value)) {
                return clazz.cast(value);
            }
            
            // 尝试JSON转换
            String jsonStr = value.toString();
            return JSONUtil.toBean(jsonStr, clazz);
            
        } catch (Exception e) {
            log.error("Redis获取缓存并转换类型失败，key: {}, class: {}, error: {}", key, clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 删除缓存
     * 
     * @param key 键
     * @return 是否成功
     */
    public boolean delete(String key) {
        try {
            if (key != null) {
                return Boolean.TRUE.equals(redisTemplate.delete(key));
            }
            return false;
        } catch (Exception e) {
            log.error("Redis删除缓存失败，key: {}, error: {}", key, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 判断键是否存在
     * 
     * @param key 键
     * @return 是否存在
     */
    public boolean exists(String key) {
        try {
            return key != null && Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis判断键存在失败，key: {}, error: {}", key, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 设置过期时间
     * 
     * @param key 键
     * @param timeout 过期时间（秒）
     * @return 是否成功
     */
    public boolean expire(String key, long timeout) {
        try {
            if (timeout > 0) {
                return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, TimeUnit.SECONDS));
            }
            return false;
        } catch (Exception e) {
            log.error("Redis设置过期时间失败，key: {}, timeout: {}, error: {}", key, timeout, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取过期时间
     * 
     * @param key 键
     * @return 过期时间（秒），-1表示永不过期，-2表示键不存在
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            log.error("Redis获取过期时间失败，key: {}, error: {}", key, e.getMessage(), e);
            return -2;
        }
    }
    
    /**
     * 递增
     * 
     * @param key 键
     * @param delta 递增值
     * @return 递增后的值
     */
    public long increment(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis递增失败，key: {}, delta: {}, error: {}", key, delta, e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 递减
     * 
     * @param key 键
     * @param delta 递减值
     * @return 递减后的值
     */
    public long decrement(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().decrement(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis递减失败，key: {}, delta: {}, error: {}", key, delta, e.getMessage(), e);
            return 0;
        }
    }
} 