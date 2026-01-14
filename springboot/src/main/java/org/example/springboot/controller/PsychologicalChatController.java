package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.AiService.PsychologicalSupportService;
import org.example.springboot.AiService.StructOutPut;
import org.example.springboot.common.Result;
import org.example.springboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.springboot.DTO.query.ConsultationSessionQueryDTO;
import org.example.springboot.DTO.response.ConsultationSessionResponseDTO;
import org.example.springboot.DTO.response.ConsultationMessageResponseDTO;
import org.example.springboot.enumClass.UserType;
import org.example.springboot.service.ConsultationSessionService;
import org.example.springboot.service.ConsultationMessageService;
import org.example.springboot.entity.ConsultationSession;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 流式心理疏导智能对话控制器
 * 提供基于Spring AI的流式心理疏导对话服务
 */
@Slf4j
@RestController
@RequestMapping("/psychological-chat")
@Tag(name = "流式心理疏导对话", description = "AI流式心理疏导智能对话机器人服务")
public class PsychologicalChatController {

    @Autowired
    private PsychologicalSupportService psychologicalSupportService;

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    /**
     * 开始新的心理疏导会话
     */
    @Operation(summary = "开始心理疏导会话", description = "为用户创建新的心理疏导对话会话")
    @PostMapping("/session/start")
    public Result<StructOutPut.StreamChatSession> startChatSession(@RequestBody ConsultationSessionCreateDTO createDTO) {
        log.info("收到开始心理疏导会话请求");
        
        try {
            // 获取当前用户
            Long userId = JwtTokenUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            StructOutPut.StreamChatSession session = psychologicalSupportService.startChatSession(userId, createDTO);
            
            log.info("心理疏导会话创建成功，会话ID: {}", session.sessionId());
            return Result.success(session);
            
        } catch (Exception e) {
            log.error("开始心理疏导会话失败: {}", e.getMessage(), e);
            return Result.error("创建会话失败: " + e.getMessage());
        }
    }

    /**
     * 流式心理疏导对话
     */
    @Operation(summary = "流式心理疏导对话", description = "支持实时流式AI心理疏导对话")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestBody StreamChatRequest request) {
        log.info("接收到流式心理疏导对话请求，会话ID: {}", request.sessionId());
        
        try {
            // 获取当前用户
            Long userHash = JwtTokenUtils.getCurrentUserId();
            if (userHash == null) {
                log.error("用户未登录！");
                return Flux.just(ServerSentEvent.<String>builder()
                                .event("error")
                                .data(toSseData(Result.error("用户未登录")))
                                .build());
            }

            // 开始流式对话
            return psychologicalSupportService.streamPsychologicalChat(request.sessionId(), request.userMessage())
                .map(fragment -> {
                    // 检查是否是风险警告消息
                    if (fragment.contains("⚠️") || fragment.contains("💡 建议:")) {
                        // 风险警告消息使用特殊事件类型
                        return ServerSentEvent.<String>builder()
                                .event("risk-warning")
                                .data(toSseData(Result.success(Map.of("content", fragment, "type", "risk"))))
                                .build();
                    } else {
                        // 正常消息
                        return ServerSentEvent.<String>builder()
                                .event("message")
                                .data(toSseData(Result.success(Map.of("content", fragment, "type", "normal"))))
                                .build();
                    }
                })
                .doOnSubscribe(subscription -> {
                    log.info("开始流式心理疏导对话，会话ID: {}", request.sessionId());
                })
                .doOnComplete(() -> {
                    log.info("流式心理疏导对话完成，会话ID: {}", request.sessionId());
                })
                .doOnError(error -> {
                    log.error("流式心理疏导对话异常: {}", error.getMessage(), error);
                })
                .onErrorReturn(ServerSentEvent.<String>builder()
                               .event("error")
                               .data(toSseData(Result.error("对话服务异常: 系统繁忙，请稍后重试")))
                               .build())
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                                      .event("done")
                                      .data("{}")
                                      .build())) // 结束事件
                .delayElements(Duration.ofMillis(50)); // 添加小延迟以确保流式体验
                
        } catch (Exception e) {
            log.error("流式心理疏导对话初始化失败: {}", e.getMessage(), e);
            return Flux.just(ServerSentEvent.<String>builder()
                                .event("error")
                                .data(toSseData(Result.error("对话初始化失败: " + e.getMessage())))
                                .build());
        }
    }

    /**
     * 获取会话情绪分析结果
     */
    @Operation(summary = "获取会话情绪分析", description = "获取指定会话的最新情绪分析结果")
    @GetMapping("/session/{sessionId}/emotion")
    public Result<StructOutPut.EmotionAnalysisResult> getSessionEmotion(@PathVariable String sessionId) {
        log.info("获取会话情绪状态，会话ID: {}", sessionId);
        try {
            Long userId = JwtTokenUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 解析会话ID
            Long dbSessionId = psychologicalSupportService.extractSessionId(sessionId);
            if (dbSessionId == null) {
                return Result.error("无效的会话ID格式");
            }

            // 获取会话信息
            ConsultationSession session = consultationSessionService.getSessionById(dbSessionId);
            if (session == null) {
                return Result.error("会话不存在");
            }

            // 验证会话所有者
            if (!session.getUserId().equals(userId)) {
                return Result.error("无权访问此会话");
            }

            // 构建情绪分析结果
            StructOutPut.EmotionAnalysisResult emotionResult;
            if (session.getLastEmotionAnalysis() != null) {
                try {
                    // 解析JSON数据
                    cn.hutool.json.JSONObject emotionJson = cn.hutool.json.JSONUtil.parseObj(session.getLastEmotionAnalysis());
                    
                    emotionResult = new StructOutPut.EmotionAnalysisResult(
                        emotionJson.getStr("primaryEmotion", "平静"),
                        emotionJson.getInt("emotionScore", 50),
                        emotionJson.getBool("isNegative", false),
                        emotionJson.getInt("riskLevel", 0),
                        emotionJson.getJSONArray("keywords") != null ? 
                            emotionJson.getJSONArray("keywords").toList(String.class) : null,
                        emotionJson.getStr("suggestion", "保持现状"),
                        emotionJson.getStr("icon", "😐"),
                        emotionJson.getStr("label", "平静"),
                        emotionJson.getStr("riskDescription", "情绪状态稳定"),
                        emotionJson.getJSONArray("improvementSuggestions") != null ? 
                            emotionJson.getJSONArray("improvementSuggestions").toList(String.class) : 
                            List.of("保持现状"),
                        emotionJson.getLong("timestamp", System.currentTimeMillis())
                    );
                    log.info("成功获取会话情绪状态，emotion: {}, riskLevel: {}", 
                            emotionResult.primaryEmotion(), emotionResult.riskLevel());
                } catch (Exception e) {
                    log.warn("解析情绪分析JSON失败: {}, 使用默认值", e.getMessage());
                    emotionResult = psychologicalSupportService.getDefaultEmotionAnalysis();
                }
            } else {
                // 如果数据库中没有情绪分析数据，返回默认值
                emotionResult = psychologicalSupportService.getDefaultEmotionAnalysis();
                log.info("暂无情绪分析数据，返回默认情绪状态");
            }

            return Result.success(emotionResult);

        } catch (Exception e) {
            log.error("获取会话情绪状态失败: {}", e.getMessage(), e);
            return Result.error("获取情绪状态失败: " + e.getMessage());
        }
    }




    // ==================== 管理功能接口 ====================

    /**
     * 分页查询咨询会话
     */
    @Operation(summary = "分页查询咨询会话", description = "分页查询用户咨询会话记录")
    @GetMapping("/sessions")
    public Result<Page<ConsultationSessionResponseDTO>> getSessionsPage(ConsultationSessionQueryDTO queryDTO) {
        log.info("分页查询咨询会话，查询条件: {}", queryDTO);
        
        try {
            // 获取当前用户信息
            Long currentUserId = JwtTokenUtils.getCurrentUserId();
            Integer currentUserType = JwtTokenUtils.getCurrentUserRole();
            
            // 权限控制：普通用户只能查看自己的会话，管理员可以查看所有会话
            log.info("currentUserType:{}", currentUserType);
            if (!UserType.ADMIN.getCode().equals(currentUserType)) {
                // 普通用户强制只能查看自己的会话
                queryDTO.setUserId(currentUserId);
            }
            // 管理员不设置userId限制，可以查看所有用户的会话
            
            Page<ConsultationSessionResponseDTO> page = consultationSessionService.selectPage(queryDTO);
            return Result.success(page);
            
        } catch (Exception e) {
            log.error("分页查询咨询会话失败: {}", e.getMessage(), e);
            return Result.error("查询会话失败: " + e.getMessage());
        }
    }

    /**
     * 获取会话详情
     */
    @Operation(summary = "获取会话详情", description = "根据会话ID获取详细信息")
    @GetMapping("/sessions/{sessionId}")
    public Result<ConsultationSessionResponseDTO> getSessionDetail(@PathVariable Long sessionId) {
        log.info("获取会话详情，会话ID: {}", sessionId);
        
        try {
            ConsultationSessionResponseDTO session = consultationSessionService.getSessionDetail(sessionId);
            return Result.success(session);
            
        } catch (Exception e) {
            log.error("获取会话详情失败: {}", e.getMessage(), e);
            return Result.error("获取会话详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取会话消息列表
     */
    @Operation(summary = "获取会话消息列表", description = "获取指定会话的所有消息")
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ConsultationMessageResponseDTO>> getSessionMessages(@PathVariable Long sessionId) {
        log.info("获取会话消息列表，会话ID: {}", sessionId);
        
        try {
            List<ConsultationMessageResponseDTO> messages = consultationMessageService.getMessagesBySessionId(sessionId);
            return Result.success(messages);
            
        } catch (Exception e) {
            log.error("获取会话消息列表失败: {}", e.getMessage(), e);
            return Result.error("获取消息列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除咨询会话
     */
    @Operation(summary = "删除咨询会话", description = "删除指定的咨询会话及其相关消息")
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Boolean> deleteSession(@PathVariable Long sessionId) {
        log.info("删除咨询会话，会话ID: {}", sessionId);
        
        try {
            // 获取当前用户
            Long userId = JwtTokenUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = consultationSessionService.deleteSession(sessionId, userId);
            
            if (success) {
                log.info("咨询会话删除成功，会话ID: {}", sessionId);
                return Result.success(true);
            } else {
                return Result.error("删除会话失败");
            }
            
        } catch (Exception e) {
            log.error("删除咨询会话失败: {}", e.getMessage(), e);
            return Result.error("删除会话失败: " + e.getMessage());
        }
    }

    /**
     * 更新会话标题
     */
    @Operation(summary = "更新会话标题", description = "更新指定咨询会话的标题")
    @PutMapping("/sessions/{sessionId}/title")
    public Result<Boolean> updateSessionTitle(@PathVariable Long sessionId, @RequestBody UpdateSessionTitleRequest request) {
        log.info("更新会话标题，会话ID: {}, 新标题: {}", sessionId, request.sessionTitle());
        
        try {
            // 获取当前用户
            Long userId = JwtTokenUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = consultationSessionService.updateSessionTitle(sessionId, userId, request.sessionTitle());
            
            if (success) {
                log.info("会话标题更新成功，会话ID: {}", sessionId);
                return Result.success(true);
            } else {
                return Result.error("更新标题失败");
            }
            
        } catch (Exception e) {
            log.error("更新会话标题失败: {}", e.getMessage(), e);
            return Result.error("更新标题失败: " + e.getMessage());
        }
    }

    /**
     * 转换为SSE数据格式
     */
    private String toSseData(Object data) {
        try {
            return cn.hutool.json.JSONUtil.toJsonStr(data);
        } catch (Exception e) {
            log.error("转换SSE数据失败: {}", e.getMessage(), e);
            return "{\"code\":500,\"message\":\"数据格式化失败\"}";
        }
    }


    /**
     * 流式对话请求DTO
     */
    public record StreamChatRequest(
        String sessionId,      // 会话ID
        String userMessage     // 用户消息
    ) {}

    /**
     * 更新会话标题请求DTO
     */
    public record UpdateSessionTitleRequest(
        String sessionTitle    // 会话标题
    ) {}

} 