package com.techsolution.stockquery.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class StockView {
    private UUID productId;
    private String productName;
    private Integer quantityAvailable;
    private LocalDateTime lastUpdated;

    public StockView() {
    }

    public StockView(UUID productId, String productName, Integer quantityAvailable, LocalDateTime lastUpdated) {
        this.productId = productId;
        this.productName = productName;
        this.quantityAvailable = quantityAvailable;
        this.lastUpdated = lastUpdated;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(Integer quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isStockBelowMinimum(int minimumLimit) {
        return quantityAvailable < minimumLimit;
    }
}

