package org.example.springboot.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * 
 * 核心功能：
 * 1. 配置RedisTemplate使用JSON序列化器
 * 2. 解决默认JDK序列化器的兼容性问题
 * 3. 提高缓存数据的可读性和跨语言兼容性
 * 
 * @author system
 * @date 2025-01-27
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * 
     * 序列化策略：
     * - Key: String序列化器（简单直接）
     * - Value: Jackson JSON序列化器（支持复杂对象）
     * - HashKey: String序列化器
     * - HashValue: Jackson JSON序列化器
     * 
     * @param connectionFactory Redis连接工厂
     * @return 配置好的RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 配置ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        
        // 创建Jackson序列化器（使用新的构造方法）
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // 创建String序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 设置key和value的序列化器
        template.setKeySerializer(stringRedisSerializer);           // key使用String序列化
        template.setValueSerializer(jackson2JsonRedisSerializer);   // value使用JSON序列化
        
        // 设置hash key和value的序列化器
        template.setHashKeySerializer(stringRedisSerializer);       // hash key使用String序列化
        template.setHashValueSerializer(jackson2JsonRedisSerializer); // hash value使用JSON序列化
        
        // 初始化RedisTemplate
        template.afterPropertiesSet();
        
        return template;
    }
} 