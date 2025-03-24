package com.rusty.openai.service;

import com.rusty.openai.entiry.Answer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    @Autowired
    private final ChatClient chatClient;
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

    public String chatplace(String subject, String tone, String message) {
        return chatClient.prompt()
                .user(message)
                .system(sp->sp
                        .param("subject",subject)
                        .param("tone", tone)
                )
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText(); //getContent
    }

    public ChatResponse chatjson(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .chatResponse(); // ChatResponse(?)-->JSON
              /*  .getResult()
                .getOutput()
                .getContent();*/
    }

    public Answer chatobject(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .entity(Answer.class);
    }
}
