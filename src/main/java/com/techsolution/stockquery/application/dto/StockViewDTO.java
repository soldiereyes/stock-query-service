package com.techsolution.stockquery.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class StockViewDTO {
    private UUID productId;
    private String productName;
    private Integer quantityAvailable;
    private LocalDateTime lastUpdated;
    private Boolean stockBelowMinimum;

    public StockViewDTO() {
    }

    public StockViewDTO(UUID productId, String productName, Integer quantityAvailable, 
                       LocalDateTime lastUpdated, Boolean stockBelowMinimum) {
        this.productId = productId;
        this.productName = productName;
        this.quantityAvailable = quantityAvailable;
        this.lastUpdated = lastUpdated;
        this.stockBelowMinimum = stockBelowMinimum;
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

    public Boolean getStockBelowMinimum() {
        return stockBelowMinimum;
    }

    public void setStockBelowMinimum(Boolean stockBelowMinimum) {
        this.stockBelowMinimum = stockBelowMinimum;
    }
}

