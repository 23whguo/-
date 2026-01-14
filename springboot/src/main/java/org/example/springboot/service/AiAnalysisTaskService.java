package org.example.springboot.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.query.AiAnalysisTaskQueryDTO;
import org.example.springboot.DTO.response.AiAnalysisTaskResponseDTO;
import org.example.springboot.entity.AiAnalysisTask;
import org.example.springboot.entity.EmotionDiary;
import org.example.springboot.entity.User;
import org.example.springboot.enumClass.AiTaskStatus;
import org.example.springboot.enumClass.AiTaskType;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.AiAnalysisTaskMapper;
import org.example.springboot.mapper.EmotionDiaryMapper;
import org.example.springboot.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI分析任务服务
 * @author system
 */
@Slf4j
@Service
public class AiAnalysisTaskService {

    @Resource
    private AiAnalysisTaskMapper aiAnalysisTaskMapper;

    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 创建AI分析任务
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(Long diaryId, Long userId, AiTaskType taskType, Integer priority) {
        log.info("创建AI分析任务，日记ID: {}, 用户ID: {}, 任务类型: {}", diaryId, userId, taskType.getCode());

        AiAnalysisTask task = new AiAnalysisTask();
        task.setDiaryId(diaryId);
        task.setUserId(userId);
        task.setStatus(AiTaskStatus.PENDING.getCode());
        task.setTaskType(taskType.getCode());
        task.setPriority(priority != null ? priority : 2); // 默认正常优先级
        task.setRetryCount(0);
        task.setMaxRetryCount(3);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        aiAnalysisTaskMapper.insert(task);
        log.info("AI分析任务创建成功，任务ID: {}", task.getId());
        return task.getId();
    }

    /**
     * 更新任务状态为处理中
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsProcessing(Long taskId) {
        LambdaUpdateWrapper<AiAnalysisTask> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AiAnalysisTask::getId, taskId)
                    .set(AiAnalysisTask::getStatus, AiTaskStatus.PROCESSING.getCode())
                    .set(AiAnalysisTask::getStartedAt, LocalDateTime.now())
                    .set(AiAnalysisTask::getUpdatedAt, LocalDateTime.now());
        
        aiAnalysisTaskMapper.update(null, updateWrapper);
        log.debug("任务状态更新为处理中，任务ID: {}", taskId);
    }

    /**
     * 标记任务完成
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsCompleted(Long taskId) {
        LambdaUpdateWrapper<AiAnalysisTask> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AiAnalysisTask::getId, taskId)
                    .set(AiAnalysisTask::getStatus, AiTaskStatus.COMPLETED.getCode())
                    .set(AiAnalysisTask::getCompletedAt, LocalDateTime.now())
                    .set(AiAnalysisTask::getUpdatedAt, LocalDateTime.now());
        
        aiAnalysisTaskMapper.update(null, updateWrapper);
        log.debug("任务标记为完成，任务ID: {}", taskId);
    }

    /**
     * 标记任务失败
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsFailed(Long taskId, String errorMessage) {
        AiAnalysisTask task = aiAnalysisTaskMapper.selectById(taskId);
        if (task == null) {
            log.warn("任务不存在，无法标记失败，任务ID: {}", taskId);
            return;
        }

        LambdaUpdateWrapper<AiAnalysisTask> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AiAnalysisTask::getId, taskId)
                    .set(AiAnalysisTask::getStatus, AiTaskStatus.FAILED.getCode())
                    .set(AiAnalysisTask::getErrorMessage, errorMessage)
                    .set(AiAnalysisTask::getRetryCount, task.getRetryCount() + 1)
                    .set(AiAnalysisTask::getUpdatedAt, LocalDateTime.now());
        
        aiAnalysisTaskMapper.update(null, updateWrapper);
        log.warn("任务标记为失败，任务ID: {}, 错误: {}", taskId, errorMessage);
    }

    /**
     * 分页查询AI分析任务
     */
    public Page<AiAnalysisTaskResponseDTO> getTaskPage(AiAnalysisTaskQueryDTO queryDTO) {
        log.info("分页查询AI分析任务，查询条件: {}", queryDTO);

        Page<AiAnalysisTask> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        LambdaQueryWrapper<AiAnalysisTask> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StrUtil.isNotBlank(queryDTO.getStatus())) {
            queryWrapper.eq(AiAnalysisTask::getStatus, queryDTO.getStatus());
        }
        if (StrUtil.isNotBlank(queryDTO.getTaskType())) {
            queryWrapper.eq(AiAnalysisTask::getTaskType, queryDTO.getTaskType());
        }
        if (queryDTO.getUserId() != null) {
            queryWrapper.eq(AiAnalysisTask::getUserId, queryDTO.getUserId());
        }
        if (queryDTO.getPriority() != null) {
            queryWrapper.eq(AiAnalysisTask::getPriority, queryDTO.getPriority());
        }
        if (StrUtil.isNotBlank(queryDTO.getStartTime())) {
            queryWrapper.ge(AiAnalysisTask::getCreatedAt, DateUtil.parse(queryDTO.getStartTime()));
        }
        if (StrUtil.isNotBlank(queryDTO.getEndTime())) {
            queryWrapper.le(AiAnalysisTask::getCreatedAt, DateUtil.parse(queryDTO.getEndTime()));
        }
        if (Boolean.TRUE.equals(queryDTO.getFailedOnly())) {
            queryWrapper.eq(AiAnalysisTask::getStatus, AiTaskStatus.FAILED.getCode());
        }
        if (Boolean.TRUE.equals(queryDTO.getRetryableOnly())) {
            queryWrapper.eq(AiAnalysisTask::getStatus, AiTaskStatus.FAILED.getCode())
                        .apply("retry_count < max_retry_count");
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(AiAnalysisTask::getCreatedAt);

        Page<AiAnalysisTask> taskPage = aiAnalysisTaskMapper.selectPage(page, queryWrapper);
        
        // 转换为响应DTO
        return convertToResponsePage(taskPage, queryDTO.getUsername());
    }

    /**
     * 重试失败的任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void retryTask(Long taskId) {
        log.info("重试AI分析任务，任务ID: {}", taskId);

        AiAnalysisTask task = aiAnalysisTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        if (!task.canRetry()) {
            throw new BusinessException("任务不可重试：" + 
                (task.getRetryCount() >= task.getMaxRetryCount() ? "已达最大重试次数" : "任务状态不允许重试"));
        }

        // 重置任务状态
        LambdaUpdateWrapper<AiAnalysisTask> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AiAnalysisTask::getId, taskId)
                    .set(AiAnalysisTask::getStatus, AiTaskStatus.PENDING.getCode())
                    .set(AiAnalysisTask::getErrorMessage, null)
                    .set(AiAnalysisTask::getStartedAt, null)
                    .set(AiAnalysisTask::getCompletedAt, null)
                    .set(AiAnalysisTask::getUpdatedAt, LocalDateTime.now());
        
        aiAnalysisTaskMapper.update(null, updateWrapper);
        log.info("任务重试状态重置完成，任务ID: {}", taskId);
    }

    /**
     * 批量重试失败的任务
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchRetryTasks(List<Long> taskIds) {
        log.info("批量重试AI分析任务，任务数量: {}", taskIds.size());

        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> failReasons = new ArrayList<>();

        for (Long taskId : taskIds) {
            try {
                retryTask(taskId);
                successCount++;
            } catch (Exception e) {
                failCount++;
                failReasons.add("任务ID " + taskId + ": " + e.getMessage());
                log.warn("批量重试任务失败，任务ID: {}, 错误: {}", taskId, e.getMessage());
            }
        }

        result.put("totalCount", taskIds.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failReasons", failReasons);

        log.info("批量重试任务完成，总数: {}, 成功: {}, 失败: {}", taskIds.size(), successCount, failCount);
        return result;
    }

    /**
     * 获取队列统计信息
     */
    public Map<String, Object> getQueueStatistics() {
        log.info("获取AI分析队列统计信息");

        Map<String, Object> stats = new HashMap<>();

        // 按状态统计
        LambdaQueryWrapper<AiAnalysisTask> queryWrapper = new LambdaQueryWrapper<>();
        List<AiAnalysisTask> allTasks = aiAnalysisTaskMapper.selectList(queryWrapper);

        Map<String, Long> statusStats = allTasks.stream()
                .collect(Collectors.groupingBy(AiAnalysisTask::getStatus, Collectors.counting()));

        stats.put("totalTasks", allTasks.size());
        stats.put("pendingTasks", statusStats.getOrDefault(AiTaskStatus.PENDING.getCode(), 0L));
        stats.put("processingTasks", statusStats.getOrDefault(AiTaskStatus.PROCESSING.getCode(), 0L));
        stats.put("completedTasks", statusStats.getOrDefault(AiTaskStatus.COMPLETED.getCode(), 0L));
        stats.put("failedTasks", statusStats.getOrDefault(AiTaskStatus.FAILED.getCode(), 0L));

        // 可重试任务数
        long retryableTasks = allTasks.stream()
                .filter(AiAnalysisTask::canRetry)
                .count();
        stats.put("retryableTasks", retryableTasks);

        // 按类型统计
        Map<String, Long> typeStats = allTasks.stream()
                .collect(Collectors.groupingBy(AiAnalysisTask::getTaskType, Collectors.counting()));
        stats.put("taskTypeStats", typeStats);

        return stats;
    }

    /**
     * 转换为响应DTO页面
     */
    private Page<AiAnalysisTaskResponseDTO> convertToResponsePage(Page<AiAnalysisTask> taskPage, String usernameFilter) {
        Page<AiAnalysisTaskResponseDTO> responsePage = new Page<>();
        responsePage.setCurrent(taskPage.getCurrent());
        responsePage.setSize(taskPage.getSize());
        responsePage.setTotal(taskPage.getTotal());
        responsePage.setPages(taskPage.getPages());

        List<AiAnalysisTaskResponseDTO> responseList = new ArrayList<>();

        // 获取相关的用户和日记信息
        Set<Long> userIds = taskPage.getRecords().stream()
                .map(AiAnalysisTask::getUserId)
                .collect(Collectors.toSet());
        
        Set<Long> diaryIds = taskPage.getRecords().stream()
                .map(AiAnalysisTask::getDiaryId)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        }

        Map<Long, EmotionDiary> diaryMap = new HashMap<>();
        if (!diaryIds.isEmpty()) {
            List<EmotionDiary> diaries = emotionDiaryMapper.selectBatchIds(diaryIds);
            diaryMap = diaries.stream().collect(Collectors.toMap(EmotionDiary::getId, diary -> diary));
        }

        for (AiAnalysisTask task : taskPage.getRecords()) {
            User user = userMap.get(task.getUserId());
            EmotionDiary diary = diaryMap.get(task.getDiaryId());

            // 如果有用户名过滤条件，进行过滤
            if (StrUtil.isNotBlank(usernameFilter) && user != null) {
                if (!user.getUsername().contains(usernameFilter) && 
                    (user.getNickname() == null || !user.getNickname().contains(usernameFilter))) {
                    continue;
                }
            }

            AiAnalysisTaskResponseDTO responseDTO = convertToResponseDTO(task, user, diary);
            responseList.add(responseDTO);
        }

        responsePage.setRecords(responseList);
        return responsePage;
    }

    /**
     * 转换为响应DTO
     */
    private AiAnalysisTaskResponseDTO convertToResponseDTO(AiAnalysisTask task, User user, EmotionDiary diary) {
        AiAnalysisTaskResponseDTO dto = new AiAnalysisTaskResponseDTO();
        
        dto.setId(task.getId());
        dto.setDiaryId(task.getDiaryId());
        dto.setUserId(task.getUserId());
        dto.setStatus(task.getStatus());
        dto.setStatusDescription(AiTaskStatus.fromCode(task.getStatus()).getDescription());
        dto.setTaskType(task.getTaskType());
        dto.setTaskTypeDescription(AiTaskType.fromCode(task.getTaskType()).getDescription());
        dto.setPriority(task.getPriority());
        dto.setPriorityDescription(getPriorityDescription(task.getPriority()));
        dto.setRetryCount(task.getRetryCount());
        dto.setMaxRetryCount(task.getMaxRetryCount());
        dto.setErrorMessage(task.getErrorMessage());
        dto.setStartedAt(task.getStartedAt());
        dto.setCompletedAt(task.getCompletedAt());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setCanRetry(task.canRetry());

        // 计算处理耗时
        if (task.getStartedAt() != null && task.getCompletedAt() != null) {
            dto.setProcessingTimeMs(java.time.Duration.between(task.getStartedAt(), task.getCompletedAt()).toMillis());
        }

        // 用户信息
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
        }

        // 日记信息
        if (diary != null) {
            dto.setDiaryDate(diary.getDiaryDate().toString());
        }

        return dto;
    }

    /**
     * 获取优先级描述
     */
    private String getPriorityDescription(Integer priority) {
        if (priority == null) {
            return "未知";
        }
        return switch (priority) {
            case 1 -> "低";
            case 2 -> "正常";
            case 3 -> "高";
            case 4 -> "紧急";
            default -> "未知";
        };
    }
}
