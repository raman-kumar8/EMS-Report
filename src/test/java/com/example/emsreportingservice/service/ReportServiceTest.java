package com.example.emsreportingservice.service;

import com.example.emsreportingservice.OpenFeign.GetAllTask;
import com.example.emsreportingservice.dto.ReportGenerateRequestDto;
import com.example.emsreportingservice.dto.ReportMetaResponseDto;
import com.example.emsreportingservice.dto.RequestListUUidsDto;
import com.example.emsreportingservice.dto.TaskModelDto;
import com.example.emsreportingservice.model.Report;
import com.example.emsreportingservice.model.ReportTaskModel;
import com.example.emsreportingservice.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private GetAllTask getAllTask;

    @Mock
    private LambdaInvokerService lambdaInvokerService;

    @Mock
    private ReportTaskService reportTaskService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateReportAsync_success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID taskId1 = UUID.randomUUID();
        UUID taskId2 = UUID.randomUUID();

        ReportGenerateRequestDto requestDto = new ReportGenerateRequestDto();
        requestDto.setReportName("Test Report");
        requestDto.setUserId(userId);
        requestDto.setTaskIds(List.of(taskId1, taskId2));

        List<TaskModelDto> taskModelDtos = new ArrayList<>();
        TaskModelDto task1 = new TaskModelDto();
        task1.setTaskName("Task 1");
        TaskModelDto task2 = new TaskModelDto();
        task2.setTaskName("Task 2");
        taskModelDtos.add(task1);
        taskModelDtos.add(task2);

        when(getAllTask.getAllById(any(RequestListUUidsDto.class)))
                .thenReturn(ResponseEntity.ok(taskModelDtos));

        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setReportId(UUID.randomUUID());
            return report;
        });

        // Act
        reportService.generateReportAsync(requestDto);

        // Assert
        verify(getAllTask, times(1)).getAllById(any(RequestListUUidsDto.class));
        verify(reportRepository, times(1)).save(any(Report.class));
        verify(notificationService, times(1)).sendNotification(any(ReportMetaResponseDto.class));
    }

    @Test
    void testGetReportMetaById_found() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setReportId(reportId);
        report.setReportName("My Report");
        report.setGeneratedTime(LocalTime.now());
        report.setS3Url("http://s3url");
        ReportTaskModel reportTaskModel = new ReportTaskModel();
        reportTaskModel.setIncludedTaskNames(List.of("Task1", "Task2"));
        report.setReportTask(reportTaskModel);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Act
        ResponseEntity<ReportMetaResponseDto> response = reportService.getReportMetaById(reportId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("My Report", response.getBody().getReportName());
        assertEquals(List.of("Task1", "Task2"), response.getBody().getTaskIncludedNames());
    }

    @Test
    void testGetReportMetaById_notFound() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            reportService.getReportMetaById(reportId);
        });

        assertEquals("Report not found", thrown.getMessage());
    }

    @Test
    void testDeleteReportById_success() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        when(reportRepository.existsById(reportId)).thenReturn(true);
        doNothing().when(reportTaskService).deleteAll(reportId);
        doNothing().when(reportRepository).deleteById(reportId);

        // Act
        boolean result = reportService.deleteReportById(reportId);

        // Assert
        assertTrue(result);
        verify(reportTaskService, times(1)).deleteAll(reportId);
        verify(reportRepository, times(1)).deleteById(reportId);
    }

    @Test
    void testDeleteReportById_notFound() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        when(reportRepository.existsById(reportId)).thenReturn(false);

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            reportService.deleteReportById(reportId);
        });
        assertEquals("Report not found", thrown.getMessage());
    }

    @Test
    void testGetAllReportsByUserId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        List<Report> reports = new ArrayList<>();
        Report report = new Report();
        report.setReportId(UUID.randomUUID());
        report.setReportName("Report 1");
        reports.add(report);

        when(reportRepository.findAllByUserId(userId)).thenReturn(reports);

        // Act
        ResponseEntity<List<Report>> response = reportService.getAllReportsByUserId(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isNotEmpty();
        assertEquals("Report 1", response.getBody().get(0).getReportName());
    }
}
