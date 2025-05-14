package com.example.emsreportingservice.service;

import com.example.emsreportingservice.repository.ReportTask;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
public class ReportTaskService {
    private final ReportTask reportTask;

    @Autowired
    public ReportTaskService(ReportTask reportTask) {
        this.reportTask = reportTask;
    }

    @Transactional
    public boolean deleteAll(UUID reportId) {
        reportTask.deleteAllByReportId(reportId);
        return true;
    }
}
