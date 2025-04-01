package com.rusty.openai.service;

import com.rusty.openai.entiry.Answer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    private final ChatClient chatClient;
    private final ChatClient rollClient;
    private final ChatModel chatModel;

    public ChatService(ChatClient chatClient, ChatClient rollClient, ChatModel chatModel) {
        this.chatClient = chatClient;
        this.rollClient = rollClient;
        this.chatModel = chatModel;
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

    private final String recipeTemplate= """
           Answer for {foodName} for {question} ?
           """;

    public Answer recipe(String foodName, String question) {
        return chatClient.prompt()
                .user(userSpec->userSpec.text(recipeTemplate)
                        .param("foodName", foodName)
                        .param("question", question)
                )
                .call()
                .entity(Answer.class);
    }

    public List<String> chatlist(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .entity(new ListOutputConverter(new DefaultConversionService()));
    }

    public String getResponse(String message){
        return chatModel.call(message);
    }

    public String getResponseOptions(String message){
        ChatResponse response = chatModel.call(
                new Prompt(
                        message,
                        OpenAiChatOptions.builder()
                                .withModel("gpt-4o")
                                .withTemperature(0.4)
                                .build()
                ));
        return response.getResult().getOutput().getContent();
    }
}
