package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.query.AiAnalysisTaskQueryDTO;
import org.example.springboot.DTO.response.AiAnalysisTaskResponseDTO;
import org.example.springboot.common.Result;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.service.AiAnalysisTaskService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * AI分析任务管理控制器
 * @author system
 */
@Tag(name = "AI分析任务管理", description = "AI情绪分析任务队列管理接口")
@Slf4j
@RestController
@RequestMapping("/ai-analysis-task")
public class AiAnalysisTaskController {

    @Resource
    private AiAnalysisTaskService aiAnalysisTaskService;

    /**
     * 分页查询AI分析任务
     */
    @Operation(summary = "分页查询AI分析任务", description = "管理员查看AI分析任务队列")
    @GetMapping("/page")
    public Result<Page<AiAnalysisTaskResponseDTO>> getTaskPage(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "任务状态") @RequestParam(required = false) String status,
            @Parameter(description = "任务类型") @RequestParam(required = false) String taskType,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "优先级") @RequestParam(required = false) Integer priority,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime,
            @Parameter(description = "是否只显示失败任务") @RequestParam(required = false) Boolean failedOnly,
            @Parameter(description = "是否只显示可重试任务") @RequestParam(required = false) Boolean retryableOnly) {
        
        // 构建查询DTO
        AiAnalysisTaskQueryDTO queryDTO = new AiAnalysisTaskQueryDTO();
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        queryDTO.setStatus(status);
        queryDTO.setTaskType(taskType);
        queryDTO.setUserId(userId);
        queryDTO.setUsername(username);
        queryDTO.setPriority(priority);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        queryDTO.setFailedOnly(failedOnly);
        queryDTO.setRetryableOnly(retryableOnly);
        
        log.info("收到分页查询AI分析任务请求，查询条件: {}", queryDTO);
        log.info("原始参数 - priority: {}, 类型: {}", priority, priority != null ? priority.getClass().getSimpleName() : "null");

        try {
            Page<AiAnalysisTaskResponseDTO> page = aiAnalysisTaskService.getTaskPage(queryDTO);
            return Result.success(page);
        } catch (Exception e) {
            log.error("分页查询AI分析任务失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取队列统计信息
     */
    @Operation(summary = "获取队列统计信息", description = "获取AI分析队列的统计数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getQueueStatistics() {
        log.info("收到获取队列统计信息请求");

        try {
            Map<String, Object> statistics = aiAnalysisTaskService.getQueueStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取队列统计信息失败: {}", e.getMessage(), e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 重试失败的任务
     */
    @Operation(summary = "重试失败的任务", description = "重新执行失败的AI分析任务")
    @PostMapping("/{taskId}/retry")
    public Result<Void> retryTask(@Parameter(description = "任务ID") @PathVariable Long taskId) {
        log.info("收到重试任务请求，任务ID: {}", taskId);

        try {
            aiAnalysisTaskService.retryTask(taskId);
            return Result.success();
        } catch (BusinessException e) {
            log.warn("重试任务失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("重试任务异常: {}", e.getMessage(), e);
            return Result.error("重试失败: " + e.getMessage());
        }
    }

    /**
     * 批量重试失败的任务
     */
    @Operation(summary = "批量重试失败的任务", description = "批量重新执行失败的AI分析任务")
    @PostMapping("/batch-retry")
    public Result<Map<String, Object>> batchRetryTasks(
            @Parameter(description = "任务ID列表") @RequestBody List<Long> taskIds) {
        log.info("收到批量重试任务请求，任务数量: {}", taskIds.size());

        if (taskIds == null || taskIds.isEmpty()) {
            return Result.error("任务ID列表不能为空");
        }

        if (taskIds.size() > 50) {
            return Result.error("单次批量重试不能超过50个任务");
        }

        try {
            Map<String, Object> result = aiAnalysisTaskService.batchRetryTasks(taskIds);
            log.info("批量重试任务完成，成功: {}, 失败: {}", 
                    result.get("successCount"), result.get("failCount"));
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量重试任务异常: {}", e.getMessage(), e);
            return Result.error("批量重试失败: " + e.getMessage());
        }
    }
}

