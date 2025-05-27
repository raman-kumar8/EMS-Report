package com.example.emsreportingservice.service;

import com.example.emsreportingservice.OpenFeign.GetAllTask;
import com.example.emsreportingservice.dto.ReportGenerateRequestDto;
import com.example.emsreportingservice.dto.ReportMetaResponseDto;
import com.example.emsreportingservice.dto.RequestListUUidsDto;
import com.example.emsreportingservice.dto.TaskModelDto;
import com.example.emsreportingservice.enums.Status;
import com.example.emsreportingservice.model.Report;
import com.example.emsreportingservice.model.ReportTaskModel;
import com.example.emsreportingservice.repository.ReportRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service

public class ReportService {

    private ReportRepository reportRepository;
    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }


    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LambdaInvokerService lambdaInvokerService;
    @Autowired
    private ReportTaskService reportTaskService;
    @Autowired
    private NotificationService notificationService;

    @Autowired
    GetAllTask getAllTask;

    @Transactional
    public void saveReport(ReportGenerateRequestDto reportGenerateRequestDto) {
        Report report = new Report();
        report.setReportId(reportGenerateRequestDto.getReportId());
        report.setReportName(reportGenerateRequestDto.getReportName());
        report.setStatus(Status.PROCESSING);
        report.setUserId(reportGenerateRequestDto.getUserId());
        report.setCreated_at(reportGenerateRequestDto.getCreationDate());

        reportRepository.save(report);
    }

    @Transactional
    public void generateReportAsync(ReportGenerateRequestDto requestDto) {
        // Fetch existing report entity from DB
        Report report = reportRepository.findById(requestDto.getReportId())
                .orElseThrow(() -> new EntityNotFoundException("Report not found"));

        // Fetch tasks by their IDs
        List<UUID> taskIds = requestDto.getTaskIds();
        RequestListUUidsDto requestListUUidsDto = new RequestListUUidsDto(taskIds);
        ResponseEntity<List<TaskModelDto>> response = getAllTask.getAllById(requestListUUidsDto);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<TaskModelDto> taskList = response.getBody();

            // Update report fields
            report.setReportName(requestDto.getReportName());
            report.setUserId(requestDto.getUserId());
            report.setGeneratedTime(LocalTime.now());
            report.setS3Url("https://s3.amazonaws.com/your-bucket-name/your-file-name.csv");
            String summary = String.format(
                    "Report '%s'  at %s with %d tasks: %s",
                    report.getReportName(),

                    report.getGeneratedTime().toString(),
                    taskList.size(),
                    taskList.stream()
                            .map(TaskModelDto::getTaskName)
                            .collect(Collectors.joining(", "))
            );

            report.setSummary(summary);
// or call your lambda invoker here

            report.setStatus(Status.COMPLETED);

            // Update task info
            ReportTaskModel reportTaskModel = new ReportTaskModel();
            reportTaskModel.setReport(report);
            reportTaskModel.setIncludedTaskNames(
                    taskList.stream()
                            .map(TaskModelDto::getTaskName)
                            .toList()
            );
            report.setReportTask(reportTaskModel);

            // No explicit save needed because report is managed entity

            // Prepare and send notification after update
            ReportMetaResponseDto notificationDto = new ReportMetaResponseDto();
            notificationDto.setReportId(report.getReportId());
            notificationDto.setReportName(report.getReportName());
            notificationDto.setGeneratedTime(report.getGeneratedTime());
            notificationDto.setS3Url(report.getS3Url());
            notificationDto.setStatus(report.getStatus());

            notificationService.sendNotification(notificationDto);

        } else {
            // Handle failed fetch of tasks, update report status and notify
            report.setStatus(Status.FAILED);

            ReportMetaResponseDto notificationDto = new ReportMetaResponseDto();
            notificationDto.setReportId(report.getReportId());
            notificationDto.setReportName(report.getReportName());
            notificationDto.setStatus(report.getStatus());

            notificationService.sendNotification(notificationDto);

            throw new RuntimeException("Failed to fetch task details for report generation");
        }
    }


    public ResponseEntity<ReportMetaResponseDto> getReportMetaById(UUID reportId) {
        Report metadata = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        ReportMetaResponseDto reportMetaResponseDto = new ReportMetaResponseDto();
        reportMetaResponseDto.setReportId(metadata.getReportId());
        reportMetaResponseDto.setReportName(metadata.getReportName());
        reportMetaResponseDto.setGeneratedTime(metadata.getGeneratedTime());
        reportMetaResponseDto.setS3Url(metadata.getS3Url());
        reportMetaResponseDto.setStatus(metadata.getStatus());



        return ResponseEntity.ok(reportMetaResponseDto);
    }


    @Transactional
    public boolean deleteReportById(UUID reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new RuntimeException("Report not found");
        }

        reportTaskService.deleteAll(reportId);
        reportRepository.deleteById(reportId);
        return true;
    }
    public ResponseEntity<List<Report>> getAllReportsByUserId(UUID userID){
      return ResponseEntity.ok(reportRepository.findAllByUserId(userID));

    }



}
