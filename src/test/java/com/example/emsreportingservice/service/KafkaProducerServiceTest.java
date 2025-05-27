package com.example.emsreportingservice.service;

import com.example.emsreportingservice.dto.ReportGenerateRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, ReportGenerateRequestDto> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set topicName via reflection or a setter if you have one
        // For simplicity, we set it via reflection here:
        try {
            java.lang.reflect.Field field = KafkaProducerService.class.getDeclaredField("topicName");
            field.setAccessible(true);
            field.set(kafkaProducerService, "test-topic");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void sendReportRequest_withTaskIds_sendsWithFirstTaskIdAsKey() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        ReportGenerateRequestDto requestDto = new ReportGenerateRequestDto();
        requestDto.setTaskIds(Collections.singletonList(taskId));

        CompletableFuture<SendResult<String, ReportGenerateRequestDto>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(eq("test-topic"), eq(taskId.toString()), eq(requestDto))).thenReturn(future);

        // Act
        kafkaProducerService.sendReportRequest(requestDto);

        // Complete the future successfully to trigger the log info block
        future.complete(null);

        // Assert
        verify(kafkaTemplate, times(1)).send("test-topic", taskId.toString(), requestDto);
    }

    @Test
    void sendReportRequest_withoutTaskIds_sendsWithRandomKey() {
        // Arrange
        ReportGenerateRequestDto requestDto = new ReportGenerateRequestDto();
        requestDto.setTaskIds(Collections.emptyList());

        CompletableFuture<SendResult<String, ReportGenerateRequestDto>> future = new CompletableFuture<>();
        // We can't predict the random UUID key, so use anyString() matcher
        when(kafkaTemplate.send(eq("test-topic"), anyString(), eq(requestDto))).thenReturn(future);

        // Act
        kafkaProducerService.sendReportRequest(requestDto);

        // Complete the future exceptionally to trigger the log error block
        future.completeExceptionally(new RuntimeException("Kafka send failure"));

        // Assert
        verify(kafkaTemplate, times(1)).send(eq("test-topic"), anyString(), eq(requestDto));
    }
}
