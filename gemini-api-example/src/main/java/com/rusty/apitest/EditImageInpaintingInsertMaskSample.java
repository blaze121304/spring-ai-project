package com.rusty.apitest;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.cloud.

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EditImageInpaintingInsertMaskSample {

    public static void main(String[] args) throws IOException {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "gen-lang-client-0470512316";
        String location = "asia-northeast3";
        String inputPath = "/path/to/my-input.png";
        String maskPath = "/path/to/my-mask.png";
        String prompt = ""; // The text prompt describing what you want to see inserted in the mask area.

        editImageInpaintingInsertMask(projectId, location, inputPath, maskPath, prompt);
    }

    // Edit an image using a mask file. Inpainting can insert the object designated by the prompt
    // into the masked area.
    public static PredictResponse editImageInpaintingInsertMask(
            String projectId, String location, String inputPath, String maskPath, String prompt)
            throws ApiException, IOException {
        final String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
        PredictionServiceSettings predictionServiceSettings =
                PredictionServiceSettings.newBuilder().setEndpoint(endpoint).build();

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests.
        try (PredictionServiceClient predictionServiceClient =
                     PredictionServiceClient.create(predictionServiceSettings)) {

            final EndpointName endpointName =
                    EndpointName.ofProjectLocationPublisherModelName(
                            projectId, location, "google", "imagegeneration@006");

            // Encode image and mask to Base64
            String imageBase64 =
                    Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(inputPath)));
            String maskBase64 =
                    Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(maskPath)));

            // Create the image and image mask maps
            Map<String, String> imageMap = new HashMap<>();
            imageMap.put("bytesBase64Encoded", imageBase64);

            Map<String, String> maskMap = new HashMap<>();
            maskMap.put("bytesBase64Encoded", maskBase64);
            Map<String, Map> imageMaskMap = new HashMap<>();
            imageMaskMap.put("image", maskMap);

            Map<String, Object> instancesMap = new HashMap<>();
            instancesMap.put("prompt", prompt); // [ "prompt", "<my-prompt>" ]
            instancesMap.put(
                    "image", imageMap); // [ "image", [ "bytesBase64Encoded", "iVBORw0KGgo...==" ] ]
            instancesMap.put(
                    "mask",
                    imageMaskMap); // [ "mask", [ "image", [ "bytesBase64Encoded", "iJKDF0KGpl...==" ] ] ]
            instancesMap.put("editMode", "inpainting-insert"); // [ "editMode", "inpainting-insert" ]
            Value instances = mapToValue(instancesMap);

            // Optional parameters
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("sampleCount", 1);
            Value parameters = mapToValue(paramsMap);

            PredictResponse predictResponse =
                    predictionServiceClient.predict(
                            endpointName, Collections.singletonList(instances), parameters);

            for (Value prediction : predictResponse.getPredictionsList()) {
                Map<String, Value> fieldsMap = prediction.getStructValue().getFieldsMap();
                if (fieldsMap.containsKey("bytesBase64Encoded")) {
                    String bytesBase64Encoded = fieldsMap.get("bytesBase64Encoded").getStringValue();
                    Path tmpPath = Files.createTempFile("imagen-", ".png");
                    Files.write(tmpPath, Base64.getDecoder().decode(bytesBase64Encoded));
                    System.out.format("Image file written to: %s\n", tmpPath.toUri());
                }
            }
            return predictResponse;
        }
    }

    private static Value mapToValue(Map<String, Object> map) throws InvalidProtocolBufferException {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        Value.Builder builder = Value.newBuilder();
        JsonFormat.parser().merge(json, builder);
        return builder.build();
    }
}