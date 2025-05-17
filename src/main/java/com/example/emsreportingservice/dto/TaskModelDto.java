package com.example.emsreportingservice.dto;

import lombok.Data;

import java.time.LocalTime;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class TaskModelDto {
    private String taskName;
    private UUID id;
    private UUID user_id;
    private String title;
    private String description;
    private LocalTime start_time;
    private LocalTime end_time;
    private LocalTime duration;
    private String taskStatus;
    private String priority;
    private ZonedDateTime created_at;
    private ZonedDateTime updated_at;
    private Tag tag;

    @Data
    public static class Tag {
        private int id;
        private String tag;
    }
}
