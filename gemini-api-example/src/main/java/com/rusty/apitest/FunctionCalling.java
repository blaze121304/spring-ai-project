package com.rusty.apitest;

import com.google.api.client.util.Value;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.*;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class FunctionCalling {
    public static void main(String[] args) throws IOException {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "gen-lang-client-0470512316";
        String location = "asia-northeast3";
        String modelName = "gemini-1.5-flash-001";  //gemini-1.5-pro

        String promptText = "how is weather in seoul?";

        whatsTheWeatherLike(projectId, location, modelName, promptText);
    }

    // A request involving the interaction with an external tool
    public static String whatsTheWeatherLike(String projectId, String location,
                                             String modelName, String promptText)
            throws IOException {
        // Initialize client that will be used to send requests.
        // This client only needs to be created once, and can be reused for multiple requests.
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

            System.out.println("Function declaration:");
            System.out.println(functionDeclaration);

            // Add the function to a "tool"
            Tool tool = Tool.newBuilder()
                    .addFunctionDeclarations(functionDeclaration)
                    .build();

            // Start a chat session from a model, with the use of the declared function.
            GenerativeModel model = new GenerativeModel(modelName, vertexAI)
                    .withTools(Arrays.asList(tool));
            ChatSession chat = model.startChat();

            System.out.println(String.format("Ask the question: %s", promptText));
            GenerateContentResponse response = chat.sendMessage(promptText);

            // The model will most likely return a function call to the declared
            // function `getCurrentWeather` with "Paris" as the value for the
            // argument `location`.
            System.out.println("\nPrint response: ");
            System.out.println(ResponseHandler.getContent(response));

            // Provide an answer to the model so that it knows what the result
            // of a "function call" is.
            Content content =
                    ContentMaker.fromMultiModalData(
                            PartMaker.fromFunctionResponse(
                                    "getCurrentWeather",
                                    Collections.singletonMap("currentWeather", "sunny")));
            System.out.println("Provide the function response: ");
            System.out.println(content);
            response = chat.sendMessage(content);

            // See what the model replies now
            System.out.println("Print response: ");
            String finalAnswer = ResponseHandler.getText(response);
            System.out.println(finalAnswer);

            return finalAnswer;
        }
    }
}