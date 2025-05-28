package com.example.emsreportingservice.service;

import com.example.emsreportingservice.dto.TaskModelDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
@Service
@RequiredArgsConstructor
public class LambdaInvokerService {

    private final ObjectMapper objectMapper;

    private final LambdaClient lambdaClient;

    // Use the injected LambdaClient (no try-with-resources here because client managed by Spring or manually closed)
    public String invokeLambda(List<TaskModelDto> tasks) {
        try {
            String payload = objectMapper.writeValueAsString(tasks);

            InvokeRequest request = InvokeRequest.builder()
                    .functionName("pdfgenerator") // ⛔ REPLACE with actual function name
                    .payload(SdkBytes.fromUtf8String(payload))
                    .build();

            InvokeResponse response = lambdaClient.invoke(request);

            return response.payload().asUtf8String();

        } catch (Exception e) {
            e.printStackTrace();
            return "Lambda invocation failed: " + e.getMessage();
        }
    }
}
