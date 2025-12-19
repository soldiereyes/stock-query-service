package com.techsolution.stockquery.infrastructure.config;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        logger.error("=== EXCEÇÃO NÃO TRATADA ===");
        logger.error("Tipo: {}", ex.getClass().getName());
        logger.error("Mensagem: {}", ex.getMessage());
        logger.error("Causa: {}", ex.getCause() != null ? ex.getCause().getMessage() : "N/A");
        logger.error("Stack trace completo:", ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("type", ex.getClass().getSimpleName());
        errorResponse.put("path", "");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("=== ARGUMENTO INVÁLIDO ===");
        logger.warn("Mensagem: {}", ex.getMessage());
        logger.warn("Stack trace:", ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeignException(FeignException ex) {
        logger.error("=== ERRO FEIGN - Comunicação com product-service ===");
        logger.error("Status HTTP: {}", ex.status());
        logger.error("Mensagem: {}", ex.getMessage());
        logger.error("Request URL: {}", ex.request() != null ? ex.request().url() : "N/A");
        logger.error("Request Method: {}", ex.request() != null ? ex.request().httpMethod() : "N/A");
        
        if (ex.responseBody().isPresent()) {
            logger.error("Response Body: {}", new String(ex.responseBody().get().array()));
        }
        
        logger.error("Stack trace completo:", ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        
        if (ex.status() == 404) {
            logger.warn("Produto não encontrado (404) no product-service");
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", "Not Found");
            errorResponse.put("message", "Produto não encontrado no product-service");
            errorResponse.put("details", "O product-service retornou status 404");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } else if (ex.status() == -1) {
            logger.error("Erro de conexão - product-service pode estar indisponível");
            errorResponse.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            errorResponse.put("error", "Service Unavailable");
            errorResponse.put("message", "Não foi possível conectar ao product-service. Verifique se o serviço está rodando.");
            errorResponse.put("details", ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        } else {
            logger.error("Erro HTTP {} ao comunicar com product-service", ex.status());
            errorResponse.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            errorResponse.put("error", "Service Unavailable");
            errorResponse.put("message", "Erro ao comunicar com product-service");
            errorResponse.put("details", String.format("Status: %d - %s", ex.status(), ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }
}

