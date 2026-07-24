package com.zzyl.agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAIConfig {

    /**
     * 创建并返回一个ChatClient的Spring Bean实例。
     * @param openAiChatModel
     * @return
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient
                .builder(openAiChatModel)
                .defaultSystem("您是智慧养老院的问答助手，主要职责是通过知识库解答与养老院服务相关的问题。")
                .build();
    }
}