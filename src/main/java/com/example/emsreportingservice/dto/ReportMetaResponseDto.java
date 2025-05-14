package com.example.emsreportingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportMetaResponseDto {
    private String reportId;
    private String reportName;
    private String summary;
    private LocalTime generatedTime;
    private String s3Url;
    private List<String> taskIncluded; // ← Add this field
}
