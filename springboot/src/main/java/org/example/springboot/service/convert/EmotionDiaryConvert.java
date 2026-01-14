package org.example.springboot.service.convert;

import org.example.springboot.DTO.command.EmotionDiaryCreateDTO;
import org.example.springboot.DTO.command.EmotionDiaryUpdateDTO;
import org.example.springboot.DTO.response.EmotionDiaryResponseDTO;
import org.example.springboot.DTO.response.EmotionDiaryStatisticsDTO;
import org.example.springboot.entity.EmotionDiary;
import org.example.springboot.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 情绪日记转换类
 * @author system
 */
public class EmotionDiaryConvert {

    /**
     * 创建命令DTO转换为EmotionDiary实体
     * @param createDTO 创建命令DTO
     * @param userId 用户ID
     * @return EmotionDiary实体
     */
    public static EmotionDiary createCommandToEntity(EmotionDiaryCreateDTO createDTO, Long userId) {
        EmotionDiary diary = new EmotionDiary();
        diary.setUserId(userId);
        diary.setDiaryDate(createDTO.getDiaryDate());
        diary.setMoodScore(createDTO.getMoodScore());
        diary.setDominantEmotion(createDTO.getDominantEmotion());
        diary.setEmotionTriggers(createDTO.getEmotionTriggers());
        diary.setDiaryContent(createDTO.getDiaryContent());
        diary.setSleepQuality(createDTO.getSleepQuality());
        diary.setStressLevel(createDTO.getStressLevel());
        diary.setCreatedAt(LocalDateTime.now());
        diary.setUpdatedAt(LocalDateTime.now());
        return diary;
    }

    /**
     * 更新命令DTO转换为EmotionDiary实体（用于更新操作）
     * @param updateDTO 更新命令DTO
     * @return EmotionDiary实体
     */
    public static EmotionDiary updateCommandToEntity(EmotionDiaryUpdateDTO updateDTO) {
        EmotionDiary diary = new EmotionDiary();
        diary.setId(updateDTO.getId());
        diary.setMoodScore(updateDTO.getMoodScore());
        diary.setDominantEmotion(updateDTO.getDominantEmotion());
        diary.setEmotionTriggers(updateDTO.getEmotionTriggers());
        diary.setDiaryContent(updateDTO.getDiaryContent());
        diary.setSleepQuality(updateDTO.getSleepQuality());
        diary.setStressLevel(updateDTO.getStressLevel());
        diary.setUpdatedAt(LocalDateTime.now());
        return diary;
    }

    /**
     * EmotionDiary实体转换为响应DTO
     * @param diary EmotionDiary实体
     * @param user 关联的用户信息（可选）
     * @return 情绪日记响应DTO
     */
    public static EmotionDiaryResponseDTO entityToResponse(EmotionDiary diary, User user) {
        EmotionDiaryResponseDTO responseDTO = new EmotionDiaryResponseDTO();
        responseDTO.setId(diary.getId());
        responseDTO.setUserId(diary.getUserId());
        responseDTO.setDiaryDate(diary.getDiaryDate());
        responseDTO.setMoodScore(diary.getMoodScore());
        responseDTO.setDominantEmotion(diary.getDominantEmotion());
        responseDTO.setEmotionTriggers(diary.getEmotionTriggers());
        responseDTO.setDiaryContent(diary.getDiaryContent());
        // 设置日记内容预览
        if (diary.getDiaryContent() != null) {
            String preview = diary.getDiaryContent().length() > 100 
                ? diary.getDiaryContent().substring(0, 100) + "..." 
                : diary.getDiaryContent();
            responseDTO.setDiaryContentPreview(preview);
        }
        responseDTO.setSleepQuality(diary.getSleepQuality());
        responseDTO.setStressLevel(diary.getStressLevel());
        responseDTO.setCreatedAt(diary.getCreatedAt());
        responseDTO.setUpdatedAt(diary.getUpdatedAt());
        responseDTO.setAiEmotionAnalysis(diary.getAiEmotionAnalysis());
        responseDTO.setAiAnalysisUpdatedAt(diary.getAiAnalysisUpdatedAt());
        responseDTO.setHasAiEmotionAnalysis(diary.hasAiEmotionAnalysis());
        
        // 设置AI分析状态
        if (diary.getAiEmotionAnalysis() != null && !diary.getAiEmotionAnalysis().trim().isEmpty()) {
            responseDTO.setAiAnalysisStatus("COMPLETED");
        } else if (diary.getAiAnalysisUpdatedAt() != null) {
            // 如果有更新时间但没有分析结果，检查是否是分析失败
            // 如果更新时间超过10分钟且没有结果，认为是分析失败
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            long minutesSinceUpdate = java.time.Duration.between(diary.getAiAnalysisUpdatedAt(), now).toMinutes();
            if (minutesSinceUpdate > 10) {
                responseDTO.setAiAnalysisStatus("FAILED");
            } else {
                responseDTO.setAiAnalysisStatus("PENDING");
            }
        } else {
            responseDTO.setAiAnalysisStatus("PENDING");
        }

        // 设置用户信息（如果提供）
        if (user != null) {
            responseDTO.setUsername(user.getUsername());
            responseDTO.setNickname(user.getNickname());
            responseDTO.setUserNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
        }

        return responseDTO;
    }

    /**
     * EmotionDiary实体转换为响应DTO（不包含用户信息）
     * @param diary EmotionDiary实体
     * @return 情绪日记响应DTO
     */
    public static EmotionDiaryResponseDTO entityToResponse(EmotionDiary diary) {
        return entityToResponse(diary, null);
    }

    /**
     * 批量转换EmotionDiary实体列表为响应DTO列表
     * @param diaries EmotionDiary实体列表
     * @return 情绪日记响应DTO列表
     */
    public static List<EmotionDiaryResponseDTO> entityListToResponseList(List<EmotionDiary> diaries) {
        return diaries.stream()
                .map(EmotionDiaryConvert::entityToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换EmotionDiary实体列表为响应DTO列表（包含用户信息映射）
     * @param diaries EmotionDiary实体列表
     * @param userMap 用户信息映射（userId -> User）
     * @return 情绪日记响应DTO列表
     */
    public static List<EmotionDiaryResponseDTO> entityListToResponseList(List<EmotionDiary> diaries, 
                                                                       Map<Long, User> userMap) {
        return diaries.stream()
                .map(diary -> entityToResponse(diary, userMap.get(diary.getUserId())))
                .collect(Collectors.toList());
    }

    /**
     * 构建情绪趋势数据
     * @param dateLabel 日期标签
     * @param moodScore 情绪评分
     * @param dominantEmotion 主要情绪
     * @return 情绪趋势数据
     */
    public static EmotionDiaryStatisticsDTO.MoodTrendData buildMoodTrendData(String dateLabel, 
                                                                              Integer moodScore, 
                                                                              String dominantEmotion) {
        EmotionDiaryStatisticsDTO.MoodTrendData trendData = new EmotionDiaryStatisticsDTO.MoodTrendData();
        trendData.setDateLabel(dateLabel);
        trendData.setMoodScore(moodScore);
        trendData.setDominantEmotion(dominantEmotion);
        return trendData;
    }
}
