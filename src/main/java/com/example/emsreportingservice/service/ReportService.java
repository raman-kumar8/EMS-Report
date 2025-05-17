package com.example.emsreportingservice.service;

import com.example.emsreportingservice.OpenFeign.GetAllTask;
import com.example.emsreportingservice.dto.ReportGenerateRequestDto;
import com.example.emsreportingservice.dto.ReportMetaResponseDto;
import com.example.emsreportingservice.dto.RequestListUUidsDto;
import com.example.emsreportingservice.dto.TaskModelDto;
import com.example.emsreportingservice.model.Report;
import com.example.emsreportingservice.model.ReportTaskModel;
import com.example.emsreportingservice.repository.ReportRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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


public void generateReportAsync(ReportGenerateRequestDto requestDto) {
    List<UUID> taskIds = requestDto.getTaskIds();
    RequestListUUidsDto requestListUUidsDto = new RequestListUUidsDto(taskIds);
    ResponseEntity<List<TaskModelDto>> response = getAllTask.getAllById(requestListUUidsDto);

    if (response.getStatusCode().is2xxSuccessful()) {
        List<TaskModelDto> taskList = response.getBody();
//        String s3Url = lambdaInvokerService.invokeLambda(taskList);
        String s3Url = "https://s3.amazonaws.com/your-bucket-name/your-file-name.csv";


        Report report = new Report();
        report.setReportName(requestDto.getReportName());
        report.setUserId(requestDto.getUserId()); // Replace with actual userId
        report.setGeneratedTime(LocalTime.now());
        report.setS3Url(s3Url);
        report.setSummary("Summary Here"); // optional
        // 2. Create ReportTaskModel
        ReportTaskModel reportTaskModel = new ReportTaskModel();
         List<String> list = new ArrayList<>();
        if (taskList == null) {
            throw new RuntimeException("Task list is null");
        }

        for(TaskModelDto taskModelDto:taskList){
             list.add(taskModelDto.getTaskName());
         }
        reportTaskModel.setReport(report);
        reportTaskModel.setIncludedTaskNames(list);
//        try {
//            reportTaskModel.setIncludedTaskUuids(taskIds); // No conversion to string
//
//
//        } catch (Exception e) {
//            System.err.println("Error serializing task IDs: " + e.getMessage());
//        }

        report.setReportTask(reportTaskModel);

       Report savedReport = null;
        try {
             savedReport = reportRepository.save(report);
            System.out.println("Saved report ID: " + savedReport.getReportId());
        } catch (Exception e) {
            System.err.println("Failed to save report: " + e.getMessage());
            e.printStackTrace();
        }


        // 5. Prepare the response
        ReportMetaResponseDto reportMetaResponseDto = new ReportMetaResponseDto();
        reportMetaResponseDto.setReportId(savedReport.getReportId().toString());
        reportMetaResponseDto.setReportName(savedReport.getReportName());
        reportMetaResponseDto.setGeneratedTime(savedReport.getGeneratedTime());
        reportMetaResponseDto.setS3Url(savedReport.getS3Url());
//        reportMetaResponseDto.setTaskIncluded(taskIds);

        // 6. Send response notification (e.g., Kafka, REST call, email)
        notificationService.sendNotification(reportMetaResponseDto); // ← your logic

    } else {
        System.err.println("Failed to fetch task details.");
    }
}

    public ResponseEntity<ReportMetaResponseDto> getReportMetaById(UUID reportId) {
        Report metadata = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        ReportMetaResponseDto reportMetaResponseDto = new ReportMetaResponseDto();
        reportMetaResponseDto.setReportId(metadata.getReportId().toString());
        reportMetaResponseDto.setReportName(metadata.getReportName());
        reportMetaResponseDto.setGeneratedTime(metadata.getGeneratedTime());
        reportMetaResponseDto.setS3Url(metadata.getS3Url());
        reportMetaResponseDto.setTaskIncludedNames(metadata.getReportTask().getIncludedTaskNames());
//        List<UUID> taskIncludedList = new ArrayList<>();
//        if (metadata.getReportTask() != null && metadata.getReportTask().getIncludedTaskUuids() != null) {
//            List<String> includedTaskStrings = metadata.getReportTask()
//                    .getIncludedTaskUuids()
//                    .stream()
//                    .map(UUID::toString)
//                    .collect(Collectors.toList());
//
//        }
//
//        reportMetaResponseDto.setTaskIncluded(taskIncludedList);



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
