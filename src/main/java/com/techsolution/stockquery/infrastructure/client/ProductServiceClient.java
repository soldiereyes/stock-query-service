package com.techsolution.stockquery.infrastructure.client;

import com.techsolution.stockquery.infrastructure.config.FeignLoggingConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
    name = "product-service", 
    url = "${product.service.url}",
    configuration = FeignLoggingConfig.class
)
public interface ProductServiceClient {

    @GetMapping("/products/{productId}")
    ResponseEntity<ProductDTO> getProductById(@PathVariable UUID productId);
}

