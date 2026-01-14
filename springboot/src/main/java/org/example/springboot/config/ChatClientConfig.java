package org.example.springboot.config;

import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class ChatClientConfig {
    public static final Integer MAX_MEMORY_MESSAGE_SIZE =30;
    public static final String DEFAULT_SYSTEM="你是一个专业的心理疏导师，温和耐心，善于倾听，能够提供专业的心理支持和建议。";

    /**
     * 配置ChatMemory - 内存存储的会话记忆
     *
     * @return ChatMemory 内存存储的会话记忆实例
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(MAX_MEMORY_MESSAGE_SIZE) // 窗口最大消息数目，保留最近30条消息
                .build();
    }



    @Bean("open-ai")
    //硅基流动
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel){

        return ChatClient.builder(openAiChatModel).
                defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory()).build()).
                defaultSystem(DEFAULT_SYSTEM).build();
    }
}
