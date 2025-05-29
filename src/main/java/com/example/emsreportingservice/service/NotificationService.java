package com.example.emsreportingservice.service;

import com.example.emsreportingservice.dto.ReportMetaResponseDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(ReportMetaResponseDto reportMeta) {
        messagingTemplate.convertAndSend("/topic}", reportMeta);
    }
}
