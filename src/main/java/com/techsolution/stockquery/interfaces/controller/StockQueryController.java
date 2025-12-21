package com.techsolution.stockquery.interfaces.controller;

import com.techsolution.stockquery.application.dto.PageResponseDTO;
import com.techsolution.stockquery.application.dto.StockViewDTO;
import com.techsolution.stockquery.application.service.StockQueryService;
import com.techsolution.stockquery.domain.model.StockView;
import com.techsolution.stockquery.infrastructure.client.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
     * Endpoint GET /stocks - Lista todos os estoques com suporte a paginação
     * 
     * @param page Número da página (começa em 0, padrão: 0)
     * @param size Tamanho da página (padrão: 20, máximo: 100)
     * @return Resposta paginada com lista de estoques
     */
    @GetMapping("/stocks")
    public ResponseEntity<PageResponseDTO<StockViewDTO>> getAllStocksPaginated(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {
        
        long startTime = System.currentTimeMillis();
        logger.info("=== INÍCIO REQUEST ===");
        logger.info("GET /stocks - Buscando estoques paginados - page: {}, size: {}", page, size);
        
        try {
            if (page == null) {
                page = 0;
            }
            if (size == null) {
                size = 20;
            }
            

            if (page < 0) {
                logger.warn("Parâmetro 'page' inválido: {}. Usando 0.", page);
                page = 0;
            }
            
            if (size < 1 || size > 100) {
                logger.warn("Parâmetro 'size' inválido: {}. Deve estar entre 1 e 100. Usando 20.", size);
                size = 20;
            }
            
            PageResponse<StockView> stockPageResponse = stockQueryService.findStocksPaginated(page, size);
            
            List<StockViewDTO> stockDTOs = stockPageResponse.getContent().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            
            PageResponseDTO<StockViewDTO> response = new PageResponseDTO<>(
                    stockDTOs,
                    stockPageResponse.getPage(),
                    stockPageResponse.getSize(),
                    stockPageResponse.getTotalElements(),
                    stockPageResponse.getTotalPages(),
                    stockPageResponse.getFirst(),
                    stockPageResponse.getLast()
            );
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("=== FIM REQUEST ===");
            logger.info("Status: 200 - Tempo de resposta: {}ms", duration);
            logger.info("Estoques retornados: {} de {} (página {} de {})", 
                    stockDTOs.size(), stockPageResponse.getTotalElements(), 
                    stockPageResponse.getPage() + 1, stockPageResponse.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("=== ERRO NA REQUEST ===");
            logger.error("Erro ao buscar estoques paginados - page: {}, size: {} - Tempo: {}ms", 
                    page, size, duration, e);
            throw e;
        }
    }

    /**
     * Endpoint GET /stock - Lista todos os estoques (carrega todas as páginas)
     * Endpoint de compatibilidade para frontend que espera todos os estoques de uma vez
     * 
     * @param page Parâmetro opcional (ignorado, carrega todas as páginas)
     * @param size Parâmetro opcional (usado para tamanho de página durante iteração)
     * @return Lista completa de todos os estoques
     */
    @GetMapping("/stock")
    public ResponseEntity<?> getAllStocks(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        
        long startTime = System.currentTimeMillis();
        logger.info("=== INÍCIO REQUEST ===");
        logger.info("GET /stock - Buscando TODOS os estoques (carregamento completo)");
        logger.info("Parâmetros recebidos - page: {} (ignorado), size: {} (usado para iteração)", page, size);
        
        try {
            // Usar size padrão de 20 se não fornecido, ou o fornecido (limitado a 100)
            int pageSize = size != null ? size : 20;
            if (pageSize > 100) {
                pageSize = 100;
            }
            
            List<StockView> allStocks = stockQueryService.findAllStocks(0, pageSize);
            
            List<StockViewDTO> stockDTOs = allStocks.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("=== FIM REQUEST ===");
            logger.info("Status: 200 - Tempo de resposta: {}ms", duration);
            logger.info("Total de estoques retornados: {}", stockDTOs.size());
            
            return ResponseEntity.ok(stockDTOs);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("=== ERRO NA REQUEST ===");
            logger.error("Erro ao buscar todos os estoques - Tempo: {}ms", duration, e);
            throw e;
        }
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

