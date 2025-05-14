package com.example.emsreportingservice.repository;

import com.example.emsreportingservice.model.ReportTaskModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ReportTask extends JpaRepository<ReportTaskModel, UUID> {

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM ReportTaskModel r WHERE r.report.reportId = :reportId")
    void deleteAllByReportId(@Param("reportId") UUID reportId);
}


