package com.example.emsreportingservice.service;

import com.example.emsreportingservice.repository.ReportTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ReportTaskServiceTest {

    @Mock
    private ReportTask reportTask;

    @InjectMocks
    private ReportTaskService reportTaskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeleteAll() {
        UUID reportId = UUID.randomUUID();

        // Act
        boolean result = reportTaskService.deleteAll(reportId);

        // Assert
        assertTrue(result);
        verify(reportTask, times(1)).deleteAllByReportId(reportId);
    }
}
