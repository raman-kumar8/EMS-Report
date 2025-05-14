package com.example.emsreportingservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;
@Entity
@Data
public class ReportTaskModel {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "report_id")
    @JsonBackReference
    private Report report;

    @Column(columnDefinition = "jsonb")  // Use 'json' if not using PostgreSQL
    private String includedTaskUuids;    // Should be stored as a JSON array string
}
