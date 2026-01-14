package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.command.EmotionDiaryCreateDTO;
import org.example.springboot.DTO.command.EmotionDiaryUpdateDTO;
import org.example.springboot.DTO.query.EmotionDiaryQueryDTO;
import org.example.springboot.DTO.response.EmotionDiaryResponseDTO;
import org.example.springboot.DTO.response.EmotionDiaryStatisticsDTO;
import org.example.springboot.common.Result;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.service.EmotionDiaryService;
import org.example.springboot.util.JwtTokenUtils;
import org.example.springboot.AiService.StructOutPut;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 情绪日记控制器
 * @author system
 */
@Tag(name = "情绪日记管理", description = "情绪日记的增删改查及统计分析")
@Slf4j
@RestController
@RequestMapping("/emotion-diary")
@Validated
public class EmotionDiaryController {

    @Resource
    private EmotionDiaryService emotionDiaryService;

    /**
     * 创建或更新情绪日记
     * 同一天只能有一条记录，如果已存在则提示进入编辑模式
     */
    @Operation(summary = "创建或更新情绪日记", description = "创建新的情绪日记记录，同一天已存在记录则提示进入编辑模式")
    @PostMapping
    public Result<EmotionDiaryResponseDTO> createOrUpdateDiary(
            @Valid @RequestBody EmotionDiaryCreateDTO createDTO,
            @Parameter(description = "是否为编辑模式") @RequestParam(required = false) Boolean isEditMode) {
        log.info("收到创建或更新情绪日记请求: {}, 编辑模式: {}", createDTO, isEditMode);

        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            EmotionDiaryResponseDTO responseDTO = emotionDiaryService.createOrUpdateDiary(userId, createDTO, isEditMode);
            return Result.success(responseDTO);
        } catch (Exception e) {
            log.error("创建或更新情绪日记失败: {}", e.getMessage(), e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 更新情绪日记
     */
    @Operation(summary = "更新情绪日记", description = "更新指定的情绪日记记录")
    @PutMapping("/{id}")
    public Result<EmotionDiaryResponseDTO> updateDiary(
            @Parameter(description = "日记ID") @PathVariable Long id,
            @Valid @RequestBody EmotionDiaryUpdateDTO updateDTO) {
        log.info("收到更新情绪日记请求，ID: {}, 数据: {}", id, updateDTO);

        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        // 设置日记ID
        updateDTO.setId(id);

        try {
            EmotionDiaryResponseDTO responseDTO = emotionDiaryService.updateDiary(userId, updateDTO);
            return Result.success(responseDTO);
        } catch (Exception e) {
            log.error("更新情绪日记失败: {}", e.getMessage(), e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取情绪日记
     */
    @Operation(summary = "获取情绪日记详情", description = "根据日记ID获取情绪日记详细信息")
    @GetMapping("/{id}")
    public Result<EmotionDiaryResponseDTO> getDiaryById(
            @Parameter(description = "日记ID") @PathVariable Long id) {
        log.info("收到获取情绪日记请求，ID: {}", id);

        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            EmotionDiaryResponseDTO responseDTO = emotionDiaryService.getDiaryById(userId, id);
            return Result.success(responseDTO);
        } catch (Exception e) {
            log.error("获取情绪日记失败: {}", e.getMessage(), e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据日期获取情绪日记
     */
    @Operation(summary = "根据日期获取情绪日记", description = "获取指定日期的情绪日记记录")
    @GetMapping("/date/{date}")
    public Result<EmotionDiaryResponseDTO> getDiaryByDate(
            @Parameter(description = "日期 (格式: yyyy-MM-dd)") @PathVariable String date) {
        log.info("收到根据日期获取情绪日记请求，日期: {}", date);

        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            LocalDate diaryDate = LocalDate.parse(date);
            EmotionDiaryResponseDTO responseDTO = emotionDiaryService.getDiaryByDate(userId, diaryDate);
            
            if (responseDTO == null) {
                return Result.error("该日期没有日记记录");
            }
            
            return Result.success(responseDTO);
        } catch (Exception e) {
            log.error("根据日期获取情绪日记失败: {}", e.getMessage(), e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询情绪日记
     */
    @Operation(summary = "分页查询情绪日记", description = "根据条件分页查询用户的情绪日记列表")
    @GetMapping("/page")
    public Result<Page<EmotionDiaryResponseDTO>> getDiaryPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "最低情绪评分") @RequestParam(required = false) Integer minMoodScore,
            @Parameter(description = "最高情绪评分") @RequestParam(required = false) Integer maxMoodScore,
            @Parameter(description = "主要情绪") @RequestParam(required = false) String dominantEmotion,
            @Parameter(description = "睡眠质量") @RequestParam(required = false) Integer sleepQuality,
            @Parameter(description = "压力水平") @RequestParam(required = false) Integer stressLevel) {
        
        log.info("收到分页查询情绪日记请求，页码: {}, 大小: {}", current, size);

        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            EmotionDiaryQueryDTO queryDTO = new EmotionDiaryQueryDTO();
            queryDTO.setUserId(userId);
            queryDTO.setCurrent(current);
            queryDTO.setSize(size);
            
            // 设置查询条件
            if (startDate != null) {
                queryDTO.setStartDate(LocalDate.parse(startDate));
            }
            if (endDate != null) {
                queryDTO.setEndDate(LocalDate.parse(endDate));
            }
            queryDTO.setMinMoodScore(minMoodScore);
            queryDTO.setMaxMoodScore(maxMoodScore);
            queryDTO.setDominantEmotion(dominantEmotion);
            queryDTO.setSleepQuality(sleepQuality);
            queryDTO.setStressLevel(stressLevel);

            Page<EmotionDiaryResponseDTO> page = emotionDiaryService.selectPage(queryDTO);
            return Result.success(page);
        } catch (Exception e) {
            log.error("分页查询情绪日记失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 删除情绪日记
     */
    @Operation(summary = "删除情绪日记", description = "删除指定的情绪日记记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDiary(@Parameter(description = "日记ID") @PathVariable Long id) {
        log.info("收到删除情绪日记请求，ID: {}", id);

        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            emotionDiaryService.deleteDiary(userId, id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除情绪日记失败: {}", e.getMessage(), e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取情绪日记统计数据
     */
    @Operation(summary = "获取情绪统计数据", description = "获取用户的情绪日记统计分析数据")
    @GetMapping("/statistics")
    public Result<EmotionDiaryStatisticsDTO> getStatistics(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") Integer days) {
        log.info("收到获取情绪统计数据请求，统计天数: {}", days);

        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            EmotionDiaryStatisticsDTO statistics = emotionDiaryService.getStatistics(userId, days);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取情绪统计数据失败: {}", e.getMessage(), e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日情绪日记
     */
    @Operation(summary = "获取今日情绪日记", description = "获取当前用户今天的情绪日记记录")
    @GetMapping("/today")
    public Result<EmotionDiaryResponseDTO> getTodayDiary() {
        log.info("收到获取今日情绪日记请求");

        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            LocalDate today = LocalDate.now();
            EmotionDiaryResponseDTO responseDTO = emotionDiaryService.getDiaryByDate(userId, today);
            
            if (responseDTO == null) {
                return Result.error("今日还没有记录情绪日记");
            }
            
            return Result.success(responseDTO);
        } catch (Exception e) {
            log.error("获取今日情绪日记失败: {}", e.getMessage(), e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定日记的AI情绪分析结果
     */
    @Operation(summary = "获取AI情绪分析结果", description = "获取指定日记的AI情绪分析结果")
    @GetMapping("/{id}/ai-analysis")
    public Result<StructOutPut.EmotionAnalysisResult> getAiEmotionAnalysis(
            @Parameter(description = "日记ID") @PathVariable Long id) {
        log.info("收到获取AI情绪分析请求，日记ID: {}", id);

        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            // 先验证用户是否有权限访问该日记
            EmotionDiaryResponseDTO diary = emotionDiaryService.getDiaryById(userId, id);
            if (diary == null) {
                return Result.error("日记不存在或无权限访问");
            }

            // 获取AI分析结果
            StructOutPut.EmotionAnalysisResult analysisResult = emotionDiaryService.getAiEmotionAnalysis(id);
            if (analysisResult == null) {
                return Result.error("AI情绪分析结果尚未生成，请稍后再试");
            }

            return Result.success(analysisResult);
        } catch (Exception e) {
            log.error("获取AI情绪分析失败: {}", e.getMessage(), e);
            return Result.error("获取分析结果失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发AI情绪分析
     */
    @Operation(summary = "手动触发AI情绪分析", description = "手动触发指定日记的AI情绪分析")
    @PostMapping("/{id}/ai-analysis")
    public Result<Void> triggerAiEmotionAnalysis(
            @Parameter(description = "日记ID") @PathVariable Long id) {
        log.info("收到手动触发AI情绪分析请求，日记ID: {}", id);

        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            // 先验证用户是否有权限访问该日记
            EmotionDiaryResponseDTO diary = emotionDiaryService.getDiaryById(userId, id);
            if (diary == null) {
                return Result.error("日记不存在或无权限访问");
            }

            // 手动触发AI分析（异步处理）
            if (diary.getDiaryContent() != null && !diary.getDiaryContent().trim().isEmpty()) {
                emotionDiaryService.performAiEmotionAnalysisAsync(id, diary.getDiaryContent());
                log.info("已手动提交AI情绪分析任务到队列，日记ID: {}", id);
                return Result.success();
            } else {
                return Result.error("日记内容为空，无法进行AI分析");
            }
        } catch (Exception e) {
            log.error("触发AI情绪分析失败: {}", e.getMessage(), e);
            return Result.error("触发分析失败: " + e.getMessage());
        }
    }

    // ========== 管理员接口 ==========

    /**
     * 管理员分页查询所有用户情绪日记
     */
    @Operation(summary = "管理员分页查询情绪日记", description = "管理员查看所有用户的情绪日记记录")
    @GetMapping("/admin/page")
    public Result<Page<EmotionDiaryResponseDTO>> getAdminDiaryPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "最低情绪评分") @RequestParam(required = false) Integer minMoodScore,
            @Parameter(description = "最高情绪评分") @RequestParam(required = false) Integer maxMoodScore,
            @Parameter(description = "主要情绪") @RequestParam(required = false) String dominantEmotion) {
        
        log.info("管理员收到分页查询情绪日记请求，页码: {}, 大小: {}", current, size);

        try {
            EmotionDiaryQueryDTO queryDTO = new EmotionDiaryQueryDTO();
            queryDTO.setCurrent(current);
            queryDTO.setSize(size);
            queryDTO.setUserId(userId);
            queryDTO.setUsername(username);
            
            // 设置查询条件
            if (startDate != null) {
                queryDTO.setStartDate(LocalDate.parse(startDate));
            }
            if (endDate != null) {
                queryDTO.setEndDate(LocalDate.parse(endDate));
            }
            queryDTO.setMinMoodScore(minMoodScore);
            queryDTO.setMaxMoodScore(maxMoodScore);
            queryDTO.setDominantEmotion(dominantEmotion);

            Page<EmotionDiaryResponseDTO> page = emotionDiaryService.selectAdminPage(queryDTO);
            return Result.success(page);
        } catch (Exception e) {
            log.error("管理员分页查询情绪日记失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 管理员获取情绪日记统计数据
     */
    @Operation(summary = "管理员获取情绪统计数据", description = "获取全局情绪日记统计分析数据")
    @GetMapping("/admin/statistics")
    public Result<EmotionDiaryStatisticsDTO> getAdminStatistics(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "30") Integer days,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {
        log.info("管理员收到获取情绪统计数据请求，统计天数: {}, 用户ID: {}", days, userId);

        try {
            EmotionDiaryStatisticsDTO statistics = emotionDiaryService.getAdminStatistics(userId, days);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("管理员获取情绪统计数据失败: {}", e.getMessage(), e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 管理员删除情绪日记
     */
    @Operation(summary = "管理员删除情绪日记", description = "管理员删除指定的情绪日记记录")
    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDeleteDiary(@Parameter(description = "日记ID") @PathVariable Long id) {
        log.info("管理员收到删除情绪日记请求，ID: {}", id);

        try {
            emotionDiaryService.adminDeleteDiary(id);
            return Result.success();
        } catch (Exception e) {
            log.error("管理员删除情绪日记失败: {}", e.getMessage(), e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 管理员获取系统概览统计
     */
    @Operation(summary = "管理员获取系统概览", description = "获取情绪日记模块的系统概览数据")
    @GetMapping("/admin/overview")
    public Result<EmotionDiaryStatisticsDTO> getAdminOverview() {
        log.info("管理员收到获取系统概览请求");

        try {
            EmotionDiaryStatisticsDTO overview = emotionDiaryService.getSystemOverview();
            return Result.success(overview);
        } catch (Exception e) {
            log.error("管理员获取系统概览失败: {}", e.getMessage(), e);
            return Result.error("获取概览数据失败: " + e.getMessage());
        }
    }

    /**
     * 管理员手动触发AI情绪分析
     */
    @Operation(summary = "管理员手动触发AI情绪分析", description = "管理员手动触发指定日记的AI情绪分析，支持重复分析")
    @PostMapping("/admin/{id}/ai-analysis")
    public Result<Void> adminTriggerAiEmotionAnalysis(
            @Parameter(description = "日记ID") @PathVariable Long id) {
        log.info("管理员手动触发AI情绪分析，日记ID: {}", id);

        try {
            // 管理员可以触发任何日记的AI分析，包括已分析过的
            emotionDiaryService.adminTriggerAiAnalysis(id);
            log.info("管理员已提交AI情绪分析任务到队列，日记ID: {}", id);
            return Result.success();
        } catch (BusinessException e) {
            log.warn("管理员触发AI情绪分析失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("管理员触发AI情绪分析异常: {}", e.getMessage(), e);
            return Result.error("触发分析失败: " + e.getMessage());
        }
    }

    /**
     * 管理员批量触发AI情绪分析
     */
    @Operation(summary = "管理员批量触发AI情绪分析", description = "管理员批量触发多个日记的AI情绪分析")
    @PostMapping("/admin/batch-ai-analysis")
    public Result<Map<String, Object>> adminBatchTriggerAiEmotionAnalysis(
            @Parameter(description = "日记ID列表") @RequestBody List<Long> diaryIds) {
        log.info("管理员批量触发AI情绪分析，日记数量: {}", diaryIds.size());

        if (diaryIds == null || diaryIds.isEmpty()) {
            return Result.error("日记ID列表不能为空");
        }

        if (diaryIds.size() > 100) {
            return Result.error("单次批量处理不能超过100条记录");
        }

        try {
            Map<String, Object> result = emotionDiaryService.adminBatchTriggerAiAnalysis(diaryIds);
            log.info("管理员批量AI分析任务已提交，成功: {}, 失败: {}", 
                    result.get("successCount"), result.get("failCount"));
            return Result.success(result);
        } catch (Exception e) {
            log.error("管理员批量触发AI情绪分析异常: {}", e.getMessage(), e);
            return Result.error("批量触发分析失败: " + e.getMessage());
        }
    }
}
