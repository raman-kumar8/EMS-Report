package com.example.emsreportingservice.service;

import com.example.emsreportingservice.dto.ReportMetaResponseDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

class NotificationServiceTest {

    @Test
    void sendNotification_callsMessagingTemplateConvertAndSend() {
        // Arrange
        SimpMessagingTemplate messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
        NotificationService notificationService = new NotificationService(messagingTemplate);

        ReportMetaResponseDto reportMeta = new ReportMetaResponseDto();
        reportMeta.setReportId(UUID.randomUUID());
        reportMeta.setReportName("Test Report");
        // (set other fields if needed)

        // Act
        notificationService.sendNotification(reportMeta);

        // Assert
        Mockito.verify(messagingTemplate)
                .convertAndSend("/topic/reports", reportMeta);
    }
}
