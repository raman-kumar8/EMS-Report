package com.example.emsreportingservice.service;

import com.example.emsreportingservice.dto.ReportGenerateRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    @Value("${kafka.topic.report}")
    private String topicName;

    private final KafkaTemplate<String, ReportGenerateRequestDto> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, ReportGenerateRequestDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendReportRequest(ReportGenerateRequestDto requestDto) {
        String key = requestDto.getTaskIds() != null && !requestDto.getTaskIds().isEmpty()
                ? requestDto.getTaskIds().get(0).toString()
                : UUID.randomUUID().toString(); // fallback key

        try {
            CompletableFuture<SendResult<String, ReportGenerateRequestDto>> future =
                    kafkaTemplate.send(topicName, key, requestDto);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Report request sent successfully for key: {}, topic: {}", key, topicName);
                } else {
                    log.error("Failed to send report request to topic {}: {}", topicName, ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error preparing report request for sending to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Error preparing report request for sending to Kafka", e);
        }
    }
}

