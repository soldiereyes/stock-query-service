package com.techsolution.stockquery.application.service;

import com.techsolution.stockquery.domain.model.StockView;
import com.techsolution.stockquery.infrastructure.client.ProductDTO;
import com.techsolution.stockquery.infrastructure.client.ProductServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockQueryService Tests")
class StockQueryServiceTest {

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private StockQueryService stockQueryService;

    private UUID productId1;
    private UUID productId2;
    private ProductDTO productDTO1;
    private ProductDTO productDTO2;

    @BeforeEach
    void setUp() {
        productId1 = UUID.randomUUID();
        productId2 = UUID.randomUUID();

        productDTO1 = new ProductDTO(
                productId1,
                "Produto 1",
                "Descrição do Produto 1",
                new BigDecimal("99.90"),
                15
        );

        productDTO2 = new ProductDTO(
                productId2,
                "Produto 2",
                "Descrição do Produto 2",
                new BigDecimal("49.90"),
                5
        );
    }

    @Test
    @DisplayName("Deve encontrar estoque por productId consultando product-service")
    void shouldFindStockByProductId() {
        // Given
        ResponseEntity<ProductDTO> response = ResponseEntity.ok(productDTO1);
        when(productServiceClient.getProductById(productId1)).thenReturn(response);

        // When
        Optional<StockView> result = stockQueryService.findByProductId(productId1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(productId1);
        assertThat(result.get().getProductName()).isEqualTo("Produto 1");
        assertThat(result.get().getQuantityAvailable()).isEqualTo(15);
        verify(productServiceClient, times(1)).getProductById(productId1);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando produto não existe no product-service")
    void shouldReturnEmptyOptionalWhenProductNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        ResponseEntity<ProductDTO> response = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        when(productServiceClient.getProductById(nonExistentId)).thenReturn(response);

        // When
        Optional<StockView> result = stockQueryService.findByProductId(nonExistentId);

        // Then
        assertThat(result).isEmpty();
        verify(productServiceClient, times(1)).getProductById(nonExistentId);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando resposta do product-service é null")
    void shouldReturnEmptyOptionalWhenResponseBodyIsNull() {
        // Given
        ResponseEntity<ProductDTO> response = ResponseEntity.ok(null);
        when(productServiceClient.getProductById(productId1)).thenReturn(response);

        // When
        Optional<StockView> result = stockQueryService.findByProductId(productId1);

        // Then
        assertThat(result).isEmpty();
        verify(productServiceClient, times(1)).getProductById(productId1);
    }

    @Test
    @DisplayName("Deve lançar exceção quando product-service retorna erro")
    void shouldThrowExceptionWhenProductServiceFails() {
        // Given
        when(productServiceClient.getProductById(productId1))
                .thenThrow(new RuntimeException("Erro de conexão"));

        // When/Then
        assertThatThrownBy(() -> stockQueryService.findByProductId(productId1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao consultar informações do produto");
        verify(productServiceClient, times(1)).getProductById(productId1);
    }

    @Test
    @DisplayName("Deve retornar o limite mínimo de estoque correto")
    void shouldReturnCorrectMinimumStockLimit() {
        // When
        int limit = stockQueryService.getMinimumStockLimit();

        // Then
        assertThat(limit).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve criar StockView com dados corretos do product-service")
    void shouldCreateStockViewWithCorrectData() {
        // Given
        ResponseEntity<ProductDTO> response = ResponseEntity.ok(productDTO2);
        when(productServiceClient.getProductById(productId2)).thenReturn(response);

        // When
        Optional<StockView> result = stockQueryService.findByProductId(productId2);

        // Then
        assertThat(result).isPresent();
        StockView stockView = result.get();
        assertThat(stockView.getProductId()).isEqualTo(productId2);
        assertThat(stockView.getProductName()).isEqualTo("Produto 2");
        assertThat(stockView.getQuantityAvailable()).isEqualTo(5);
        assertThat(stockView.getLastUpdated()).isNotNull();
    }
}

