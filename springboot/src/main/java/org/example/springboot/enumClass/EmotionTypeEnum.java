package org.example.springboot.enumClass;

import lombok.Getter;

/**
 * 情绪类型枚举
 * 用于AI情绪分析的标准化情绪分类
 * 
 * @author system
 * @since 2025-01-18
 */
@Getter
public enum EmotionTypeEnum {
    
    // ========== 正面情绪 ==========
    HAPPY("开心", "positive", "😊", "积极愉悦的情绪状态", "#FBBF24"),
    EXCITED("兴奋", "positive", "😄", "充满活力和热情", "#F59E0B"),
    GRATEFUL("感激", "positive", "🥺", "对他人或事物的感谢之情", "#34D399"),
    LOVE("喜爱", "positive", "❤️", "对人或事物的喜爱和热爱", "#34D399"),
    SATISFIED("满足", "positive", "😌", "对现状的满意和安逸", "#10B981"),
    PEACEFUL("平静", "positive", "🙂", "内心宁静安详的状态", "#60A5FA"),
    HOPEFUL("希望", "positive", "✨", "对未来的积极期待", "#A78BFA"),
    
    // ========== 负面情绪 ==========
    ANGRY("愤怒", "negative", "😠", "强烈的不满和敌意", "#DC2626"),
    SAD("悲伤", "negative", "😢", "内心痛苦和失落", "#EF4444"),
    ANXIOUS("焦虑", "negative", "😰", "对未来的担忧和不安", "#F59E0B"),
    FEARFUL("恐惧", "negative", "😨", "对危险或威胁的害怕", "#7C3AED"),
    DISAPPOINTED("失望", "negative", "😞", "期望落空的失落感", "#F472B6"),
    LONELY("孤独", "negative", "😶", "缺乏陪伴和理解的孤单", "#8B5CF6"),
    TROUBLED("困扰", "negative", "😔", "内心的困扰和烦恼", "#F472B6"),
    DESPERATE("绝望", "negative", "😵", "完全失去希望的极度痛苦", "#991B1B"),
    
    // ========== 中性情绪 ==========
    CONFUSED("困惑", "neutral", "🤔", "对事物的不理解和疑惑", "#6B7280"),
    BORED("无聊", "neutral", "😐", "缺乏兴趣和刺激的状态", "#9CA3AF"),
    THOUGHTFUL("思考", "neutral", "💭", "深入思考和反思的状态", "#64748B"),
    NOSTALGIC("回忆", "neutral", "🧠", "对过去的回想和怀念", "#71717A");
    
    /**
     * 情绪名称（中文）
     */
    private final String emotionName;
    
    /**
     * 情绪类别：positive(正面)、negative(负面)、neutral(中性)
     */
    private final String category;
    
    /**
     * 情绪图标
     */
    private final String icon;
    
    /**
     * 情绪描述
     */
    private final String description;
    
    /**
     * 情绪颜色（用于3D可视化）
     */
    private final String color;
    
    EmotionTypeEnum(String emotionName, String category, String icon, String description, String color) {
        this.emotionName = emotionName;
        this.category = category;
        this.icon = icon;
        this.description = description;
        this.color = color;
    }
    
    /**
     * 根据情绪名称获取枚举
     * 
     * @param emotionName 情绪名称
     * @return 对应的枚举，如果不存在则返回null
     */
    public static EmotionTypeEnum getByEmotionName(String emotionName) {
        if (emotionName == null || emotionName.trim().isEmpty()) {
            return null;
        }
        
        for (EmotionTypeEnum emotion : values()) {
            if (emotion.getEmotionName().equals(emotionName.trim())) {
                return emotion;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为正面情绪
     * 
     * @return true-正面情绪，false-非正面情绪
     */
    public boolean isPositive() {
        return "positive".equals(this.category);
    }
    
    /**
     * 判断是否为负面情绪
     * 
     * @return true-负面情绪，false-非负面情绪
     */
    public boolean isNegative() {
        return "negative".equals(this.category);
    }
    
    /**
     * 判断是否为中性情绪
     * 
     * @return true-中性情绪，false-非中性情绪
     */
    public boolean isNeutral() {
        return "neutral".equals(this.category);
    }
    
    /**
     * 获取所有正面情绪的名称列表
     * 
     * @return 正面情绪名称数组
     */
    public static String[] getPositiveEmotions() {
        return new String[]{"开心", "兴奋", "感激", "喜爱", "满足", "平静", "希望"};
    }
    
    /**
     * 获取所有负面情绪的名称列表
     * 
     * @return 负面情绪名称数组
     */
    public static String[] getNegativeEmotions() {
        return new String[]{"愤怒", "悲伤", "焦虑", "恐惧", "失望", "孤独", "困扰", "绝望"};
    }
    
    /**
     * 获取所有中性情绪的名称列表
     * 
     * @return 中性情绪名称数组
     */
    public static String[] getNeutralEmotions() {
        return new String[]{"困惑", "无聊", "思考", "回忆"};
    }
    
    /**
     * 获取所有情绪名称列表
     * 
     * @return 所有情绪名称数组
     */
    public static String[] getAllEmotionNames() {
        EmotionTypeEnum[] emotions = values();
        String[] names = new String[emotions.length];
        for (int i = 0; i < emotions.length; i++) {
            names[i] = emotions[i].getEmotionName();
        }
        return names;
    }
    
    /**
     * 根据情绪名称获取颜色
     * 
     * @param emotionName 情绪名称
     * @return 对应的颜色值，如果不存在则返回默认颜色
     */
    public static String getColorByType(String emotionName) {
        EmotionTypeEnum emotion = getByEmotionName(emotionName);
        return emotion != null ? emotion.getColor() : "#6B7280"; // 默认灰色
    }
}
