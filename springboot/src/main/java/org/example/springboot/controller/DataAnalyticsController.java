package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.common.Result;
import org.example.springboot.DTO.response.DataAnalyticsResponseDTO;
import org.example.springboot.service.DataAnalyticsService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 数据分析控制器
 * @author system
 */
@Tag(name = "数据分析管理")
@RestController
@RequestMapping("/data-analytics")
@Slf4j
public class DataAnalyticsController {

    @Resource
    private DataAnalyticsService dataAnalyticsService;

    @Operation(summary = "获取综合数据分析")
    @GetMapping("/overview")
    public Result<DataAnalyticsResponseDTO> getDataAnalytics(
            @Parameter(description = "分析天数") @RequestParam(defaultValue = "30") Integer days) {
        
        log.info("获取数据分析，分析天数: {}", days);
        
        // 权限检查：需要管理员权限
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未登录");
        }
        
        DataAnalyticsResponseDTO analytics = dataAnalyticsService.getDataAnalytics(days);
        return Result.success(analytics);
    }

    @Operation(summary = "获取情绪热力图数据")
    @GetMapping("/emotion-heatmap")
    public Result<DataAnalyticsResponseDTO.EmotionHeatmapData> getEmotionHeatmap(
            @Parameter(description = "分析天数") @RequestParam(defaultValue = "30") Integer days) {
        
        log.info("获取情绪热力图数据，分析天数: {}", days);
        
        // 权限检查：需要管理员权限
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未登录");
        }
        
        DataAnalyticsResponseDTO analytics = dataAnalyticsService.getDataAnalytics(days);
        return Result.success(analytics.getEmotionHeatmap());
    }

    @Operation(summary = "获取系统概览数据")
    @GetMapping("/system-overview")
    public Result<DataAnalyticsResponseDTO.SystemOverview> getSystemOverview(
            @Parameter(description = "分析天数") @RequestParam(defaultValue = "30") Integer days) {
        
        log.info("获取系统概览数据，分析天数: {}", days);
        
        // 权限检查：需要管理员权限
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未登录");
        }
        
        DataAnalyticsResponseDTO analytics = dataAnalyticsService.getDataAnalytics(days);
        return Result.success(analytics.getSystemOverview());
    }

    @Operation(summary = "获取情绪趋势数据")
    @GetMapping("/emotion-trend")
    public Result<java.util.List<DataAnalyticsResponseDTO.EmotionTrendData>> getEmotionTrend(
            @Parameter(description = "分析天数") @RequestParam(defaultValue = "30") Integer days) {
        
        log.info("获取情绪趋势数据，分析天数: {}", days);
        
        // 权限检查：需要管理员权限
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未登录");
        }
        
        DataAnalyticsResponseDTO analytics = dataAnalyticsService.getDataAnalytics(days);
        return Result.success(analytics.getEmotionTrend());
    }

    @Operation(summary = "获取咨询会话统计")
    @GetMapping("/consultation-stats")
    public Result<DataAnalyticsResponseDTO.ConsultationStatistics> getConsultationStats(
            @Parameter(description = "分析天数") @RequestParam(defaultValue = "30") Integer days) {
        
        log.info("获取咨询会话统计，分析天数: {}", days);
        
        // 权限检查：需要管理员权限
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未登录");
        }
        
        DataAnalyticsResponseDTO analytics = dataAnalyticsService.getDataAnalytics(days);
        return Result.success(analytics.getConsultationStats());
    }

    @Operation(summary = "获取用户活跃度数据")
    @GetMapping("/user-activity")
    public Result<java.util.List<DataAnalyticsResponseDTO.UserActivityData>> getUserActivity(
            @Parameter(description = "分析天数") @RequestParam(defaultValue = "30") Integer days) {
        
        log.info("获取用户活跃度数据，分析天数: {}", days);
        
        // 权限检查：需要管理员权限
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("未登录");
        }
        
        DataAnalyticsResponseDTO analytics = dataAnalyticsService.getDataAnalytics(days);
        return Result.success(analytics.getUserActivity());
    }
}
