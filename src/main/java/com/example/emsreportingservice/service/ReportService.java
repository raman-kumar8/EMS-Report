package com.example.emsreportingservice.service;

import com.example.emsreportingservice.openfeign.GetAllTask;
import com.example.emsreportingservice.dto.ReportGenerateRequestDto;
import com.example.emsreportingservice.dto.ReportMetaResponseDto;
import com.example.emsreportingservice.dto.RequestListUUidsDto;
import com.example.emsreportingservice.dto.TaskModelDto;
import com.example.emsreportingservice.enums.Status;
import com.example.emsreportingservice.exception.CustomException;
import com.example.emsreportingservice.model.Report;
import com.example.emsreportingservice.model.ReportTaskModel;
import com.example.emsreportingservice.repository.ReportRepository;
import com.pusher.rest.Pusher;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service

public class ReportService {

    private final ReportRepository reportRepository;

    private final LambdaInvokerService lambdaInvokerService;
    private final ReportTaskService reportTaskService;

    private final Pusher pusher;
    private final GetAllTask getAllTask;
    private String generateSummary(List<TaskModelDto> taskList) {
        StringBuilder sb = new StringBuilder();
        sb.append("Report includes ").append(taskList.size()).append(" tasks: ");

        for (TaskModelDto task : taskList) {
            String taskTitle = task.getTitle() != null ? task.getTitle() : "Untitled";
            sb.append(taskTitle).append(", ");
            if (sb.length() >= 240) break; // Leave room for ellipsis
        }

        if (sb.length() > 255) {
            sb.setLength(252);
            sb.append("...");
        }

        return sb.toString().replaceAll(", $", ""); // Clean up trailing comma
    }
    @Autowired
    public ReportService(
            ReportRepository reportRepository,

            LambdaInvokerService lambdaInvokerService,
            ReportTaskService reportTaskService,

            Pusher pusher,
            GetAllTask getAllTask
    ) {
        this.reportRepository = reportRepository;

        this.lambdaInvokerService = lambdaInvokerService;
        this.reportTaskService = reportTaskService;

        this.pusher = pusher;
        this.getAllTask = getAllTask;
    }

    @Transactional
    public void saveReport(ReportGenerateRequestDto reportGenerateRequestDto) {
        Report report = new Report();
        report.setReportId(reportGenerateRequestDto.getReportId());
        report.setReportName(reportGenerateRequestDto.getReportName());
        report.setStatus(Status.PROCESSING);
        report.setUserId(reportGenerateRequestDto.getUserId());
        report.setCreatedAt(reportGenerateRequestDto.getCreationDate());

        reportRepository.save(report);
    }

    @Transactional
    // In your ReportService.java or wherever generateReportAsync resides

    public void generateReportAsync(ReportGenerateRequestDto requestDto) {
        // Fetch existing report entity from DB
        Report report = reportRepository.findById(requestDto.getReportId())
                .orElseThrow(() -> new EntityNotFoundException("Report not found"));

        // Fetch tasks by their IDs
        List<UUID> taskIds = requestDto.getTaskIds();
        RequestListUUidsDto requestListUUidsDto = new RequestListUUidsDto(taskIds);
        ResponseEntity<List<TaskModelDto>> response = getAllTask.getAllById(requestListUUidsDto);

        String userIdForChannel = String.valueOf(report.getUserId());

        // --- IMPORTANT CHANGE HERE: Remove "private-" prefix for public channels ---
        String channelName = "user-" + userIdForChannel; // Changed from "private-user-"
        // --- End IMPORTANT CHANGE ---

        // Define the event name your frontend will listen for
        String eventName = "report-status-update";

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<TaskModelDto> taskList = response.getBody();

            // Update report fields
            report.setReportName(requestDto.getReportName());
            report.setUserId(requestDto.getUserId());
            report.setGeneratedTime(LocalTime.now());
            String invok = lambdaInvokerService.invokeLambda(taskList);

            report.setS3Url(invok);
            report.setSummary(generateSummary(taskList));
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

            pusher.trigger(channelName, eventName,String.valueOf(report.getReportId()));


        } else {
            // Handle failed fetch of tasks, update report status and notify
            report.setStatus(Status.FAILED);

            ReportMetaResponseDto notificationDto = new ReportMetaResponseDto();
            notificationDto.setReportId(report.getReportId());
            notificationDto.setReportName(report.getReportName());
            notificationDto.setStatus(report.getStatus());

            pusher.trigger(channelName, eventName,String.valueOf(report.getReportId()));

            throw new CustomException("Failed to fetch task details for report generation");
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
            throw new CustomException("Report not found");
        }

        reportTaskService.deleteAll(reportId);
        reportRepository.deleteById(reportId);
        return true;
    }
    public ResponseEntity<List<Report>> getAllReportsByUserId(UUID userID){
      return ResponseEntity.ok(reportRepository.findAllByUserId(userID));

    }
    public List<Report> getAllReports(){
        return reportRepository.findAll();
    }



}
