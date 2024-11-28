package com.rusty.geminitest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.rusty.geminitest.controller.GeminiApiController;
import com.rusty.geminitest.domain.dto.EmoRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class GeminiApiService {

    @Value("${google.api.key}")
    private String apiKey;

    @Value("${vertex.ai.region}")
    private String region;

    @Value("${vertex.ai.project.id}")
    private String projectId;

    @Value("${vertex.ai.model}")
    private String modelName;

    private final Logger log  = LogManager.getLogger(GeminiApiService.class);
    public JsonNode getResponse(MultipartFile file, String prompt) throws Exception {
        String fileUri = uploadFileToGoogle(file);
        return generateContent(fileUri, prompt);
    }

    private String uploadFileToGoogle(MultipartFile file) throws Exception {
        String uploadUrl = "https://generativelanguage.googleapis.com/upload/v1beta/files?key=" + apiKey;

        HttpHeaders startHeaders = new HttpHeaders();
        startHeaders.set("X-Goog-Upload-Protocol", "resumable");
        startHeaders.set("X-Goog-Upload-Command", "start");
        startHeaders.set("X-Goog-Upload-Header-Content-Length", String.valueOf(file.getSize()));
        startHeaders.set("X-Goog-Upload-Header-Content-Type", file.getContentType());
        startHeaders.setContentType(MediaType.APPLICATION_JSON);

        String jsonBody = "{\"file\": {\"display_name\": \"" + file.getOriginalFilename() + "\"}}";

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> startRequest = new HttpEntity<>(jsonBody, startHeaders);
        ResponseEntity<String> startResponse = restTemplate.exchange(uploadUrl, HttpMethod.POST, startRequest, String.class);

        String sessionUri = startResponse.getHeaders().getFirst("X-Goog-Upload-URL");
        if (sessionUri == null || sessionUri.isEmpty()) {
            throw new Exception("Failed to obtain upload session URI.");
        }

        HttpHeaders uploadHeaders = new HttpHeaders();
        uploadHeaders.set("X-Goog-Upload-Protocol", "resumable");
        uploadHeaders.set("X-Goog-Upload-Command", "upload, finalize");
        uploadHeaders.set("X-Goog-Upload-Offset", "0");
        uploadHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> uploadRequest = new HttpEntity<>(file.getBytes(), uploadHeaders);
        ResponseEntity<String> uploadResponse = restTemplate.exchange(sessionUri, HttpMethod.POST, uploadRequest, String.class);

        return new ObjectMapper().readTree(uploadResponse.getBody()).path("file").path("uri").asText();
    }

    //https://cloud.google.com/vertex-ai/docs/reference/rest/v1/projects.locations.publishers.models/generateContent 참조
    public JsonNode generateContent(String fileUri, String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        String requestBody = "{ \"contents\": [ { \"role\": \"user\", \"parts\": [ { \"fileData\": { \"fileUri\": \"" + fileUri + "\" } }, { \"text\": \"" + prompt + "\" } ] } ] }";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        return new ObjectMapper().readTree(response.getBody());
    }


    //https://cloud.google.com/vertex-ai/generative-ai/docs/model-reference/gemini?hl=ko 참조
    public String helpEmotion(EmoRequest emoRequest) {

        // Initialize client that will be used to send requests.
        // This client only needs to be created once, and can be reused for multiple requests.
        try (VertexAI vertexAI = new VertexAI(projectId, region)) {
            String output;
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            // Send the question to the model for processing.
            GenerateContentResponse response = model.generateContent(emoRequest.getPrompt());
            // Extract the generated text from the model's response.
            output = ResponseHandler.getText(response);
            return output;
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
