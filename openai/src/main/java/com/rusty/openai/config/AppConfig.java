package com.rusty.openai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder){
        return chatClientBuilder.build();
    }

    @Bean
    public ChatClient rollClient(ChatClient.Builder chatClientBuilder){
        //LLM에 역할부여
        return chatClientBuilder.defaultSystem("당신은 교육 튜터입니다. 개념을 명확하고 간단하게 설명하세요").build();
    }

}
