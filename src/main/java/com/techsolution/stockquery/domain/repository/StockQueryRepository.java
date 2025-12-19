package com.techsolution.stockquery.domain.repository;

import com.techsolution.stockquery.domain.model.StockView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockQueryRepository {
    List<StockView> findAll();
    Optional<StockView> findByProductId(UUID productId);
}

