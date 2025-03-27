package com.rusty.openai.controller;

import com.rusty.openai.service.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AskAIController {

    private final ChatService chatService;
    public AskAIController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/ask")
    public  String getResponse(@RequestParam("message") String message){
        return chatService.getResponse(message);
    }

    @GetMapping("/ask-ai")
    public  String getResponseOptions(@RequestParam("message") String message){
        return chatService.getResponseOptions(message);
    }
}
