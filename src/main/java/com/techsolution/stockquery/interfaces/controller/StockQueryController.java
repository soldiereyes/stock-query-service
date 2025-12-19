package com.techsolution.stockquery.interfaces.controller;

import com.techsolution.stockquery.application.dto.StockViewDTO;
import com.techsolution.stockquery.application.service.StockQueryService;
import com.techsolution.stockquery.domain.model.StockView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stocks")
public class StockQueryController {

    private static final Logger logger = LoggerFactory.getLogger(StockQueryController.class);

    private final StockQueryService stockQueryService;

    public StockQueryController(StockQueryService stockQueryService) {
        this.stockQueryService = stockQueryService;
    }

    @GetMapping
    public ResponseEntity<List<StockViewDTO>> getAllStocks() {
        logger.info("Consultando todos os estoques");
        List<StockView> stocks = stockQueryService.findAll();
        List<StockViewDTO> dtos = stocks.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<StockViewDTO> getStockByProductId(@PathVariable UUID productId) {
        logger.info("Consultando estoque para produto: {}", productId);
        return stockQueryService.findByProductId(productId)
                .map(stock -> ResponseEntity.ok(toDTO(stock)))
                .orElse(ResponseEntity.notFound().build());
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

