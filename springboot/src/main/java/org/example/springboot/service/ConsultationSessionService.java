package org.example.springboot.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.springboot.DTO.query.ConsultationSessionQueryDTO;
import org.example.springboot.DTO.response.ConsultationSessionResponseDTO;
import org.example.springboot.entity.ConsultationSession;
import org.example.springboot.entity.User;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.ConsultationSessionMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.service.convert.ConsultationConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 咨询会话服务类
 * @author system
 */
@Slf4j
@Service
public class ConsultationSessionService {

    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ConsultationConvert consultationConvert;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    /**
     * 创建咨询会话
     *
     * @param userId 用户ID
     * @param createDTO 创建DTO
     * @return 会话信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsultationSession createSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        log.info("创建咨询会话，用户ID: {}", userId);

        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 简化逻辑：不再检查活跃会话，允许用户创建多个会话

        // 创建会话
        ConsultationSession session = ConsultationSession.builder()
                .userId(userId)
                .sessionTitle(createDTO.getSessionTitle())
                .startedAt(LocalDateTime.now())
                .build();

        // 如果未提供标题，使用默认标题
        if (StrUtil.isBlank(session.getSessionTitle())) {
            session.setSessionTitle("小暖助手 - " + DateUtil.format(LocalDateTime.now(), "MM-dd HH:mm"));
        }

        consultationSessionMapper.insert(session);

        log.info("咨询会话创建成功，会话ID: {}", session.getId());
        return session;
    }



    /**
     * 根据ID获取会话
     *
     * @param sessionId 会话ID
     * @return 会话信息，如果不存在则返回null
     */
    public ConsultationSession getSessionById(Long sessionId) {
        return consultationSessionMapper.selectById(sessionId);
    }

    /**
     * 更新会话的最后一次情绪分析结果
     *
     * @param sessionId 会话ID
     * @param emotionAnalysisJson 情绪分析结果JSON字符串
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateLastEmotionAnalysis(Long sessionId, String emotionAnalysisJson) {
        log.info("更新会话情绪分析，会话ID: {}, JSON数据: {}", sessionId, emotionAnalysisJson);

        LambdaUpdateWrapper<ConsultationSession> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConsultationSession::getId, sessionId)
                    .set(ConsultationSession::getLastEmotionAnalysis, emotionAnalysisJson)
                    .set(ConsultationSession::getLastEmotionUpdatedAt, LocalDateTime.now());

        consultationSessionMapper.update(null, updateWrapper);
        log.info("会话情绪分析更新成功，会话ID: {}", sessionId);
    }

    /**
     * 分页查询咨询会话
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    public Page<ConsultationSessionResponseDTO> selectPage(ConsultationSessionQueryDTO queryDTO) {
        log.info("分页查询咨询会话，查询条件: {}", queryDTO);

        // 构建查询条件
        LambdaQueryWrapper<ConsultationSession> queryWrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getUserId() != null) {
            queryWrapper.eq(ConsultationSession::getUserId, queryDTO.getUserId());
        }
        
        
        if (StrUtil.isNotBlank(queryDTO.getStartDate())) {
            queryWrapper.ge(ConsultationSession::getStartedAt, queryDTO.getStartDate() + " 00:00:00");
        }
        
        if (StrUtil.isNotBlank(queryDTO.getEndDate())) {
            queryWrapper.le(ConsultationSession::getStartedAt, queryDTO.getEndDate() + " 23:59:59");
        }
        
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            queryWrapper.like(ConsultationSession::getSessionTitle, queryDTO.getKeyword());
        }

        // 排序：最新创建的在前
        queryWrapper.orderByDesc(ConsultationSession::getStartedAt);

        // 分页查询
        Page<ConsultationSession> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getSize());
        Page<ConsultationSession> sessionPage = consultationSessionMapper.selectPage(page, queryWrapper);

        // 转换为响应DTO
        Page<ConsultationSessionResponseDTO> resultPage = new Page<>();
        resultPage.setCurrent(sessionPage.getCurrent());
        resultPage.setSize(sessionPage.getSize());
        resultPage.setTotal(sessionPage.getTotal());
        resultPage.setPages(sessionPage.getPages());

        List<ConsultationSessionResponseDTO> responseDTOList = sessionPage.getRecords().stream()
                .map(this::convertToResponseDTO)
                .toList();
        
        resultPage.setRecords(responseDTOList);

        log.info("分页查询咨询会话完成，返回{}条记录", responseDTOList.size());
        return resultPage;
    }

    /**
     * 根据ID获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话详情
     */
    public ConsultationSessionResponseDTO getSessionDetail(Long sessionId) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }

        return convertToResponseDTO(session);
    }

    /**
     * 删除咨询会话
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSession(Long sessionId, Long userId) {
        log.info("删除咨询会话，会话ID: {}, 用户ID: {}", sessionId, userId);

        // 查找会话
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }

        // 验证会话所有者
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此会话");
        }

        try {
            // 级联删除相关消息
            consultationMessageService.deleteMessagesBySessionId(sessionId);
            
            // 删除会话记录
            int deletedRows = consultationSessionMapper.deleteById(sessionId);
            
            if (deletedRows > 0) {
                log.info("咨询会话删除成功，会话ID: {}", sessionId);
                return true;
            } else {
                log.warn("咨询会话删除失败，会话ID: {}", sessionId);
                return false;
            }
        } catch (Exception e) {
            log.error("删除咨询会话异常，会话ID: {}, 错误: {}", sessionId, e.getMessage(), e);
            throw new BusinessException("删除会话失败: " + e.getMessage());
        }
    }

    /**
     * 更新会话标题
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param newTitle 新标题
     * @return 是否更新成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSessionTitle(Long sessionId, Long userId, String newTitle) {
        log.info("更新会话标题，会话ID: {}, 用户ID: {}, 新标题: {}", sessionId, userId, newTitle);

        // 查找会话
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }

        // 验证会话所有者
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此会话");
        }

        try {
            // 如果标题为空或只有空白字符，生成默认标题
            String finalTitle = newTitle;
            if (StrUtil.isBlank(finalTitle)) {
                finalTitle = "小暖助手 - " + DateUtil.format(session.getStartedAt(), "MM-dd HH:mm");
            }

            // 更新会话标题
            LambdaUpdateWrapper<ConsultationSession> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ConsultationSession::getId, sessionId)
                        .set(ConsultationSession::getSessionTitle, finalTitle);

            int updated = consultationSessionMapper.update(null, updateWrapper);
            
            if (updated > 0) {
                log.info("会话标题更新成功，会话ID: {}, 新标题: {}", sessionId, finalTitle);
                return true;
            } else {
                log.warn("会话标题更新失败，无记录被更新，会话ID: {}", sessionId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("更新会话标题失败，会话ID: {}, 错误: {}", sessionId, e.getMessage(), e);
            throw new BusinessException("更新标题失败: " + e.getMessage());
        }
    }

    /**
     * 转换为响应DTO
     */
    private ConsultationSessionResponseDTO convertToResponseDTO(ConsultationSession session) {
        ConsultationSessionResponseDTO responseDTO = consultationConvert.toResponseDTO(session);

        // 查询用户信息
        User user = userMapper.selectById(session.getUserId());
        if (user != null) {
            responseDTO.setUserNickname(user.getNickname());
            responseDTO.setUserAvatar(user.getAvatar());
        }

        // 设置描述字段 - 简化后不再需要

        // 计算持续时间
        responseDTO.setDurationMinutes(session.getDurationMinutes());

        // 简化后不再计算情绪改善度

        // 获取消息统计信息
        enrichWithMessageInfo(responseDTO, session.getId());

        return responseDTO;
    }


    /**
     * 丰富消息信息
     */
    private void enrichWithMessageInfo(ConsultationSessionResponseDTO responseDTO, Long sessionId) {
        try {
            // 获取消息统计
            responseDTO.setMessageCount(consultationMessageService.getMessageCountBySessionId(sessionId));
            
            // 获取最后一条消息
            var lastMessage = consultationMessageService.getLastMessageBySessionId(sessionId);
            if (lastMessage != null) {
                responseDTO.setLastMessageContent(lastMessage.getContentPreview());
                responseDTO.setLastMessageTime(lastMessage.getCreatedAt());
            }

            // 获取情绪标签
            responseDTO.setEmotionTags(consultationMessageService.getEmotionTagsBySessionId(sessionId));
            
            // 设置主要情绪
            if (responseDTO.getEmotionTags() != null && !responseDTO.getEmotionTags().isEmpty()) {
                responseDTO.setPrimaryEmotion(responseDTO.getEmotionTags().get(0));
            }
        } catch (Exception e) {
            log.warn("丰富消息信息失败，会话ID: {}, 错误: {}", sessionId, e.getMessage());
        }
    }
}
