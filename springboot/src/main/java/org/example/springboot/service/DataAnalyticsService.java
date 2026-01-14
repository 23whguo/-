package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.response.DataAnalyticsResponseDTO;
import org.example.springboot.entity.*;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据分析服务
 * @author system
 */
@Slf4j
@Service
public class DataAnalyticsService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;

    @Resource
    private ConsultationSessionMapper consultationSessionMapper;

    @Resource
    private ConsultationMessageMapper consultationMessageMapper;

    /**
     * 获取综合数据分析
     * @param days 分析天数范围
     * @return 数据分析结果
     */
    @Transactional(readOnly = true)
    public DataAnalyticsResponseDTO getDataAnalytics(Integer days) {
        try {
            log.info("开始获取数据分析，分析天数: {}", days);

            // 设置默认分析天数
            if (days == null || days <= 0) {
                days = 30;
            }

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);

            DataAnalyticsResponseDTO analytics = DataAnalyticsResponseDTO.builder()
                    .systemOverview(getSystemOverview(startDate, endDate))
                    .emotionHeatmap(getEmotionHeatmapData(startDate, endDate))
                    .emotionTrend(getEmotionTrendData(startDate, endDate))
                    .consultationStats(getConsultationStatistics(startDate, endDate))
                    .userActivity(getUserActivityData(startDate, endDate))
                    .build();

            log.info("数据分析获取完成");
            return analytics;

        } catch (Exception e) {
            log.error("获取数据分析失败", e);
            throw new ServiceException("获取数据分析失败，请稍后重试");
        }
    }

    /**
     * 获取系统概览数据
     */
    private DataAnalyticsResponseDTO.SystemOverview getSystemOverview(LocalDate startDate, LocalDate endDate) {
        // 总用户数
        Long totalUsers = userMapper.selectCount(null);

        // 活跃用户数(在时间范围内有记录的用户)
        LambdaQueryWrapper<EmotionDiary> activeDiaryWrapper = new LambdaQueryWrapper<>();
        activeDiaryWrapper.ge(EmotionDiary::getDiaryDate, startDate)
                .le(EmotionDiary::getDiaryDate, endDate);
        List<EmotionDiary> activeDiaries = emotionDiaryMapper.selectList(activeDiaryWrapper);
        
        LambdaQueryWrapper<ConsultationSession> activeSessionWrapper = new LambdaQueryWrapper<>();
        activeSessionWrapper.ge(ConsultationSession::getStartedAt, startDate.atStartOfDay())
                .le(ConsultationSession::getStartedAt, endDate.atTime(23, 59, 59));
        List<ConsultationSession> activeSessions = consultationSessionMapper.selectList(activeSessionWrapper);

        Set<Long> activeUserIds = new HashSet<>();
        activeDiaries.forEach(diary -> activeUserIds.add(diary.getUserId()));
        activeSessions.forEach(session -> activeUserIds.add(session.getUserId()));
        Long activeUsers = (long) activeUserIds.size();

        // 情绪日记总数
        Long totalDiaries = emotionDiaryMapper.selectCount(null);

        // 咨询会话总数
        Long totalSessions = consultationSessionMapper.selectCount(null);

        // 平均情绪评分
        List<EmotionDiary> allDiaries = emotionDiaryMapper.selectList(
                new LambdaQueryWrapper<EmotionDiary>()
                        .ge(EmotionDiary::getDiaryDate, startDate)
                        .le(EmotionDiary::getDiaryDate, endDate)
        );
        BigDecimal avgMoodScore = allDiaries.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(allDiaries.stream()
                        .filter(diary -> diary.getMoodScore() != null)
                        .mapToInt(EmotionDiary::getMoodScore)
                        .average()
                        .orElse(0.0))
                        .setScale(1, RoundingMode.HALF_UP);

        // 今日统计
        LocalDate today = LocalDate.now();
        
        // 今日新增用户
        Long todayNewUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .ge(User::getCreatedAt, today.atStartOfDay())
                        .le(User::getCreatedAt, today.atTime(23, 59, 59))
        );

        // 今日新增日记
        Long todayNewDiaries = emotionDiaryMapper.selectCount(
                new LambdaQueryWrapper<EmotionDiary>()
                        .eq(EmotionDiary::getDiaryDate, today)
        );

        // 今日新增会话
        Long todayNewSessions = consultationSessionMapper.selectCount(
                new LambdaQueryWrapper<ConsultationSession>()
                        .ge(ConsultationSession::getStartedAt, today.atStartOfDay())
                        .le(ConsultationSession::getStartedAt, today.atTime(23, 59, 59))
        );

        return DataAnalyticsResponseDTO.SystemOverview.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalDiaries(totalDiaries)
                .totalSessions(totalSessions)
                .avgMoodScore(avgMoodScore)
                .todayNewUsers(todayNewUsers)
                .todayNewDiaries(todayNewDiaries)
                .todayNewSessions(todayNewSessions)
                .build();
    }

    /**
     * 获取情绪热力图数据
     */
    private DataAnalyticsResponseDTO.EmotionHeatmapData getEmotionHeatmapData(LocalDate startDate, LocalDate endDate) {
        // 查询时间范围内的所有情绪日记
        LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(EmotionDiary::getDiaryDate, startDate)
                .le(EmotionDiary::getDiaryDate, endDate);
        List<EmotionDiary> diaries = emotionDiaryMapper.selectList(wrapper);

        // 初始化7x24的热力图网格 (7天 x 24小时)
        List<List<DataAnalyticsResponseDTO.HeatmapPoint>> gridData = new ArrayList<>();
        for (int day = 0; day < 7; day++) {
            List<DataAnalyticsResponseDTO.HeatmapPoint> dayData = new ArrayList<>();
            for (int hour = 0; hour < 24; hour++) {
                dayData.add(DataAnalyticsResponseDTO.HeatmapPoint.builder()
                        .x(hour)
                        .y(day)
                        .value(0)
                        .avgMoodScore(BigDecimal.ZERO)
                        .dominantEmotion("平静")
                        .build());
            }
            gridData.add(dayData);
        }

        // 按创建时间分组统计(按小时和星期分布)
        Map<String, List<EmotionDiary>> timeGrouped = diaries.stream()
                .filter(diary -> diary.getCreatedAt() != null)
                .collect(Collectors.groupingBy(diary -> {
                    LocalDateTime createdAt = diary.getCreatedAt();
                    int dayOfWeek = createdAt.getDayOfWeek().getValue() % 7; // 转换为0-6
                    int hour = createdAt.getHour();
                    return dayOfWeek + "_" + hour;
                }));

        // 填充热力图数据
        String peakEmotionTime = "00:00";
        int maxValue = 0;
        
        for (Map.Entry<String, List<EmotionDiary>> entry : timeGrouped.entrySet()) {
            String[] parts = entry.getKey().split("_");
            int day = Integer.parseInt(parts[0]);
            int hour = Integer.parseInt(parts[1]);
            
            List<EmotionDiary> dayHourDiaries = entry.getValue();
            int count = dayHourDiaries.size();
            
            // 计算平均情绪评分
            double avgMood = dayHourDiaries.stream()
                    .filter(diary -> diary.getMoodScore() != null)
                    .mapToInt(EmotionDiary::getMoodScore)
                    .average()
                    .orElse(0.0);

            // 统计主要情绪
            String dominantEmotion = dayHourDiaries.stream()
                    .map(EmotionDiary::getDominantEmotion)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.groupingBy(emotion -> emotion, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("平静");

            // 更新网格数据
            gridData.get(day).set(hour, DataAnalyticsResponseDTO.HeatmapPoint.builder()
                    .x(hour)
                    .y(day)
                    .value(count)
                    .avgMoodScore(BigDecimal.valueOf(avgMood).setScale(1, RoundingMode.HALF_UP))
                    .dominantEmotion(dominantEmotion)
                    .build());

            // 记录峰值时间
            if (count > maxValue) {
                maxValue = count;
                peakEmotionTime = String.format("%02d:00", hour);
            }
        }

        // 统计情绪分布
        Map<String, Integer> emotionDistribution = diaries.stream()
                .map(EmotionDiary::getDominantEmotion)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.groupingBy(
                        emotion -> emotion,
                        Collectors.reducing(0, e -> 1, Integer::sum)
                ));

        String dateRange = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + 
                          " 至 " + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return DataAnalyticsResponseDTO.EmotionHeatmapData.builder()
                .gridData(gridData)
                .emotionDistribution(emotionDistribution)
                .peakEmotionTime(peakEmotionTime)
                .dateRange(dateRange)
                .build();
    }

    /**
     * 获取情绪趋势数据
     */
    private List<DataAnalyticsResponseDTO.EmotionTrendData> getEmotionTrendData(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(EmotionDiary::getDiaryDate, startDate)
                .le(EmotionDiary::getDiaryDate, endDate);
        List<EmotionDiary> diaries = emotionDiaryMapper.selectList(wrapper);

        // 按日期分组
        Map<LocalDate, List<EmotionDiary>> dailyDiaries = diaries.stream()
                .collect(Collectors.groupingBy(EmotionDiary::getDiaryDate));

        List<DataAnalyticsResponseDTO.EmotionTrendData> trendData = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<EmotionDiary> dayDiaries = dailyDiaries.getOrDefault(date, Collections.emptyList());
            
            if (dayDiaries.isEmpty()) {
                trendData.add(DataAnalyticsResponseDTO.EmotionTrendData.builder()
                        .date(date)
                        .avgMoodScore(BigDecimal.ZERO)
                        .recordCount(0)
                        .positiveRatio(BigDecimal.ZERO)
                        .negativeRatio(BigDecimal.ZERO)
                        .dominantEmotion("无数据")
                        .build());
                continue;
            }

            // 计算平均情绪评分
            double avgMood = dayDiaries.stream()
                    .filter(diary -> diary.getMoodScore() != null)
                    .mapToInt(EmotionDiary::getMoodScore)
                    .average()
                    .orElse(0.0);

            // 计算正负面情绪占比
            long positiveCount = dayDiaries.stream()
                    .filter(diary -> diary.getMoodScore() != null && diary.getMoodScore() >= 6)
                    .count();
            long negativeCount = dayDiaries.stream()
                    .filter(diary -> diary.getMoodScore() != null && diary.getMoodScore() <= 4)
                    .count();

            BigDecimal positiveRatio = !dayDiaries.isEmpty() ?
                    BigDecimal.valueOf((double) positiveCount / dayDiaries.size() * 100).setScale(1, RoundingMode.HALF_UP) : 
                    BigDecimal.ZERO;
            BigDecimal negativeRatio = !dayDiaries.isEmpty() ?
                    BigDecimal.valueOf((double) negativeCount / dayDiaries.size() * 100).setScale(1, RoundingMode.HALF_UP) : 
                    BigDecimal.ZERO;

            // 统计主要情绪
            String dominantEmotion = dayDiaries.stream()
                    .map(EmotionDiary::getDominantEmotion)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.groupingBy(emotion -> emotion, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("平静");

            trendData.add(DataAnalyticsResponseDTO.EmotionTrendData.builder()
                    .date(date)
                    .avgMoodScore(BigDecimal.valueOf(avgMood).setScale(1, RoundingMode.HALF_UP))
                    .recordCount(dayDiaries.size())
                    .positiveRatio(positiveRatio)
                    .negativeRatio(negativeRatio)
                    .dominantEmotion(dominantEmotion)
                    .build());
        }

        return trendData;
    }

    /**
     * 获取咨询会话统计
     */
    private DataAnalyticsResponseDTO.ConsultationStatistics getConsultationStatistics(LocalDate startDate, LocalDate endDate) {
        // 查询时间范围内的会话
        LambdaQueryWrapper<ConsultationSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(ConsultationSession::getStartedAt, startDate.atStartOfDay())
                .le(ConsultationSession::getStartedAt, endDate.atTime(23, 59, 59));
        List<ConsultationSession> sessions = consultationSessionMapper.selectList(wrapper);

        long totalSessions = (long) sessions.size();

        // 计算平均会话时长
        BigDecimal avgDurationMinutes = sessions.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(sessions.stream()
                        .filter(session -> session.getDurationMinutes() != null)
                        .mapToLong(ConsultationSession::getDurationMinutes)
                        .average()
                        .orElse(0.0))
                        .setScale(1, RoundingMode.HALF_UP);

        // 移除情绪改善统计功能 - 属性已从DTO中删除

        // 每日会话数趋势
        Map<LocalDate, List<ConsultationSession>> dailySessions = sessions.stream()
                .collect(Collectors.groupingBy(session -> session.getStartedAt().toLocalDate()));

        List<DataAnalyticsResponseDTO.DailySessionCount> dailyTrend = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<ConsultationSession> daySessions = dailySessions.getOrDefault(date, Collections.emptyList());
            Set<Long> userIds = daySessions.stream()
                    .map(ConsultationSession::getUserId)
                    .collect(Collectors.toSet());

            dailyTrend.add(DataAnalyticsResponseDTO.DailySessionCount.builder()
                    .date(date)
                    .sessionCount(daySessions.size())
                    .userCount(userIds.size())
                    .build());
        }

        // 高频情绪标签(从消息中获取)
        List<Long> sessionIds = sessions.stream()
                .map(ConsultationSession::getId)
                .toList();

        Map<String, Integer> topEmotionTags = new HashMap<>();
        if (!sessionIds.isEmpty()) {
            LambdaQueryWrapper<ConsultationMessage> messageWrapper = new LambdaQueryWrapper<>();
            messageWrapper.in(ConsultationMessage::getSessionId, sessionIds)
                    .isNotNull(ConsultationMessage::getEmotionTag)
                    .ne(ConsultationMessage::getEmotionTag, "");
            
            List<ConsultationMessage> messages = consultationMessageMapper.selectList(messageWrapper);
            topEmotionTags = messages.stream()
                    .map(ConsultationMessage::getEmotionTag)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.groupingBy(
                            tag -> tag,
                            Collectors.reducing(0, e -> 1, Integer::sum)
                    ));
        }

        return DataAnalyticsResponseDTO.ConsultationStatistics.builder()
                .totalSessions(totalSessions)
                .avgDurationMinutes(avgDurationMinutes)
                .dailyTrend(dailyTrend)
                .topEmotionTags(topEmotionTags)
                .build();
    }

    /**
     * 获取用户活跃度数据
     */
    private List<DataAnalyticsResponseDTO.UserActivityData> getUserActivityData(LocalDate startDate, LocalDate endDate) {
        List<DataAnalyticsResponseDTO.UserActivityData> activityData = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // 新增用户数
            Long newUsers = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .ge(User::getCreatedAt, date.atStartOfDay())
                            .le(User::getCreatedAt, date.atTime(23, 59, 59))
            );

            // 日记记录用户数
            List<EmotionDiary> dayDiaries = emotionDiaryMapper.selectList(
                    new LambdaQueryWrapper<EmotionDiary>()
                            .eq(EmotionDiary::getDiaryDate, date)
            );
            Set<Long> diaryUserIds = dayDiaries.stream()
                    .map(EmotionDiary::getUserId)
                    .collect(Collectors.toSet());

            // 咨询用户数
            List<ConsultationSession> daySessions = consultationSessionMapper.selectList(
                    new LambdaQueryWrapper<ConsultationSession>()
                            .ge(ConsultationSession::getStartedAt, date.atStartOfDay())
                            .le(ConsultationSession::getStartedAt, date.atTime(23, 59, 59))
            );
            Set<Long> consultationUserIds = daySessions.stream()
                    .map(ConsultationSession::getUserId)
                    .collect(Collectors.toSet());

            // 活跃用户数(日记或咨询任一活动)
            Set<Long> allActiveUserIds = new HashSet<>(diaryUserIds);
            allActiveUserIds.addAll(consultationUserIds);

            activityData.add(DataAnalyticsResponseDTO.UserActivityData.builder()
                    .date(date)
                    .activeUsers(allActiveUserIds.size())
                    .newUsers(newUsers.intValue())
                    .diaryUsers(diaryUserIds.size())
                    .consultationUsers(consultationUserIds.size())
                    .build());
        }

        return activityData;
    }
}
