package com.example.emsreportingservice.service;

import com.example.emsreportingservice.dto.ReportMetaResponseDto;
import com.example.emsreportingservice.model.Report;
import com.example.emsreportingservice.repository.ReportRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service

public class ReportService {
    private ReportRepository reportRepository;
    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }
    @Autowired
    private ReportTaskService reportTaskService;


    public ResponseEntity<ReportMetaResponseDto> getReportMetaById(UUID reportId) {
        Report metadata = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        ReportMetaResponseDto reportMetaResponseDto = new ReportMetaResponseDto();
        reportMetaResponseDto.setReportId(metadata.getReportId().toString());
        reportMetaResponseDto.setReportName(metadata.getReportName());
        reportMetaResponseDto.setSummary(metadata.getSummary());
        reportMetaResponseDto.setGeneratedTime(metadata.getGeneratedTime());
        reportMetaResponseDto.setS3Url(metadata.getS3Url());

        // Convert JSON string to List<String>
        List<String> taskIncludedList = new ArrayList<>();
        try {
            if (metadata.getReportTask() != null && metadata.getReportTask().getIncludedTaskUuids() != null) {
                ObjectMapper mapper = new ObjectMapper();
                taskIncludedList = mapper.readValue(
                        metadata.getReportTask().getIncludedTaskUuids(),
                        new TypeReference<List<String>>() {}
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing includedTaskUuids", e);
        }

        reportMetaResponseDto.setTaskIncluded(taskIncludedList);

        return ResponseEntity.ok(reportMetaResponseDto);
    }
    @Transactional
    public boolean deleteReportById(UUID reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new RuntimeException("Report not found");
        }

        reportTaskService.deleteAll(reportId); // must execute native delete
        reportRepository.deleteById(reportId);
        return true;
    }
    public ResponseEntity<List<Report>> getAllReportsByUserId(UUID userID){
      return ResponseEntity.ok(reportRepository.findAllByUserId(userID));

    }



}
