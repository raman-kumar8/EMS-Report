package com.example.emsreportingservice.service;

import com.example.emsreportingservice.dto.ReportGenerateRequestDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private final ReportService reportService;

    public KafkaConsumerService(ReportService reportService) {
        this.reportService = reportService;
    }

    @KafkaListener(topics = "${kafka.topic.report}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "reportKafkaListenerContainerFactory")
    public void consumeReportRequest( ReportGenerateRequestDto requestDto) {
        // Call the actual logic to generate report
        reportService.generateReportAsync(requestDto);
    }
}
