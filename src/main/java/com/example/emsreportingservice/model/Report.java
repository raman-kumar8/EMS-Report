package com.example.emsreportingservice.model;

import com.example.emsreportingservice.enums.Status;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
public class Report {

    @Id
    private UUID reportId;


    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String reportName;

    @Column
    private LocalTime generatedTime;

    @Column
    private String summary;

    @Column
    private String s3Url;
    private Status status;
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @OneToOne(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private ReportTaskModel reportTask;
}
