package com.example.emsreportingservice.repository;

import com.example.emsreportingservice.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findAllByUserId(UUID userId);
}

