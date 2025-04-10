package com.rusty.openai.controller;

import com.rusty.openai.entiry.Answer;
import com.rusty.openai.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatController {

    //LLM과 통신하는 객체
    @Autowired
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam("message") String message) {
        return chatService.chat(message);
    }

    @GetMapping("/chatsys")
    public String chatSys(@RequestParam("message") String message) {
        return chatService.chatMessage(message);
    }
    @GetMapping("/chatplace")
    //                                            QuestionDTO
    public String chatplace(String subject, String tone, String message){
        return chatService.chatplace(subject, tone, message);
    }

    @GetMapping("/chatjson")
    public ChatResponse chatjson(String message){
        return chatService.chatjson(message);
    }

    @GetMapping("/chatobject")
    // {"answer" : "dsddsddsd" }
    public Answer chatobject(String message){
        return chatService.chatobject(message);
    }

    @GetMapping("/recipe")
    public Answer recipe(String foodName, String question){
        return chatService.recipe(foodName, question);
    }

    @GetMapping("/chatlist")
    public List<String> chatlist(String message){
        return chatService.chatlist(message);
    }



}
