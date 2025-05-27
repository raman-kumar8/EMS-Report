package com.example.emsreportingservice.dto;

import com.example.emsreportingservice.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportMetaResponseDto {
    private UUID reportId;
    private String reportName;

    private LocalTime generatedTime;
    private String s3Url;
    private Status status;
}
