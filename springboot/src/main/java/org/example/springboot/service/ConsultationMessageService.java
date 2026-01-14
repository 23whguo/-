package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.response.ConsultationMessageResponseDTO;
import org.example.springboot.entity.ConsultationMessage;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.ConsultationMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 咨询消息服务类
 * @author system
 */
@Slf4j
@Service
public class ConsultationMessageService {

    @Resource
    private ConsultationMessageMapper consultationMessageMapper;


    /**
     * 保存用户消息
     *
     * @param sessionId 会话ID
     * @param content 消息内容
     * @param emotionTag 情绪标签
     * @return 消息实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsultationMessage saveUserMessage(Long sessionId, String content, String emotionTag) {
        log.info("保存用户消息，会话ID: {}", sessionId);

        ConsultationMessage message = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(1) // 用户
                .messageType(1) // 文本
                .content(content)
                .emotionTag(emotionTag)
                .createdAt(LocalDateTime.now())
                .build();

        consultationMessageMapper.insert(message);
        log.info("用户消息保存成功，消息ID: {}", message.getId());
        return message;
    }

    /**
     * 保存AI助手消息
     *
     * @param sessionId 会话ID
     * @param content 消息内容
     * @param aiModel AI模型名称
     * @return 消息实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsultationMessage saveAiMessage(Long sessionId, String content, String aiModel) {
        log.info("保存AI助手消息，会话ID: {}", sessionId);

        ConsultationMessage message = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(2) // AI助手
                .messageType(1) // 文本
                .content(content)
                .aiModel(aiModel)
                .createdAt(LocalDateTime.now())
                .build();

        consultationMessageMapper.insert(message);
        log.info("AI助手消息保存成功，消息ID: {}", message.getId());
        return message;
    }

    /**
     * 获取会话的所有消息
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    public List<ConsultationMessageResponseDTO> getMessagesBySessionId(Long sessionId) {
        log.info("获取会话消息，会话ID: {}", sessionId);

        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId)
                    .orderByAsc(ConsultationMessage::getCreatedAt);

        List<ConsultationMessage> messages = consultationMessageMapper.selectList(queryWrapper);
        
        List<ConsultationMessageResponseDTO> responseDTOList = messages.stream()
                .map(this::convertToResponseDTO)
                .toList();

        log.info("获取会话消息完成，会话ID: {}, 消息数量: {}", sessionId, responseDTOList.size());
        return responseDTOList;
    }

    /**
     * 获取会话的消息数量
     *
     * @param sessionId 会话ID
     * @return 消息数量
     */
    public Integer getMessageCountBySessionId(Long sessionId) {
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId);
        
        Long count = consultationMessageMapper.selectCount(queryWrapper);
        return count.intValue();
    }

    /**
     * 获取会话的最后一条消息
     *
     * @param sessionId 会话ID
     * @return 最后一条消息
     */
    public ConsultationMessageResponseDTO getLastMessageBySessionId(Long sessionId) {
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId)
                    .orderByDesc(ConsultationMessage::getCreatedAt)
                    .last("LIMIT 1");

        ConsultationMessage message = consultationMessageMapper.selectOne(queryWrapper);
        return message != null ? convertToResponseDTO(message) : null;
    }

    /**
     * 获取会话的所有情绪标签
     *
     * @param sessionId 会话ID
     * @return 情绪标签列表
     */
    public List<String> getEmotionTagsBySessionId(Long sessionId) {
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId)
                    .isNotNull(ConsultationMessage::getEmotionTag)
                    .ne(ConsultationMessage::getEmotionTag, "");

        List<ConsultationMessage> messages = consultationMessageMapper.selectList(queryWrapper);
        
        return messages.stream()
                .map(ConsultationMessage::getEmotionTag)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 根据关键词搜索消息
     *
     * @param keyword 关键词
     * @return 包含关键词的会话ID列表
     */
    public List<Long> searchSessionIdsByKeyword(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return List.of();
        }

        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ConsultationMessage::getContent, keyword)
                    .select(ConsultationMessage::getSessionId);

        List<ConsultationMessage> messages = consultationMessageMapper.selectList(queryWrapper);
        
        return messages.stream()
                .map(ConsultationMessage::getSessionId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 删除会话的所有消息
     *
     * @param sessionId 会话ID
     * @return 删除的消息数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteMessagesBySessionId(Long sessionId) {
        log.info("删除会话相关消息，会话ID: {}", sessionId);
        
        if (sessionId == null) {
            throw new BusinessException("会话ID不能为空");
        }
        
        try {
            LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ConsultationMessage::getSessionId, sessionId);
            
            // 先查询要删除的消息数量，用于日志记录
            Long messageCount = consultationMessageMapper.selectCount(queryWrapper);
            
            // 执行删除操作
            int deletedRows = consultationMessageMapper.delete(queryWrapper);
            
            log.info("删除会话消息完成，会话ID: {}, 预期删除: {}, 实际删除: {}", 
                    sessionId, messageCount, deletedRows);
            
            return deletedRows;
        } catch (Exception e) {
            log.error("删除会话消息失败，会话ID: {}, 错误: {}", sessionId, e.getMessage(), e);
            throw new BusinessException("删除会话消息失败: " + e.getMessage());
        }
    }

    /**
     * 转换为响应DTO
     * 严格遵循DTO转换规范：禁止使用BeanUtil.copyProperties()，采用手动字段赋值
     */
    private ConsultationMessageResponseDTO convertToResponseDTO(ConsultationMessage message) {
        if (message == null) {
            return null;
        }
        
        // 手动逐字段赋值，确保转换的准确性和可控性
        ConsultationMessageResponseDTO responseDTO = new ConsultationMessageResponseDTO();
        responseDTO.setId(message.getId());
        responseDTO.setSessionId(message.getSessionId());
        responseDTO.setSenderType(message.getSenderType());
        responseDTO.setMessageType(message.getMessageType());
        responseDTO.setContent(message.getContent());
        responseDTO.setEmotionTag(message.getEmotionTag());
        responseDTO.setAiModel(message.getAiModel());
        responseDTO.setCreatedAt(message.getCreatedAt());
        
        // 设置描述字段（通过实体方法获取）
        responseDTO.setSenderTypeDesc(message.getSenderTypeDesc());
        responseDTO.setMessageTypeDesc(message.getMessageTypeDesc());
        
        // 计算消息长度
        responseDTO.calculateContentLength();
        
        return responseDTO;
    }
}
