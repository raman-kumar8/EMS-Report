package com.example.emsreportingservice.exception;

import static org.assertj.core.api.Assertions.assertThat;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


 class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleCustomException() {
        // Arrange
        String message = "Custom exception occurred";
        CustomException customException = new CustomException(message);

        // Act
        ResponseEntity<ErrorDetails> response = globalExceptionHandler.handleCustomException(customException);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ErrorDetails errorDetails = response.getBody();
        assertThat(errorDetails).isNotNull();
        assertThat(errorDetails.getMessage()).isEqualTo(message);

    }

    @Test
    void testHandleGenericException() {
        // Arrange
        Exception exception = new Exception("Generic error");

        // Act
        ResponseEntity<ErrorDetails> response = globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        ErrorDetails errorDetails = response.getBody();
        assertThat(errorDetails).isNotNull();
        assertThat(errorDetails.getMessage()).isEqualTo("Internal server error");

    }
}
