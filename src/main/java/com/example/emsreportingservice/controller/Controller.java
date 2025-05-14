package com.example.emsreportingservice.controller;

import com.example.emsreportingservice.dto.ReportMetaResponseDto;
import com.example.emsreportingservice.model.Report;
import com.example.emsreportingservice.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class Controller {
     private final ReportService reportService;
     @Autowired
     public Controller(ReportService reportService) {
         this.reportService = reportService;
     }
    @GetMapping("/report/{reportId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ReportMetaResponseDto> getReportMetaById(@PathVariable String reportId) {
     return reportService.getReportMetaById(UUID.fromString(reportId));
    }
    @DeleteMapping("/report/{reportId}")
    @ResponseStatus(HttpStatus.OK)
    public boolean deleteReportById(@PathVariable String reportId) {
         return reportService.deleteReportById(UUID.fromString(reportId));
    }
    @GetMapping("/report/user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Report>> getAllReportsByUserId(@PathVariable String userId) {
        return reportService.getAllReportsByUserId(UUID.fromString(userId));
    }

}
