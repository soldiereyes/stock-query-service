package com.techsolution.stockquery.interfaces.controller;

import com.techsolution.stockquery.application.dto.StockViewDTO;
import com.techsolution.stockquery.application.service.StockQueryService;
import com.techsolution.stockquery.domain.model.StockView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class StockQueryController {

    private static final Logger logger = LoggerFactory.getLogger(StockQueryController.class);

    private final StockQueryService stockQueryService;

    public StockQueryController(StockQueryService stockQueryService) {
        this.stockQueryService = stockQueryService;
    }

    @GetMapping("/stocks/{productId}")
    public ResponseEntity<StockViewDTO> getStockByProductId(@PathVariable UUID productId) {
        long startTime = System.currentTimeMillis();
        logger.info("=== INÍCIO REQUEST ===");
        logger.info("GET /stocks/{} - Consultando estoque para produto", productId);
        
        try {
            ResponseEntity<StockViewDTO> response = stockQueryService.findByProductId(productId)
                    .map(stock -> {
                        StockViewDTO dto = toDTO(stock);
                        logger.info("Produto encontrado - ID: {}, Nome: {}, Estoque: {}, Abaixo do mínimo: {}", 
                                dto.getProductId(), dto.getProductName(), 
                                dto.getQuantityAvailable(), dto.getStockBelowMinimum());
                        return ResponseEntity.ok(dto);
                    })
                    .orElseGet(() -> {
                        logger.warn("Produto não encontrado - ID: {}", productId);
                        return ResponseEntity.notFound().build();
                    });
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("=== FIM REQUEST ===");
            logger.info("Status: {} - Tempo de resposta: {}ms", 
                    response.getStatusCode().value(), duration);
            
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("=== ERRO NA REQUEST ===");
            logger.error("Erro ao processar requisição para produto {} - Tempo: {}ms", 
                    productId, duration, e);
            throw e;
        }
    }

    /**
     * Endpoint de compatibilidade: /stock/{productId} (sem 's') para suportar frontend que usa este path
     */
    @GetMapping("/stock/{productId}")
    public ResponseEntity<StockViewDTO> getStockByProductIdCompat(@PathVariable UUID productId) {
        logger.info("GET /stock/{} - Endpoint de compatibilidade (redirecionando para /stocks/{})", productId, productId);
        // Reutiliza a mesma lógica do endpoint principal
        return getStockByProductId(productId);
    }

    /**
     * Endpoint de compatibilidade: /stock sem parâmetros (retorna erro informativo)
     */
    @GetMapping("/stock")
    public ResponseEntity<?> getAllStocks() {
        logger.info("GET /stock - Endpoint de compatibilidade chamado sem productId");
        logger.warn("Endpoint /stock requer productId. Use /stock/{productId} ou /stocks/{productId}");
        
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Bad Request",
            "message", "Endpoint /stock requer productId. Use /stock/{productId} ou /stocks/{productId} para consultar estoque de um produto específico.",
            "example", "/stocks/{productId}"
        ));
    }

    private StockViewDTO toDTO(StockView stock) {
        boolean isBelowMinimum = stock.isStockBelowMinimum(stockQueryService.getMinimumStockLimit());
        return new StockViewDTO(
                stock.getProductId(),
                stock.getProductName(),
                stock.getQuantityAvailable(),
                stock.getLastUpdated(),
                isBelowMinimum
        );
    }
}

