package com.example.emsreportingservice.service;

import com.example.emsreportingservice.dto.TaskModelDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LambdaInvokerServiceTest {

    @Mock
    private LambdaClient lambdaClient;

    @InjectMocks
    private LambdaInvokerService lambdaInvokerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void invokeLambda_returnsResponsePayload() throws Exception {
        // Arrange
        TaskModelDto task = new TaskModelDto();
        task.setTaskName("Test Task");
        List<TaskModelDto> tasks = List.of(task);

        String expectedPayload = "[{\"taskName\":\"Test Task\"}]";

        InvokeResponse mockResponse = mock(InvokeResponse.class);
        SdkBytes sdkBytes = SdkBytes.fromUtf8String("Lambda response string");

        when(mockResponse.payload()).thenReturn(sdkBytes);
        when(lambdaClient.invoke(any(InvokeRequest.class))).thenReturn(mockResponse);

        // Act
        String result = lambdaInvokerService.invokeLambda(tasks);

        // Assert
        assertEquals("Lambda response string", result);

        // Verify invoke called once with correct payload
        ArgumentCaptor<InvokeRequest> captor = ArgumentCaptor.forClass(InvokeRequest.class);
        verify(lambdaClient).invoke(captor.capture());

        InvokeRequest sentRequest = captor.getValue();
        String payloadSent = sentRequest.payload().asUtf8String();

        assertEquals(expectedPayload, payloadSent);
        assertEquals("your-lambda-function-name", sentRequest.functionName());
    }

    @Test
    void invokeLambda_handlesExceptionGracefully() throws Exception {
        // Arrange
        when(lambdaClient.invoke(any(InvokeRequest.class))).thenThrow(new RuntimeException("AWS error"));

        // Act
        String result = lambdaInvokerService.invokeLambda(List.of());

        // Assert
        assertTrue(result.startsWith("Lambda invocation failed: AWS error"));
    }
}
