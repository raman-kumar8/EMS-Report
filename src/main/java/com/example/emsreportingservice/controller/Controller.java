package com.example.emsreportingservice.controller;

import com.example.emsreportingservice.dto.ReportGenerateRequestDto;
import com.example.emsreportingservice.dto.ReportMetaResponseDto;
import com.example.emsreportingservice.enums.Status;
import com.example.emsreportingservice.model.Report;
import com.example.emsreportingservice.service.KafkaProducerService;
import com.example.emsreportingservice.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/report")
public class Controller {
     private final ReportService reportService;
    private final  KafkaProducerService kafkaProducerService;

     @Autowired
     public Controller(ReportService reportService ,KafkaProducerService kafkaProducerService) {
         this.reportService = reportService;
         this.kafkaProducerService = kafkaProducerService;
     }


    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<ReportMetaResponseDto> generateReport(@Valid @RequestBody ReportGenerateRequestDto reportGenerateRequestDto) {
        // Generate a new UUID for the report
        UUID newReportId = UUID.randomUUID();

        // Create a new DTO with the generated reportId (copy other fields)
        ReportGenerateRequestDto dtoWithId = new ReportGenerateRequestDto();
        dtoWithId.setReportId(newReportId);
        dtoWithId.setReportName(reportGenerateRequestDto.getReportName());
        dtoWithId.setUserId(reportGenerateRequestDto.getUserId());
        dtoWithId.setTaskIds(reportGenerateRequestDto.getTaskIds());
        dtoWithId.setCreationDate(reportGenerateRequestDto.getCreationDate());

        // Save the report first with PROCESSING status
        reportService.saveReport(dtoWithId);

        // Send Kafka message with the newly created reportId
        kafkaProducerService.sendReportRequest(dtoWithId);

        // Prepare response for frontend
        ReportMetaResponseDto responseDto = new ReportMetaResponseDto();
        responseDto.setReportId(newReportId);
        responseDto.setReportName(dtoWithId.getReportName());
        responseDto.setStatus(Status.PROCESSING);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{reportId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ReportMetaResponseDto> getReportMetaById(@PathVariable String reportId) {
     return reportService.getReportMetaById(UUID.fromString(reportId));
    }
    @DeleteMapping("/{reportId}")
    @ResponseStatus(HttpStatus.OK)
    public boolean deleteReportById(@PathVariable String reportId) {
         return reportService.deleteReportById(UUID.fromString(reportId));
    }
    @GetMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Report>> getAllReportsByUserId(@PathVariable String userId) {
        return reportService.getAllReportsByUserId(UUID.fromString(userId));
    }
    @GetMapping("/admin/getAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Report>> getAllReports() {
         return new ResponseEntity<>(reportService.getAllReports(), HttpStatus.OK);
    }

}
