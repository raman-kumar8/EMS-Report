package com.example.emsreportingservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;
@Data
public class ReportGenerateRequestDto {
    @NotNull(message =  "Please select The Tasks")
    private List<UUID> taskIds;
    @NotNull(message = "Please select the report name")
    private String reportName;
    @NotNull(message = "Please select the user id")
    private UUID userId;
    @NotNull
    private Date creationDate;

    private UUID reportId;
}
