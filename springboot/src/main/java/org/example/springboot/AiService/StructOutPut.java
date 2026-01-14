package org.example.springboot.AiService;

import java.util.List;

public class StructOutPut {

    // 结构化输出参考，使用Record
//   public record LearningPathResult(
//        String pathTitle,
//        String learningGoal,
//
//    ){}

    /**
     * 内容审核结果
     * @param isPass 是否通过审核
     * @param riskLevel 风险等级: 0-安全，1-低风险，2-中风险，3-高风险
     * @param violationTypes 违规类型列表：如色情、暴力、政治敏感、恶意攻击等
     * @param riskScore 风险评分 0.0-1.0，分数越高风险越大
     * @param suggestion 审核建议：通过、人工复审、直接拦截
     */
    public record ContentAuditResult(
            boolean isPass,
            int riskLevel,
            List<String> violationTypes,
            double riskScore,
            String suggestion,
            String reason
    ) {}

    /**
     * 情绪分析结果
     * @param primaryEmotion 主要情绪类型
     * @param emotionScore 情绪强度评分 0-100
     * @param isNegative 是否为负面情绪
     * @param riskLevel 心理风险等级：0-正常，1-关注，2-预警，3-危机（仅当isNegative为true时有效）
     * @param keywords 情绪关键词
     * @param suggestion 专业建议
     * @param icon 情绪图标
     * @param label 情绪标签
     * @param riskDescription 风险描述
     * @param improvementSuggestions 改善建议列表
     * @param timestamp 分析时间戳
     */
    public record EmotionAnalysisResult(
            String primaryEmotion,
            int emotionScore,
            boolean isNegative,
            int riskLevel,
            List<String> keywords,
            String suggestion,
            String icon,
            String label,
            String riskDescription,
            List<String> improvementSuggestions,
            long timestamp
    ) {}

    /**
     * 敏感词检测结果
     * @param hasSensitiveWords 是否包含敏感词
     * @param sensitiveWords 检测到的敏感词列表
     * @param severity 严重程度：low、medium、high
     * @param filteredContent 过滤后的内容（敏感词用*替换）
     */
    public record SensitiveWordResult(
            boolean hasSensitiveWords,
            List<String> sensitiveWords,
            String severity,
            String filteredContent
    ) {}

    /**
     * 流式心理疏导对话会话信息
     * @param sessionId 会话ID
     * @param userHash 用户哈希（匿名用户标识）
     * @param initialMessage 初始消息
     * @param startTime 会话开始时间
     * @param expiryTime 会话过期时间
     * @param status 会话状态（ACTIVE、ENDED等）
     * @param emotionHistory 情绪历史记录
     * @param messageCount 消息数量
     */
    public record StreamChatSession(
            String sessionId,
            Long userHash,
            String initialMessage,
            Long startTime,
            Long expiryTime,
            String status,
            List<EmotionAnalysisResult> emotionHistory,
            Integer messageCount
    ) {}


    /**
     * 会话管理请求
     * @param action 操作类型：START, CONTINUE, END
     * @param sessionId 会话ID（继续或结束时需要）
     * @param userMessage 用户消息
     * @param contextLimit 上下文限制（保留最近N条消息）
     */
    public record SessionManagementRequest(
            String action,
            String sessionId,
            String userMessage,
            Integer contextLimit
    ) {}

    /**
     * 阿里云智能对话机器人流式访问Token信息
     * @param accessToken 认证Token
     * @param channelId 流式通道ID
     * @param expiresAt Token过期时间
     */
    public record AliyunChatbotAccessToken(
            String accessToken,
            String channelId,
            Long expiresAt
    ) {}

    /**
     * 阿里云智能对话机器人请求参数
     * @param instanceId 机器人ID
     * @param utterance 用户输入内容
     * @param sessionId 会话ID（可选）
     * @param senderId 发送者ID（可选）
     * @param senderNick 发送者昵称（可选）
     * @param vendorParam 自定义参数（可选）
     * @param perspective 视角编码（可选）
     * @param sandBox 是否测试环境
     * @param command 请求指令（可选）
     */
    public record AliyunChatbotRequest(
            String instanceId,
            String utterance,
            String sessionId,
            String senderId,
            String senderNick,
            String vendorParam,
            List<String> perspective,
            Boolean sandBox,
            String command
    ) {}

    /**
     * 阿里云智能对话机器人响应结果
     * @param messageId 消息ID
     * @param sessionId 会话ID
     * @param sequenceId 序列ID
     * @param source 答案来源
     * @param streamEnd 流式返回是否结束
     * @param messageBody 消息体
     */
    public record AliyunChatbotResponse(
            String messageId,
            String sessionId,
            String sequenceId,
            String source,
            Boolean streamEnd,
            AliyunMessageBody messageBody
    ) {}

    /**
     * 阿里云智能对话机器人消息体
     * @param type 答案类型：Direct-直出，Clarify-反问或澄清
     * @param directMessageBody 直出消息体
     * @param clarifyMessageBody 澄清消息体
     * @param commands 指令参数
     */
    public record AliyunMessageBody(
            String type,
            AliyunDirectMessageBody directMessageBody,
            AliyunClarifyMessageBody clarifyMessageBody,
            Object commands
    ) {}

    /**
     * 阿里云智能对话机器人直出消息体
     * @param contentType 内容类型：PLAIN_TEXT-纯文本，RICH_TEXT-富文本
     * @param sentenceList 答案段落列表
     * @param hitSystemAskConfig 命中的问答策略配置
     * @param answerReference 答案引用内容
     * @param ext 扩展信息
     * @param relatedQuestionList 关联问题列表
     */
    public record AliyunDirectMessageBody(
            String contentType,
            List<AliyunSentence> sentenceList,
            String hitSystemAskConfig,
            Object answerReference,
            Object ext,
            List<Object> relatedQuestionList
    ) {}

    /**
     * 阿里云智能对话机器人澄清消息体
     * @param clarifyContent 澄清的完整回复
     * @param title 澄清的标题
     * @param clarifyList 澄清内容列表
     */
    public record AliyunClarifyMessageBody(
            String clarifyContent,
            String title,
            List<Object> clarifyList
    ) {}

    /**
     * 阿里云智能对话机器人答案段落
     * @param content 答案段落内容
     * @param referNumber 段落序号
     */
    public record AliyunSentence(
            String content,
            Integer referNumber
    ) {}

}
