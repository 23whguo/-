package org.example.springboot.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.command.EmotionDiaryCreateDTO;
import org.example.springboot.DTO.command.EmotionDiaryUpdateDTO;
import org.example.springboot.DTO.query.EmotionDiaryQueryDTO;
import org.example.springboot.DTO.response.EmotionDiaryResponseDTO;
import org.example.springboot.DTO.response.EmotionDiaryStatisticsDTO;
import org.example.springboot.entity.EmotionDiary;
import org.example.springboot.entity.User;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.EmotionDiaryMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.service.convert.EmotionDiaryConvert;
import org.example.springboot.AiService.StructOutPut;
import org.example.springboot.AiService.PsychologicalSupportService;
import org.example.springboot.enumClass.AiTaskType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 情绪日记业务逻辑层
 * @author system
 */
@Slf4j
@Service
public class EmotionDiaryService {

    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private PsychologicalSupportService psychologicalSupportService;
    
    @Resource
    private AiAnalysisTaskService aiAnalysisTaskService;

    /**
     * 创建或更新情绪日记
     * 同一用户同一天只能有一条记录，如果已存在则更新
     * 
     * @param userId 用户ID
     * @param createDTO 创建DTO
     * @param isEditMode 是否为编辑模式（兼容参数，暂未使用）
     * @return 情绪日记响应DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public EmotionDiaryResponseDTO createOrUpdateDiary(Long userId, EmotionDiaryCreateDTO createDTO, Boolean isEditMode) {
        log.info("创建或更新情绪日记，用户ID: {}, 日期: {}", userId, createDTO.getDiaryDate());

        // 检查同一天是否已有记录
        EmotionDiary existingDiary = getByUserIdAndDate(userId, createDTO.getDiaryDate());
        
        EmotionDiary diary;
        if (existingDiary != null) {
            // 更新现有记录
            log.info("更新现有日记记录，日记ID: {}", existingDiary.getId());
            diary = updateExistingDiary(existingDiary, createDTO);
        } else {
            // 创建新记录
            log.info("创建新的日记记录");
            diary = EmotionDiaryConvert.createCommandToEntity(createDTO, userId);
            emotionDiaryMapper.insert(diary);
        }

        // 异步触发AI情绪分析
        if (diary.getDiaryContent() != null && !diary.getDiaryContent().trim().isEmpty()) {
            performAiEmotionAnalysisAsync(diary.getId(), diary.getDiaryContent());
            log.info("已提交AI情绪分析任务到队列，日记ID: {}", diary.getId());
        }

        return EmotionDiaryConvert.entityToResponse(diary);
    }

    /**
     * 更新情绪日记
     * 
     * @param userId 用户ID
     * @param updateDTO 更新DTO
     * @return 情绪日记响应DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public EmotionDiaryResponseDTO updateDiary(Long userId, EmotionDiaryUpdateDTO updateDTO) {
        log.info("更新情绪日记，用户ID: {}, 日记ID: {}", userId, updateDTO.getId());

        EmotionDiary existingDiary = emotionDiaryMapper.selectById(updateDTO.getId());
        if (existingDiary == null) {
            throw new BusinessException("日记记录不存在");
        }

        if (!existingDiary.getUserId().equals(userId)) {
            throw new BusinessException("无权限修改此日记");
        }

        // 只更新非空字段
        LambdaUpdateWrapper<EmotionDiary> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(EmotionDiary::getId, updateDTO.getId());

        if (updateDTO.getMoodScore() != null) {
            updateWrapper.set(EmotionDiary::getMoodScore, updateDTO.getMoodScore());
        }
        if (StrUtil.isNotBlank(updateDTO.getDominantEmotion())) {
            updateWrapper.set(EmotionDiary::getDominantEmotion, updateDTO.getDominantEmotion());
        }
        if (updateDTO.getEmotionTriggers() != null) {
            updateWrapper.set(EmotionDiary::getEmotionTriggers, updateDTO.getEmotionTriggers());
        }
        if (updateDTO.getDiaryContent() != null) {
            updateWrapper.set(EmotionDiary::getDiaryContent, updateDTO.getDiaryContent());
        }
        if (updateDTO.getSleepQuality() != null) {
            updateWrapper.set(EmotionDiary::getSleepQuality, updateDTO.getSleepQuality());
        }
        if (updateDTO.getStressLevel() != null) {
            updateWrapper.set(EmotionDiary::getStressLevel, updateDTO.getStressLevel());
        }

        // 重置AI分析状态 - 每次更新都清空AI分析结果，需要重新分析
        updateWrapper.set(EmotionDiary::getAiEmotionAnalysis, null);
        updateWrapper.set(EmotionDiary::getAiAnalysisUpdatedAt, null);
        
        log.info("更新情绪日记并重置AI分析状态，日记ID: {}", updateDTO.getId());

        emotionDiaryMapper.update(null, updateWrapper);
        
        // 重新查询更新后的记录
        EmotionDiary updatedDiary = emotionDiaryMapper.selectById(updateDTO.getId());
        
        // 异步触发AI情绪分析（更新后重新分析）
        if (updatedDiary.getDiaryContent() != null && !updatedDiary.getDiaryContent().trim().isEmpty()) {
            performAiEmotionAnalysisAsync(updatedDiary.getId(), updatedDiary.getDiaryContent());
            log.info("已提交更新后的AI情绪分析任务到队列，日记ID: {}", updatedDiary.getId());
        }
        
        return EmotionDiaryConvert.entityToResponse(updatedDiary);
    }

    /**
     * 根据ID获取情绪日记
     * 
     * @param userId 用户ID
     * @param diaryId 日记ID
     * @return 情绪日记响应DTO
     */
    public EmotionDiaryResponseDTO getDiaryById(Long userId, Long diaryId) {
        log.info("获取情绪日记，用户ID: {}, 日记ID: {}", userId, diaryId);

        EmotionDiary diary = emotionDiaryMapper.selectById(diaryId);
        if (diary == null) {
            throw new BusinessException("日记记录不存在");
        }

        if (!diary.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问此日记");
        }

        return EmotionDiaryConvert.entityToResponse(diary);
    }

    /**
     * 根据用户ID和日期获取情绪日记
     * 
     * @param userId 用户ID
     * @param date 日期
     * @return 情绪日记响应DTO，如果不存在返回null
     */
    public EmotionDiaryResponseDTO getDiaryByDate(Long userId, LocalDate date) {
        log.info("根据日期获取情绪日记，用户ID: {}, 日期: {}", userId, date);

        EmotionDiary diary = getByUserIdAndDate(userId, date);
        if (diary == null) {
            return null;
        }

        return EmotionDiaryConvert.entityToResponse(diary);
    }

    /**
     * 分页查询情绪日记
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    public Page<EmotionDiaryResponseDTO> selectPage(EmotionDiaryQueryDTO queryDTO) {
        log.info("分页查询情绪日记，查询条件: {}", queryDTO);

        LambdaQueryWrapper<EmotionDiary> queryWrapper = buildQueryWrapper(queryDTO);
        queryWrapper.orderByDesc(EmotionDiary::getDiaryDate);

        Page<EmotionDiary> emotionDiaryPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        emotionDiaryPage = emotionDiaryMapper.selectPage(emotionDiaryPage, queryWrapper);

        // 转换为响应DTO
        Page<EmotionDiaryResponseDTO> responsePage = new Page<>();
        responsePage.setCurrent(emotionDiaryPage.getCurrent());
        responsePage.setSize(emotionDiaryPage.getSize());
        responsePage.setTotal(emotionDiaryPage.getTotal());
        responsePage.setPages(emotionDiaryPage.getPages());

        List<EmotionDiaryResponseDTO> responseList = EmotionDiaryConvert.entityListToResponseList(emotionDiaryPage.getRecords());
        responsePage.setRecords(responseList);

        return responsePage;
    }

    /**
     * 删除情绪日记
     * 
     * @param userId 用户ID
     * @param diaryId 日记ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDiary(Long userId, Long diaryId) {
        log.info("删除情绪日记，用户ID: {}, 日记ID: {}", userId, diaryId);

        EmotionDiary diary = emotionDiaryMapper.selectById(diaryId);
        if (diary == null) {
            throw new BusinessException("日记记录不存在");
        }

        if (!diary.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除此日记");
        }

        emotionDiaryMapper.deleteById(diaryId);
        log.info("情绪日记删除成功，日记ID: {}", diaryId);
    }

    /**
     * 获取情绪日记统计数据
     * 
     * @param userId 用户ID
     * @param days 统计天数（默认7天）
     * @return 统计数据
     */
    public EmotionDiaryStatisticsDTO getStatistics(Long userId, Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        
        log.info("获取情绪日记统计数据，用户ID: {}, 统计天数: {}", userId, days);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        LambdaQueryWrapper<EmotionDiary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EmotionDiary::getUserId, userId)
                   .between(EmotionDiary::getDiaryDate, startDate, endDate)
                   .orderByAsc(EmotionDiary::getDiaryDate);

        List<EmotionDiary> diaries = emotionDiaryMapper.selectList(queryWrapper);

        return calculateStatistics(diaries, days);
    }

    /**
     * 获取AI情绪分析结果
     * 
     * @param diaryId 日记ID
     * @return AI情绪分析结果
     */
    public StructOutPut.EmotionAnalysisResult getAiEmotionAnalysis(Long diaryId) {
        log.info("获取AI情绪分析结果，日记ID: {}", diaryId);

        EmotionDiary diary = emotionDiaryMapper.selectById(diaryId);
        if (diary == null) {
            throw new BusinessException("日记记录不存在");
        }

        String aiAnalysisJson = diary.getAiEmotionAnalysis();
        if (StrUtil.isBlank(aiAnalysisJson)) {
            log.info("日记ID: {} 的AI分析结果为空", diaryId);
            return null;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(aiAnalysisJson, StructOutPut.EmotionAnalysisResult.class);
        } catch (Exception e) {
            log.error("解析AI情绪分析结果失败，日记ID: {}, JSON: {}, 错误: {}", diaryId, aiAnalysisJson, e.getMessage());
            throw new BusinessException("AI分析结果解析失败");
        }
    }

    /**
     * 异步执行AI情绪分析并更新到数据库（带队列管理）
     * 
     * @param diaryId 日记ID
     * @param diaryContent 日记内容
     * @param taskType 任务类型
     * @param priority 优先级
     */
    @Async
    public CompletableFuture<Void> performAiEmotionAnalysisAsync(Long diaryId, String diaryContent, AiTaskType taskType, Integer priority) {
        return CompletableFuture.runAsync(() -> {
            // 获取日记信息
            EmotionDiary diary = emotionDiaryMapper.selectById(diaryId);
            if (diary == null) {
                log.warn("日记不存在，跳过AI分析，日记ID: {}", diaryId);
                return;
            }

            // 创建任务记录
            Long taskId = aiAnalysisTaskService.createTask(diaryId, diary.getUserId(), taskType, priority);
            
            try {
                log.info("开始异步AI情绪分析，日记ID: {}, 任务ID: {}", diaryId, taskId);
                
                // 标记任务为处理中
                aiAnalysisTaskService.markAsProcessing(taskId);
                
                // 设置分析开始状态
                LambdaUpdateWrapper<EmotionDiary> startWrapper = new LambdaUpdateWrapper<>();
                startWrapper.eq(EmotionDiary::getId, diaryId)
                           .set(EmotionDiary::getAiAnalysisUpdatedAt, java.time.LocalDateTime.now());
                emotionDiaryMapper.update(null, startWrapper);
                
                // 构建完整的分析内容
                StringBuilder analysisContent = new StringBuilder();
                analysisContent.append("情绪评分: ").append(diary.getMoodScore()).append("/10\n");
                if (diary.getDominantEmotion() != null) {
                    analysisContent.append("主要情绪: ").append(diary.getDominantEmotion()).append("\n");
                }
                if (diary.getEmotionTriggers() != null) {
                    analysisContent.append("情绪触发因素: ").append(diary.getEmotionTriggers()).append("\n");
                }
                if (diary.getSleepQuality() != null) {
                    analysisContent.append("睡眠质量: ").append(diary.getSleepQuality()).append("/5\n");
                }
                if (diary.getStressLevel() != null) {
                    analysisContent.append("压力水平: ").append(diary.getStressLevel()).append("/5\n");
                }
                analysisContent.append("日记内容: ").append(diaryContent);
                
                // 调用AI分析服务
                StructOutPut.EmotionAnalysisResult analysisResult = 
                    psychologicalSupportService.analyzeUserEmotion(analysisContent.toString());
                
                if (analysisResult != null) {
                    // 将分析结果转换为JSON并保存到数据库
                    ObjectMapper objectMapper = new ObjectMapper();
                    String analysisJson = objectMapper.writeValueAsString(analysisResult);
                    
                    // 更新数据库中的AI分析结果
                    LambdaUpdateWrapper<EmotionDiary> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(EmotionDiary::getId, diaryId)
                                .set(EmotionDiary::getAiEmotionAnalysis, analysisJson)
                                .set(EmotionDiary::getAiAnalysisUpdatedAt, java.time.LocalDateTime.now());
                    
                    emotionDiaryMapper.update(null, updateWrapper);
                    
                    // 标记任务完成
                    aiAnalysisTaskService.markAsCompleted(taskId);
                    
                    log.info("AI情绪分析完成并已保存，日记ID: {}, 任务ID: {}, 主要情绪: {}, 风险等级: {}", 
                            diaryId, taskId, analysisResult.primaryEmotion(), analysisResult.riskLevel());
                } else {
                    // 标记任务失败
                    aiAnalysisTaskService.markAsFailed(taskId, "AI分析服务返回null");
                    log.warn("AI情绪分析返回null，日记ID: {}, 任务ID: {}", diaryId, taskId);
                }
                
            } catch (Exception e) {
                // 标记任务失败
                aiAnalysisTaskService.markAsFailed(taskId, e.getMessage());
                log.error("异步AI情绪分析失败，日记ID: {}, 任务ID: {}, 错误: {}", diaryId, taskId, e.getMessage(), e);
            }
        });
    }

    /**
     * 兼容旧版本的方法
     */
    @Deprecated
    public CompletableFuture<Void> performAiEmotionAnalysisAsync(Long diaryId, String diaryContent) {
        return performAiEmotionAnalysisAsync(diaryId, diaryContent, AiTaskType.AUTO, 2);
    }

    /**
     * 根据用户ID和日期获取日记实体
     */
    private EmotionDiary getByUserIdAndDate(Long userId, LocalDate date) {
        LambdaQueryWrapper<EmotionDiary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EmotionDiary::getUserId, userId)
                   .eq(EmotionDiary::getDiaryDate, date);
        
        return emotionDiaryMapper.selectOne(queryWrapper);
    }

    /**
     * 更新现有日记记录
     */
    private EmotionDiary updateExistingDiary(EmotionDiary existingDiary, EmotionDiaryCreateDTO createDTO) {
        existingDiary.setMoodScore(createDTO.getMoodScore());
        existingDiary.setDominantEmotion(createDTO.getDominantEmotion());
        existingDiary.setEmotionTriggers(createDTO.getEmotionTriggers());
        existingDiary.setDiaryContent(createDTO.getDiaryContent());
        existingDiary.setSleepQuality(createDTO.getSleepQuality());
        existingDiary.setStressLevel(createDTO.getStressLevel());
        
        // 重置AI分析状态 - 内容更新后需要重新分析
        existingDiary.setAiEmotionAnalysis(null);
        existingDiary.setAiAnalysisUpdatedAt(null);
        
        log.info("更新现有日记并重置AI分析状态，日记ID: {}", existingDiary.getId());
        
        emotionDiaryMapper.updateById(existingDiary);
        
        // 异步触发AI情绪分析（更新后重新分析）
        if (existingDiary.getDiaryContent() != null && !existingDiary.getDiaryContent().trim().isEmpty()) {
            performAiEmotionAnalysisAsync(existingDiary.getId(), existingDiary.getDiaryContent());
            log.info("已提交更新现有日记的AI情绪分析任务到队列，日记ID: {}", existingDiary.getId());
        }
        
        return existingDiary;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<EmotionDiary> buildQueryWrapper(EmotionDiaryQueryDTO queryDTO) {
        LambdaQueryWrapper<EmotionDiary> queryWrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getUserId() != null) {
            queryWrapper.eq(EmotionDiary::getUserId, queryDTO.getUserId());
        }
        if (queryDTO.getStartDate() != null) {
            queryWrapper.ge(EmotionDiary::getDiaryDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            queryWrapper.le(EmotionDiary::getDiaryDate, queryDTO.getEndDate());
        }
        if (queryDTO.getMinMoodScore() != null) {
            queryWrapper.ge(EmotionDiary::getMoodScore, queryDTO.getMinMoodScore());
        }
        if (queryDTO.getMaxMoodScore() != null) {
            queryWrapper.le(EmotionDiary::getMoodScore, queryDTO.getMaxMoodScore());
        }
        if (StrUtil.isNotBlank(queryDTO.getDominantEmotion())) {
            queryWrapper.eq(EmotionDiary::getDominantEmotion, queryDTO.getDominantEmotion());
        }
        if (queryDTO.getSleepQuality() != null) {
            queryWrapper.eq(EmotionDiary::getSleepQuality, queryDTO.getSleepQuality());
        }
        if (queryDTO.getStressLevel() != null) {
            queryWrapper.eq(EmotionDiary::getStressLevel, queryDTO.getStressLevel());
        }

        return queryWrapper;
    }

    /**
     * 计算统计数据
     */
    private EmotionDiaryStatisticsDTO calculateStatistics(List<EmotionDiary> diaries, Integer totalDays) {
        EmotionDiaryStatisticsDTO statistics = new EmotionDiaryStatisticsDTO();
        
        statistics.setTotalDays(totalDays);
        statistics.setRecordedDays(diaries.size());
        
        if (diaries.isEmpty()) {
            setDefaultStatistics(statistics);
            return statistics;
        }

        // 计算完成率
        BigDecimal completionRate = BigDecimal.valueOf(diaries.size())
                .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        statistics.setCompletionRate(completionRate);

        // 计算情绪评分统计
        calculateMoodStatistics(statistics, diaries);
        
        // 计算生活指标统计
        calculateLifeIndicatorStatistics(statistics, diaries);
        
        // 计算情绪分布
        calculateEmotionDistribution(statistics, diaries);
        
        // 生成趋势数据
        generateMoodTrend(statistics, diaries);
        
        // 生成建议
        generateSuggestions(statistics, diaries);

        return statistics;
    }

    /**
     * 设置默认统计数据
     */
    private void setDefaultStatistics(EmotionDiaryStatisticsDTO statistics) {
        statistics.setCompletionRate(BigDecimal.ZERO);
        statistics.setAverageMoodScore(BigDecimal.ZERO);
        statistics.setPositiveDays(0);
        statistics.setNegativeDays(0);
        statistics.setNeutralDays(0);
        statistics.setMoodTrend(new ArrayList<>());
        statistics.setEmotionDistribution(new HashMap<>());
        statistics.setSuggestions(List.of("建议开始记录情绪日记，培养情绪觉察能力"));
    }

    /**
     * 计算情绪评分统计
     */
    private void calculateMoodStatistics(EmotionDiaryStatisticsDTO statistics, List<EmotionDiary> diaries) {
        List<Integer> moodScores = diaries.stream()
                .map(EmotionDiary::getMoodScore)
                .filter(Objects::nonNull)
                .toList();

        if (!moodScores.isEmpty()) {
            double average = moodScores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            statistics.setAverageMoodScore(BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP));
            statistics.setMaxMoodScore(moodScores.stream().max(Integer::compareTo).orElse(0));
            statistics.setMinMoodScore(moodScores.stream().min(Integer::compareTo).orElse(0));

            long positiveDays = diaries.stream().filter(EmotionDiary::isPositiveMood).count();
            long negativeDays = diaries.stream().filter(EmotionDiary::isNegativeMood).count();
            long neutralDays = diaries.size() - positiveDays - negativeDays;

            statistics.setPositiveDays((int) positiveDays);
            statistics.setNegativeDays((int) negativeDays);
            statistics.setNeutralDays((int) neutralDays);
        }
    }

    /**
     * 计算生活指标统计
     */
    private void calculateLifeIndicatorStatistics(EmotionDiaryStatisticsDTO statistics, List<EmotionDiary> diaries) {
        List<Integer> sleepQualities = diaries.stream()
                .map(EmotionDiary::getSleepQuality)
                .filter(Objects::nonNull)
                .toList();

        if (!sleepQualities.isEmpty()) {
            double avgSleep = sleepQualities.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            statistics.setAverageSleepQuality(BigDecimal.valueOf(avgSleep).setScale(1, RoundingMode.HALF_UP));
        }

        List<Integer> stressLevels = diaries.stream()
                .map(EmotionDiary::getStressLevel)
                .filter(Objects::nonNull)
                .toList();

        if (!stressLevels.isEmpty()) {
            double avgStress = stressLevels.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            statistics.setAverageStressLevel(BigDecimal.valueOf(avgStress).setScale(1, RoundingMode.HALF_UP));
        }
    }

    /**
     * 计算情绪分布
     */
    private void calculateEmotionDistribution(EmotionDiaryStatisticsDTO statistics, List<EmotionDiary> diaries) {
        Map<String, Integer> emotionDistribution = diaries.stream()
                .filter(diary -> StrUtil.isNotBlank(diary.getDominantEmotion()))
                .collect(Collectors.groupingBy(
                        EmotionDiary::getDominantEmotion,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

        statistics.setEmotionDistribution(emotionDistribution);

        // 找出最常见的情绪
        String mostCommonEmotion = emotionDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("无");
        statistics.setMostCommonEmotion(mostCommonEmotion);
    }

    /**
     * 生成情绪趋势数据
     */
    private void generateMoodTrend(EmotionDiaryStatisticsDTO statistics, List<EmotionDiary> diaries) {
        List<EmotionDiaryStatisticsDTO.MoodTrendData> trendData = diaries.stream()
                .map(diary -> EmotionDiaryConvert.buildMoodTrendData(
                        diary.getDiaryDate().format(DateTimeFormatter.ofPattern("MM/dd")),
                        diary.getMoodScore(),
                        diary.getDominantEmotion()
                ))
                .collect(Collectors.toList());

        statistics.setMoodTrend(trendData);
    }

    /**
     * 生成改善建议
     */
    private void generateSuggestions(EmotionDiaryStatisticsDTO statistics, List<EmotionDiary> diaries) {
        List<String> suggestions = new ArrayList<>();

        BigDecimal avgMood = statistics.getAverageMoodScore();
        if (avgMood != null) {
            if (avgMood.compareTo(BigDecimal.valueOf(7)) >= 0) {
                suggestions.add("您的情绪状态整体良好，建议继续保持现有的生活方式");
            } else if (avgMood.compareTo(BigDecimal.valueOf(4)) <= 0) {
                suggestions.add("您的情绪状态偏低，建议寻求专业心理咨询师的帮助");
                suggestions.add("可以尝试适量运动和放松训练来改善情绪");
            } else {
                suggestions.add("情绪状态一般，建议多进行愉快的活动和社交");
            }
        }

        BigDecimal avgSleep = statistics.getAverageSleepQuality();
        if (avgSleep != null && avgSleep.compareTo(BigDecimal.valueOf(3)) < 0) {
            suggestions.add("睡眠质量有待改善，建议规律作息和营造良好睡眠环境");
        }

        BigDecimal avgStress = statistics.getAverageStressLevel();
        if (avgStress != null && avgStress.compareTo(BigDecimal.valueOf(3)) > 0) {
            suggestions.add("压力水平较高，建议学习压力管理技巧，如冥想、深呼吸等");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("继续保持记录习惯，关注情绪变化模式");
        }

        statistics.setSuggestions(suggestions);
    }

    // ========== 管理员方法 ==========

    /**
     * 管理员分页查询所有用户情绪日记
     */
    @Transactional(readOnly = true)
    public Page<EmotionDiaryResponseDTO> selectAdminPage(EmotionDiaryQueryDTO queryDTO) {
        Page<EmotionDiary> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        wrapper.eq(queryDTO.getUserId() != null, EmotionDiary::getUserId, queryDTO.getUserId())
                .ge(queryDTO.getStartDate() != null, EmotionDiary::getDiaryDate, queryDTO.getStartDate())
                .le(queryDTO.getEndDate() != null, EmotionDiary::getDiaryDate, queryDTO.getEndDate())
                .ge(queryDTO.getMinMoodScore() != null, EmotionDiary::getMoodScore, queryDTO.getMinMoodScore())
                .le(queryDTO.getMaxMoodScore() != null, EmotionDiary::getMoodScore, queryDTO.getMaxMoodScore())
                .eq(StrUtil.isNotBlank(queryDTO.getDominantEmotion()), EmotionDiary::getDominantEmotion, queryDTO.getDominantEmotion())
                .eq(queryDTO.getSleepQuality() != null, EmotionDiary::getSleepQuality, queryDTO.getSleepQuality())
                .eq(queryDTO.getStressLevel() != null, EmotionDiary::getStressLevel, queryDTO.getStressLevel());

        // 如果有用户名查询条件，需要关联用户表
        if (StrUtil.isNotBlank(queryDTO.getUsername())) {
            wrapper.in(EmotionDiary::getUserId, 
                    userMapper.selectList(new LambdaQueryWrapper<User>()
                            .like(User::getUsername, queryDTO.getUsername())
                            .or()
                            .like(User::getNickname, queryDTO.getUsername()))
                            .stream()
                            .map(User::getId)
                            .collect(Collectors.toList()));
        }

        wrapper.orderByDesc(EmotionDiary::getDiaryDate, EmotionDiary::getCreatedAt);

        Page<EmotionDiary> entityPage = emotionDiaryMapper.selectPage(page, wrapper);
        
        // 转换为响应DTO
        List<EmotionDiaryResponseDTO> responseDTOs = entityPage.getRecords().stream()
                .map(EmotionDiaryConvert::entityToResponse)
                .collect(Collectors.toList());

        // 填充用户信息
        responseDTOs.forEach(dto -> {
            User user = userMapper.selectById(dto.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setNickname(user.getNickname());
            }
        });

        Page<EmotionDiaryResponseDTO> responsePage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        responsePage.setRecords(responseDTOs);
        
        return responsePage;
    }

    /**
     * 管理员获取统计数据
     */
    @Transactional(readOnly = true)
    public EmotionDiaryStatisticsDTO getAdminStatistics(Long userId, Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(EmotionDiary::getDiaryDate, startDate)
                .le(EmotionDiary::getDiaryDate, endDate);
        
        if (userId != null) {
            wrapper.eq(EmotionDiary::getUserId, userId);
        }

        List<EmotionDiary> diaries = emotionDiaryMapper.selectList(wrapper);

        EmotionDiaryStatisticsDTO statistics = new EmotionDiaryStatisticsDTO();
        statistics.setRecordDays(diaries.size());
        statistics.setTargetDays(days);

        if (diaries.isEmpty()) {
            setDefaultStatistics(statistics);
            return statistics;
        }

        // 计算各项统计指标
        calculateMoodStatistics(statistics, diaries);
        calculateLifeIndicatorStatistics(statistics, diaries);
        calculateEmotionDistribution(statistics, diaries);
        generateMoodTrend(statistics, diaries);
        generateSuggestions(statistics, diaries);

        return statistics;
    }

    /**
     * 管理员删除情绪日记
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteDiary(Long diaryId) {
        EmotionDiary existingDiary = emotionDiaryMapper.selectById(diaryId);
        if (existingDiary == null) {
            throw new BusinessException("日记记录不存在");
        }

        int deleteResult = emotionDiaryMapper.deleteById(diaryId);
        if (deleteResult <= 0) {
            throw new BusinessException("删除失败");
        }
    }

    /**
     * 获取系统概览统计
     */
    @Transactional(readOnly = true)
    public EmotionDiaryStatisticsDTO getSystemOverview() {
        EmotionDiaryStatisticsDTO overview = new EmotionDiaryStatisticsDTO();
        
        // 总记录数
        Long totalRecords = emotionDiaryMapper.selectCount(null);
        overview.setRecordDays(totalRecords.intValue());
        
        // 今日记录数
        LocalDate today = LocalDate.now();
        Long todayRecords = emotionDiaryMapper.selectCount(
                new LambdaQueryWrapper<EmotionDiary>()
                        .eq(EmotionDiary::getDiaryDate, today)
        );
        overview.setTargetDays(todayRecords.intValue());
        
        // 本周记录数
        LocalDate weekStart = today.minusDays(6);
        List<EmotionDiary> weekDiaries = emotionDiaryMapper.selectList(
                new LambdaQueryWrapper<EmotionDiary>()
                        .ge(EmotionDiary::getDiaryDate, weekStart)
                        .le(EmotionDiary::getDiaryDate, today)
        );
        
        // 活跃用户数（本周有记录的用户）
        long activeUsers = weekDiaries.stream()
                .map(EmotionDiary::getUserId)
                .distinct()
                .count();
        overview.setPositiveDays((int) activeUsers);
        
        // 计算平均情绪评分
        if (!weekDiaries.isEmpty()) {
            double avgMood = weekDiaries.stream()
                    .map(EmotionDiary::getMoodScore)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            overview.setAverageMoodScore(BigDecimal.valueOf(avgMood).setScale(1, RoundingMode.HALF_UP));
        } else {
            overview.setAverageMoodScore(BigDecimal.ZERO);
        }
        
        // 情绪分布
        Map<String, Integer> emotionDistribution = weekDiaries.stream()
                .filter(diary -> cn.hutool.core.util.StrUtil.isNotBlank(diary.getDominantEmotion()))
                .collect(Collectors.groupingBy(
                        EmotionDiary::getDominantEmotion,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));
        overview.setEmotionDistribution(emotionDistribution);
        
        return overview;
    }

    /**
     * 管理员手动触发AI分析（支持重复分析）
     * 
     * @param diaryId 日记ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminTriggerAiAnalysis(Long diaryId) {
        log.info("管理员触发AI情绪分析，日记ID: {}", diaryId);

        EmotionDiary diary = emotionDiaryMapper.selectById(diaryId);
        if (diary == null) {
            throw new BusinessException("日记记录不存在");
        }

        // 检查日记内容
        String analysisContent = diary.getAnalysisContent();
        if (StrUtil.isBlank(analysisContent)) {
            throw new BusinessException("日记内容为空，无法进行AI分析");
        }

        // 管理员可以重复分析，无需检查是否已分析过
        // 异步触发AI分析（管理员触发，高优先级）
        performAiEmotionAnalysisAsync(diaryId, analysisContent, AiTaskType.ADMIN, 3);
        log.info("管理员已提交AI情绪分析任务到队列，日记ID: {}", diaryId);
    }

    /**
     * 管理员批量触发AI分析
     * 
     * @param diaryIds 日记ID列表
     * @return 处理结果统计
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> adminBatchTriggerAiAnalysis(List<Long> diaryIds) {
        log.info("管理员批量触发AI情绪分析，日记数量: {}", diaryIds.size());

        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> failReasons = new ArrayList<>();

        for (Long diaryId : diaryIds) {
            try {
                EmotionDiary diary = emotionDiaryMapper.selectById(diaryId);
                if (diary == null) {
                    failCount++;
                    failReasons.add("日记ID " + diaryId + ": 记录不存在");
                    continue;
                }

                String analysisContent = diary.getAnalysisContent();
                if (StrUtil.isBlank(analysisContent)) {
                    failCount++;
                    failReasons.add("日记ID " + diaryId + ": 内容为空");
                    continue;
                }

                // 异步触发AI分析（批量触发，正常优先级）
                performAiEmotionAnalysisAsync(diaryId, analysisContent, AiTaskType.BATCH, 2);
                successCount++;
                log.debug("已提交AI分析任务，日记ID: {}", diaryId);

            } catch (Exception e) {
                failCount++;
                failReasons.add("日记ID " + diaryId + ": " + e.getMessage());
                log.warn("批量AI分析失败，日记ID: {}, 错误: {}", diaryId, e.getMessage());
            }
        }

        result.put("totalCount", diaryIds.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failReasons", failReasons);

        log.info("管理员批量AI分析完成，总数: {}, 成功: {}, 失败: {}", 
                diaryIds.size(), successCount, failCount);

        return result;
    }
}
