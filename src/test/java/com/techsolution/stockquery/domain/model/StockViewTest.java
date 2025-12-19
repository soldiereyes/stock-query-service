package com.techsolution.stockquery.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StockView Domain Model Tests")
class StockViewTest {

    private UUID productId;
    private String productName;
    private Integer quantityAvailable;
    private LocalDateTime lastUpdated;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        productName = "Produto Teste";
        quantityAvailable = 15;
        lastUpdated = LocalDateTime.now();
    }

    @Test
    @DisplayName("Deve criar StockView com construtor completo")
    void shouldCreateStockViewWithFullConstructor() {
        // When
        StockView stockView = new StockView(productId, productName, quantityAvailable, lastUpdated);

        // Then
        assertThat(stockView.getProductId()).isEqualTo(productId);
        assertThat(stockView.getProductName()).isEqualTo(productName);
        assertThat(stockView.getQuantityAvailable()).isEqualTo(quantityAvailable);
        assertThat(stockView.getLastUpdated()).isEqualTo(lastUpdated);
    }

    @Test
    @DisplayName("Deve criar StockView vazio e setar valores")
    void shouldCreateEmptyStockViewAndSetValues() {
        // Given
        StockView stockView = new StockView();

        // When
        stockView.setProductId(productId);
        stockView.setProductName(productName);
        stockView.setQuantityAvailable(quantityAvailable);
        stockView.setLastUpdated(lastUpdated);

        // Then
        assertThat(stockView.getProductId()).isEqualTo(productId);
        assertThat(stockView.getProductName()).isEqualTo(productName);
        assertThat(stockView.getQuantityAvailable()).isEqualTo(quantityAvailable);
        assertThat(stockView.getLastUpdated()).isEqualTo(lastUpdated);
    }

    @Test
    @DisplayName("Deve retornar true quando estoque está abaixo do limite mínimo")
    void shouldReturnTrueWhenStockIsBelowMinimum() {
        // Given
        int minimumLimit = 10;
        StockView stockView = new StockView(productId, productName, 5, lastUpdated);

        // When
        boolean result = stockView.isStockBelowMinimum(minimumLimit);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando estoque está acima do limite mínimo")
    void shouldReturnFalseWhenStockIsAboveMinimum() {
        // Given
        int minimumLimit = 10;
        StockView stockView = new StockView(productId, productName, 15, lastUpdated);

        // When
        boolean result = stockView.isStockBelowMinimum(minimumLimit);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando estoque está exatamente no limite mínimo")
    void shouldReturnFalseWhenStockIsExactlyAtMinimum() {
        // Given
        int minimumLimit = 10;
        StockView stockView = new StockView(productId, productName, 10, lastUpdated);

        // When
        boolean result = stockView.isStockBelowMinimum(minimumLimit);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Deve retornar true quando estoque é zero")
    void shouldReturnTrueWhenStockIsZero() {
        // Given
        int minimumLimit = 10;
        StockView stockView = new StockView(productId, productName, 0, lastUpdated);

        // When
        boolean result = stockView.isStockBelowMinimum(minimumLimit);

        // Then
        assertThat(result).isTrue();
    }
}

