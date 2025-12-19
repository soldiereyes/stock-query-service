package com.techsolution.stockquery.application.service;

import com.techsolution.stockquery.domain.model.StockView;
import com.techsolution.stockquery.domain.repository.StockQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockQueryService Tests")
class StockQueryServiceTest {

    @Mock
    private StockQueryRepository stockQueryRepository;

    @InjectMocks
    private StockQueryService stockQueryService;

    private UUID productId1;
    private UUID productId2;
    private StockView stockView1;
    private StockView stockView2;

    @BeforeEach
    void setUp() {
        productId1 = UUID.randomUUID();
        productId2 = UUID.randomUUID();

        stockView1 = new StockView(
                productId1,
                "Produto 1",
                15,
                LocalDateTime.now()
        );

        stockView2 = new StockView(
                productId2,
                "Produto 2",
                5,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve retornar todos os estoques")
    void shouldFindAllStocks() {
        // Given
        List<StockView> expectedStocks = Arrays.asList(stockView1, stockView2);
        when(stockQueryRepository.findAll()).thenReturn(expectedStocks);

        // When
        List<StockView> result = stockQueryService.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expectedStocks);
        verify(stockQueryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há estoques")
    void shouldReturnEmptyListWhenNoStocks() {
        // Given
        when(stockQueryRepository.findAll()).thenReturn(List.of());

        // When
        List<StockView> result = stockQueryService.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(stockQueryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve encontrar estoque por productId")
    void shouldFindStockByProductId() {
        // Given
        when(stockQueryRepository.findByProductId(productId1)).thenReturn(Optional.of(stockView1));

        // When
        Optional<StockView> result = stockQueryService.findByProductId(productId1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(stockView1);
        assertThat(result.get().getProductId()).isEqualTo(productId1);
        verify(stockQueryRepository, times(1)).findByProductId(productId1);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando produto não existe")
    void shouldReturnEmptyOptionalWhenProductNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(stockQueryRepository.findByProductId(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<StockView> result = stockQueryService.findByProductId(nonExistentId);

        // Then
        assertThat(result).isEmpty();
        verify(stockQueryRepository, times(1)).findByProductId(nonExistentId);
    }

    @Test
    @DisplayName("Deve retornar o limite mínimo de estoque correto")
    void shouldReturnCorrectMinimumStockLimit() {
        // When
        int limit = stockQueryService.getMinimumStockLimit();

        // Then
        assertThat(limit).isEqualTo(10);
    }
}

