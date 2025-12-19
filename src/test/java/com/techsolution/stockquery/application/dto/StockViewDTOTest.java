package com.techsolution.stockquery.application.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StockViewDTO Tests")
class StockViewDTOTest {

    private UUID productId;
    private String productName;
    private Integer quantityAvailable;
    private LocalDateTime lastUpdated;
    private Boolean stockBelowMinimum;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        productName = "Produto Teste";
        quantityAvailable = 15;
        lastUpdated = LocalDateTime.now();
        stockBelowMinimum = false;
    }

    @Test
    @DisplayName("Deve criar StockViewDTO com construtor completo")
    void shouldCreateStockViewDTOWithFullConstructor() {
        // When
        StockViewDTO dto = new StockViewDTO(productId, productName, quantityAvailable, lastUpdated, stockBelowMinimum);

        // Then
        assertThat(dto.getProductId()).isEqualTo(productId);
        assertThat(dto.getProductName()).isEqualTo(productName);
        assertThat(dto.getQuantityAvailable()).isEqualTo(quantityAvailable);
        assertThat(dto.getLastUpdated()).isEqualTo(lastUpdated);
        assertThat(dto.getStockBelowMinimum()).isEqualTo(stockBelowMinimum);
    }

    @Test
    @DisplayName("Deve criar StockViewDTO vazio e setar valores")
    void shouldCreateEmptyStockViewDTOAndSetValues() {
        // Given
        StockViewDTO dto = new StockViewDTO();

        // When
        dto.setProductId(productId);
        dto.setProductName(productName);
        dto.setQuantityAvailable(quantityAvailable);
        dto.setLastUpdated(lastUpdated);
        dto.setStockBelowMinimum(stockBelowMinimum);

        // Then
        assertThat(dto.getProductId()).isEqualTo(productId);
        assertThat(dto.getProductName()).isEqualTo(productName);
        assertThat(dto.getQuantityAvailable()).isEqualTo(quantityAvailable);
        assertThat(dto.getLastUpdated()).isEqualTo(lastUpdated);
        assertThat(dto.getStockBelowMinimum()).isEqualTo(stockBelowMinimum);
    }

    @Test
    @DisplayName("Deve setar e obter stockBelowMinimum corretamente")
    void shouldSetAndGetStockBelowMinimumCorrectly() {
        // Given
        StockViewDTO dto = new StockViewDTO();

        // When
        dto.setStockBelowMinimum(true);

        // Then
        assertThat(dto.getStockBelowMinimum()).isTrue();

        // When
        dto.setStockBelowMinimum(false);

        // Then
        assertThat(dto.getStockBelowMinimum()).isFalse();
    }
}


