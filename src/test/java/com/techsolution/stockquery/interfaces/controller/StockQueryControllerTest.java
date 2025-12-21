package com.techsolution.stockquery.interfaces.controller;

import com.techsolution.stockquery.application.dto.PageResponseDTO;
import com.techsolution.stockquery.application.dto.StockViewDTO;
import com.techsolution.stockquery.application.service.StockQueryService;
import com.techsolution.stockquery.domain.model.StockView;
import com.techsolution.stockquery.infrastructure.client.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockQueryController Tests")
class StockQueryControllerTest {

    @Mock
    private StockQueryService stockQueryService;

    @InjectMocks
    private StockQueryController stockQueryController;

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
    @DisplayName("GET /stocks/{productId} - Deve retornar estoque quando produto existe")
    void shouldGetStockByProductIdWhenExists() {
        // Given
        when(stockQueryService.findByProductId(productId1)).thenReturn(Optional.of(stockView1));
        when(stockQueryService.getMinimumStockLimit()).thenReturn(10);

        // When
        ResponseEntity<StockViewDTO> response = stockQueryController.getStockByProductId(productId1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        StockViewDTO dto = response.getBody();
        assertThat(dto.getProductId()).isEqualTo(productId1);
        assertThat(dto.getProductName()).isEqualTo("Produto 1");
        assertThat(dto.getQuantityAvailable()).isEqualTo(15);
        assertThat(dto.getStockBelowMinimum()).isFalse();

        verify(stockQueryService, times(1)).findByProductId(productId1);
    }

    @Test
    @DisplayName("GET /stocks/{productId} - Deve retornar 404 quando produto não existe")
    void shouldReturn404WhenProductNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(stockQueryService.findByProductId(nonExistentId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<StockViewDTO> response = stockQueryController.getStockByProductId(nonExistentId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(stockQueryService, times(1)).findByProductId(nonExistentId);
    }

    @Test
    @DisplayName("GET /stocks/{productId} - Deve marcar estoque abaixo do mínimo corretamente")
    void shouldMarkStockBelowMinimumCorrectly() {
        // Given
        when(stockQueryService.findByProductId(productId2)).thenReturn(Optional.of(stockView2));
        when(stockQueryService.getMinimumStockLimit()).thenReturn(10);

        // When
        ResponseEntity<StockViewDTO> response = stockQueryController.getStockByProductId(productId2);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStockBelowMinimum()).isTrue();
        assertThat(response.getBody().getQuantityAvailable()).isEqualTo(5);
    }

    @Test
    @DisplayName("GET /stocks - Deve retornar estoques paginados")
    void shouldGetAllStocksPaginated() {
        // Given
        PageResponse<StockView> pageResponse = new PageResponse<>(
                List.of(stockView1, stockView2), 0, 20, 2L, 1, true, true
        );
        when(stockQueryService.findStocksPaginated(0, 20)).thenReturn(pageResponse);
        when(stockQueryService.getMinimumStockLimit()).thenReturn(10);

        // When
        ResponseEntity<PageResponseDTO<StockViewDTO>> response = 
                stockQueryController.getAllStocksPaginated(0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);
        assertThat(response.getBody().getPage()).isEqualTo(0);
        assertThat(response.getBody().getSize()).isEqualTo(20);
        assertThat(response.getBody().getTotalElements()).isEqualTo(2L);
        assertThat(response.getBody().getTotalPages()).isEqualTo(1);
        assertThat(response.getBody().getFirst()).isTrue();
        assertThat(response.getBody().getLast()).isTrue();
        
        verify(stockQueryService, times(1)).findStocksPaginated(0, 20);
    }

    @Test
    @DisplayName("GET /stocks - Deve usar valores padrão quando parâmetros não são fornecidos")
    void shouldUseDefaultValuesWhenParametersNotProvided() {
        // Given
        PageResponse<StockView> pageResponse = new PageResponse<>(
                List.of(stockView1), 0, 20, 1L, 1, true, true
        );
        when(stockQueryService.findStocksPaginated(0, 20)).thenReturn(pageResponse);
        when(stockQueryService.getMinimumStockLimit()).thenReturn(10);

        // When
        ResponseEntity<PageResponseDTO<StockViewDTO>> response = 
                stockQueryController.getAllStocksPaginated(null, null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(stockQueryService, times(1)).findStocksPaginated(0, 20);
    }

    @Test
    @DisplayName("GET /stocks - Deve validar e corrigir parâmetros inválidos")
    void shouldValidateAndCorrectInvalidParameters() {
        // Given
        PageResponse<StockView> pageResponse = new PageResponse<>(
                List.of(stockView1), 0, 20, 1L, 1, true, true
        );
        when(stockQueryService.findStocksPaginated(0, 20)).thenReturn(pageResponse);
        when(stockQueryService.getMinimumStockLimit()).thenReturn(10);

        // When - page negativo e size maior que 100
        ResponseEntity<PageResponseDTO<StockViewDTO>> response = 
                stockQueryController.getAllStocksPaginated(-1, 150);

        // Then - deve corrigir para valores válidos
        verify(stockQueryService, times(1)).findStocksPaginated(0, 20);
    }

    @Test
    @DisplayName("GET /stock - Deve retornar todos os estoques (carregamento completo)")
    void shouldGetAllStocksComplete() {
        // Given
        List<StockView> allStocks = List.of(stockView1, stockView2);
        when(stockQueryService.findAllStocks(0, 20)).thenReturn(allStocks);
        when(stockQueryService.getMinimumStockLimit()).thenReturn(10);

        // When
        ResponseEntity<?> response = stockQueryController.getAllStocks(null, null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(List.class);
        
        @SuppressWarnings("unchecked")
        List<StockViewDTO> body = (List<StockViewDTO>) response.getBody();
        assertThat(body).hasSize(2);
        
        verify(stockQueryService, times(1)).findAllStocks(0, 20);
    }

    @Test
    @DisplayName("GET /stock - Deve usar size fornecido para iteração")
    void shouldUseProvidedSizeForIteration() {
        // Given
        List<StockView> allStocks = List.of(stockView1, stockView2);
        when(stockQueryService.findAllStocks(0, 50)).thenReturn(allStocks);
        when(stockQueryService.getMinimumStockLimit()).thenReturn(10);

        // When
        ResponseEntity<?> response = stockQueryController.getAllStocks(null, 50);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(stockQueryService, times(1)).findAllStocks(0, 50);
    }

    @Test
    @DisplayName("GET /stock - Deve limitar size ao máximo de 100")
    void shouldLimitSizeToMax100() {
        // Given
        List<StockView> allStocks = List.of(stockView1);
        when(stockQueryService.findAllStocks(0, 100)).thenReturn(allStocks);
        when(stockQueryService.getMinimumStockLimit()).thenReturn(10);

        // When - tenta usar size maior que 100
        ResponseEntity<?> response = stockQueryController.getAllStocks(null, 150);

        // Then - deve usar 100 como máximo
        verify(stockQueryService, times(1)).findAllStocks(0, 100);
        verify(stockQueryService, never()).findAllStocks(0, 150);
    }

    @Test
    @DisplayName("GET /stock/{productId} - Endpoint de compatibilidade deve redirecionar para endpoint principal")
    void shouldRedirectCompatibilityEndpoint() {
        // Given
        when(stockQueryService.findByProductId(productId1)).thenReturn(Optional.of(stockView1));
        when(stockQueryService.getMinimumStockLimit()).thenReturn(10);

        // When
        ResponseEntity<StockViewDTO> response = 
                stockQueryController.getStockByProductIdCompat(productId1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(stockQueryService, times(1)).findByProductId(productId1);
    }
}

