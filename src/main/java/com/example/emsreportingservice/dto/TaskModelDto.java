package com.example.emsreportingservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalTime;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class TaskModelDto {
    private String taskName;
    private UUID id;
    private UUID userId;
    private String title;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime duration;
    private String taskStatus;
    private String priority;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Tag tag;
    private String assignedBy;

    @Data
    public static class Tag {
        private int id;
        @JsonProperty("tag")
        private String tagName;
    }
}
