package com.techsolution.stockquery.application.service;

import com.techsolution.stockquery.domain.model.StockView;
import com.techsolution.stockquery.infrastructure.client.PageResponse;
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
import java.util.ArrayList;
import java.util.List;
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

    @Test
    @DisplayName("Deve buscar estoques paginados do product-service")
    void shouldFindStocksPaginated() {
        // Given
        List<ProductDTO> products = List.of(productDTO1, productDTO2);
        PageResponse<ProductDTO> pageResponse = new PageResponse<>(
                products, 0, 20, 2L, 1, true, true
        );
        ResponseEntity<PageResponse<ProductDTO>> response = ResponseEntity.ok(pageResponse);
        when(productServiceClient.getProducts(0, 20)).thenReturn(response);

        // When
        PageResponse<StockView> result = stockQueryService.findStocksPaginated(0, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotalElements()).isEqualTo(2L);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getFirst()).isTrue();
        assertThat(result.getLast()).isTrue();
        
        verify(productServiceClient, times(1)).getProducts(0, 20);
    }

    @Test
    @DisplayName("Deve buscar todos os estoques iterando sobre todas as páginas")
    void shouldFindAllStocksIteratingAllPages() {
        // Given - Primeira página
        List<ProductDTO> page1Products = List.of(productDTO1);
        PageResponse<ProductDTO> page1Response = new PageResponse<>(
                page1Products, 0, 20, 2L, 2, true, false
        );
        ResponseEntity<PageResponse<ProductDTO>> page1 = ResponseEntity.ok(page1Response);
        
        // Segunda página
        List<ProductDTO> page2Products = List.of(productDTO2);
        PageResponse<ProductDTO> page2Response = new PageResponse<>(
                page2Products, 1, 20, 2L, 2, false, true
        );
        ResponseEntity<PageResponse<ProductDTO>> page2 = ResponseEntity.ok(page2Response);
        
        when(productServiceClient.getProducts(0, 20)).thenReturn(page1);
        when(productServiceClient.getProducts(1, 20)).thenReturn(page2);

        // When
        List<StockView> result = stockQueryService.findAllStocks(0, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProductId()).isEqualTo(productId1);
        assertThat(result.get(1).getProductId()).isEqualTo(productId2);
        
        verify(productServiceClient, times(1)).getProducts(0, 20);
        verify(productServiceClient, times(1)).getProducts(1, 20);
    }

    @Test
    @DisplayName("Deve buscar todos os estoques quando há apenas uma página")
    void shouldFindAllStocksWithSinglePage() {
        // Given
        List<ProductDTO> products = List.of(productDTO1, productDTO2);
        PageResponse<ProductDTO> pageResponse = new PageResponse<>(
                products, 0, 20, 2L, 1, true, true
        );
        ResponseEntity<PageResponse<ProductDTO>> response = ResponseEntity.ok(pageResponse);
        when(productServiceClient.getProducts(0, 20)).thenReturn(response);

        // When
        List<StockView> result = stockQueryService.findAllStocks(0, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        verify(productServiceClient, times(1)).getProducts(0, 20);
        verify(productServiceClient, never()).getProducts(1, 20);
    }

    @Test
    @DisplayName("Deve limitar size ao máximo de 100 ao buscar estoques paginados")
    void shouldLimitSizeToMax100WhenFindingStocksPaginated() {
        // Given
        List<ProductDTO> products = new ArrayList<>();
        PageResponse<ProductDTO> pageResponse = new PageResponse<>(
                products, 0, 100, 0L, 1, true, true
        );
        ResponseEntity<PageResponse<ProductDTO>> response = ResponseEntity.ok(pageResponse);
        when(productServiceClient.getProducts(0, 100)).thenReturn(response);

        // When - tenta usar size maior que 100
        stockQueryService.findStocksPaginated(0, 150);

        // Then - deve usar 100 como máximo
        verify(productServiceClient, times(1)).getProducts(0, 100);
        verify(productServiceClient, never()).getProducts(0, 150);
    }

    @Test
    @DisplayName("Deve lançar exceção quando product-service falha ao buscar produtos paginados")
    void shouldThrowExceptionWhenProductServiceFailsOnPaginatedRequest() {
        // Given
        when(productServiceClient.getProducts(0, 20))
                .thenThrow(new RuntimeException("Erro de conexão"));

        // When/Then
        assertThatThrownBy(() -> stockQueryService.findStocksPaginated(0, 20))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao buscar produtos paginados");
        verify(productServiceClient, times(1)).getProducts(0, 20);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando product-service retorna página sem conteúdo")
    void shouldReturnEmptyListWhenProductServiceReturnsEmptyPage() {
        // Given
        PageResponse<ProductDTO> emptyPageResponse = new PageResponse<>(
                new ArrayList<>(), 0, 20, 0L, 1, true, true
        );
        ResponseEntity<PageResponse<ProductDTO>> response = ResponseEntity.ok(emptyPageResponse);
        when(productServiceClient.getProducts(0, 20)).thenReturn(response);

        // When
        List<StockView> result = stockQueryService.findAllStocks(0, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(productServiceClient, times(1)).getProducts(0, 20);
    }
}

