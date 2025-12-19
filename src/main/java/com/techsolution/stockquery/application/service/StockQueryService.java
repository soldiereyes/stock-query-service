package com.techsolution.stockquery.application.service;

import com.techsolution.stockquery.domain.model.StockView;
import com.techsolution.stockquery.domain.repository.StockQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StockQueryService {

    private static final int MINIMUM_STOCK_LIMIT = 10;

    private final StockQueryRepository stockQueryRepository;

    public StockQueryService(StockQueryRepository stockQueryRepository) {
        this.stockQueryRepository = stockQueryRepository;
    }

    @Transactional(readOnly = true)
    public List<StockView> findAll() {
        return stockQueryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<StockView> findByProductId(UUID productId) {
        return stockQueryRepository.findByProductId(productId);
    }

    public int getMinimumStockLimit() {
        return MINIMUM_STOCK_LIMIT;
    }
}

