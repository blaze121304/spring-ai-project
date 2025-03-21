package com.rusty.openai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    @Autowired
    private final ChatClient chatClient;
    @Autowired
    private final ChatClient rollClient;
    

    public ChatService(ChatClient chatClient, ChatClient rollClient) {
        this.chatClient = chatClient;
        this.rollClient = rollClient;
    }

    public String chat(String message) {
        return chatClient.prompt().user(message).call().content();
    }

    public String chatMessage(String message) {
        return rollClient.prompt()
                .user(message)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
    }
}
