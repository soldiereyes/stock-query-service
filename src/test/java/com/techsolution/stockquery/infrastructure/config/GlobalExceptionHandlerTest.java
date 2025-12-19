package com.techsolution.stockquery.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Deve tratar exceção genérica corretamente")
    void shouldHandleGenericException() {
        // Given
        Exception exception = new Exception("Erro genérico de teste");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("message")).isEqualTo("Erro genérico de teste");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException corretamente")
    void shouldHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("Argumento inválido");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    @DisplayName("Deve incluir timestamp na resposta de erro")
    void shouldIncludeTimestampInErrorResponse() {
        // Given
        Exception exception = new Exception("Teste");

        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleException(exception);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }
}


