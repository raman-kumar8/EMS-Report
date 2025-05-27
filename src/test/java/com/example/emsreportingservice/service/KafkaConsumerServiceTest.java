package com.example.emsreportingservice.service;

import com.example.emsreportingservice.dto.ReportGenerateRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class KafkaConsumerServiceTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void consumeReportRequest_callsGenerateReportAsync() {
        // Arrange
        ReportGenerateRequestDto requestDto = new ReportGenerateRequestDto();
        // You can set fields on requestDto here if needed

        // Act
        kafkaConsumerService.consumeReportRequest(requestDto);

        // Assert
        Mockito.verify(reportService, Mockito.times(1)).generateReportAsync(requestDto);
    }
}
