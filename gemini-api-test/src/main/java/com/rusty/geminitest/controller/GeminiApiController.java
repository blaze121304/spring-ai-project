package com.rusty.geminitest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.rusty.geminitest.domain.dto.EmoRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.rusty.geminitest.service.GeminiApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;

@RestController
@RequestMapping("/api/gemini")
public class GeminiApiController {

    private final Logger log  = LogManager.getLogger(GeminiApiController.class);
    private final GeminiApiService geminiApiService;

    @Autowired
    public GeminiApiController(GeminiApiService geminiApiService) {
        this.geminiApiService = geminiApiService;
    }

    @Value("${gemini.prompt}")
    private String prompt;

    @Value("${vertex.ai.project.id}")
    private String projectId;

    @Value("${vertex.ai.region}")
    private String region;

    @Value("${vertex.ai.model}")
    private String model;

    @PostMapping("/pimage")
    public ResponseEntity<HashMap<String, Object>> processImage(@RequestParam("file") MultipartFile file, @RequestParam("prompt") String prompt) {
        log.info("\n\nINSIDE CLASS == GeminiApiController, METHOD == processImage(); ");

        try {
            JsonNode result = geminiApiService.getResponse(file, prompt);

            if(result != null) {
                log.info("\nImage processed successfully.");
                log.info("\nEXITING METHOD == processImage() OF CLASS == GeminiApiController \n\n");
                return getResponseFormat(HttpStatus.OK, "Image processed successfully", result);
            } else {
                log.info("\nImage processing failed.");
                log.info("\nEXITING METHOD == processImage() OF CLASS == GeminiApiController \n\n");
                return getResponseFormat(HttpStatus.INTERNAL_SERVER_ERROR, "Image processing failed", null);
            }
        } catch (Exception e) {
            log.warn("\nError in processImage() method of GeminiApiController: " + e.getMessage());
            log.info("\nEXITING METHOD == processImage() OF CLASS == GeminiApiController \n\n");
            return getResponseFormat(HttpStatus.INTERNAL_SERVER_ERROR, "Critical Error: " + e.getLocalizedMessage(), null);
        }
    }


    public ResponseEntity<HashMap<String, Object>> getResponseFormat(HttpStatus status, String message, Object data) {
        int responseStatus = (status.equals(HttpStatus.OK)) ? 1 : 0;

        HashMap<String, Object> map = new HashMap<>();
        map.put("responseCode", responseStatus);
        map.put("message", message);
        map.put("data", data);
        return ResponseEntity.status(status).body(map);

    }


    @GetMapping("/emotion")
    public String emotionTell(EmoRequest emoRequest) throws Exception {
        String response;
//        emoRequest.setPrompt(prompt);
        emoRequest.setPrompt("why sky is blue");
        response = geminiApiService.helpEmotion(emoRequest);
        return response;
    }

    @GetMapping("/text")
    public void chatText() throws Exception {
        String response;
        geminiApiService.chatDiscussion(projectId,region,model);
    }

}
