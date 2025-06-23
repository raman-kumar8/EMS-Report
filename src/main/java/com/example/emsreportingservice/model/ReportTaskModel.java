package com.example.emsreportingservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;


import java.util.List;
import java.util.UUID;
@Entity
@Data
public class ReportTaskModel {

    @Id
    @GeneratedValue(generator = "UUID")

    private UUID id;

    @OneToOne
    @JoinColumn(name = "report_id")
    @JsonBackReference
    private Report report;
    @Column
    @ElementCollection
    private List<String> includedTaskNames;
}
