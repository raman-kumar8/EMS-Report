package com.example.emsreportingservice.controller;

import com.example.emsreportingservice.dto.ReportGenerateRequestDto;
import com.example.emsreportingservice.dto.ReportMetaResponseDto;
import com.example.emsreportingservice.model.Report;
import com.example.emsreportingservice.service.KafkaProducerService;
import com.example.emsreportingservice.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(Controller.class)
 class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGenerateReport() throws Exception {
        ReportGenerateRequestDto requestDto = new ReportGenerateRequestDto();
        // Populate required fields if any

        mockMvc.perform(post("/report/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Report generation request received."));
    }

    @Test
    void testGetReportMetaById() throws Exception {
        UUID reportId = UUID.randomUUID();
        ReportMetaResponseDto responseDto = new ReportMetaResponseDto();
        // Populate mock response fields if needed

        Mockito.when(reportService.getReportMetaById(reportId))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/report/" + reportId))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteReportById() throws Exception {
        UUID reportId = UUID.randomUUID();
        Mockito.when(reportService.deleteReportById(reportId)).thenReturn(true);

        mockMvc.perform(delete("/report/" + reportId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testGetAllReportsByUserId() throws Exception {
        UUID userId = UUID.randomUUID();
        Report report = new Report(); // Populate if needed
        List<Report> reports = Collections.singletonList(report);

        Mockito.when(reportService.getAllReportsByUserId(userId))
                .thenReturn(ResponseEntity.ok(reports));

        mockMvc.perform(get("/report/user/" + userId))
                .andExpect(status().isOk());
    }
}
