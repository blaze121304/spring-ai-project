package com.rusty.geminitest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.FunctionDeclaration;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Schema;
import com.google.cloud.vertexai.api.Type;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.rusty.geminitest.controller.GeminiApiController;
import com.rusty.geminitest.domain.dto.EmoRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

@Service
public class GeminiApiService {

    @Value("${google.api.key}")
    private String apiKey;

    @Value("${vertex.ai.region}")
    private String location;

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
    public String helpEmotion(EmoRequest emoRequest) throws Exception {

        ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(
                new FileInputStream("D:/gen-lang-client-0470512316-d584ef56f856.json"));

            try (VertexAI vertexAI = new VertexAI(projectId, location)) {

                FunctionDeclaration functionDeclaration = FunctionDeclaration.newBuilder()
                        .setName("getCurrentWeather")
                        .setDescription("What's the weather like in Seoul?")
                        .setParameters(
                                Schema.newBuilder()
                                        .setType(Type.OBJECT)
                                        .putProperties("location", Schema.newBuilder()
                                                .setType(Type.STRING)
                                                .setDescription("location")
                                                .build()
                                        )
                                        .addRequired("location")
                                        .build()
                        )
                        .build();

            } catch (Exception e) {
                log.warn(e.getMessage());
                throw new RuntimeException(e);
            }

        return "";
    }

    // Ask interrelated questions in a row using a ChatSession object.
    public void chatDiscussion(String projectId, String location, String modelName)
            throws IOException {
        // Initialize client that will be used to send requests. This client only needs
        // to be created once, and can be reused for multiple requests.
        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerateContentResponse response;

            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            // Create a chat session to be used for interactive conversation.
            ChatSession chatSession = new ChatSession(model);

            response = chatSession.sendMessage("Hello.");
            System.out.println(ResponseHandler.getText(response));

            response = chatSession.sendMessage("What are all the colors in a rainbow?");
            System.out.println(ResponseHandler.getText(response));

            response = chatSession.sendMessage("Why does it appear when it rains?");
            System.out.println(ResponseHandler.getText(response));
            System.out.println("Chat Ended.");
        }
    }


    }
