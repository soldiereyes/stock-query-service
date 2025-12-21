package com.techsolution.stockquery.infrastructure.client;

import com.techsolution.stockquery.infrastructure.config.FeignLoggingConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(
    name = "product-service", 
    url = "${product.service.url}",
    configuration = FeignLoggingConfig.class
)
public interface ProductServiceClient {

    @GetMapping("/products/{productId}")
    ResponseEntity<ProductDTO> getProductById(@PathVariable UUID productId);

    /**
     * Busca produtos paginados do product-service
     * @param page Número da página (começa em 0, padrão: 0)
     * @param size Tamanho da página (padrão: 20, máximo: 100)
     * @return Resposta paginada com lista de produtos
     */
    @GetMapping("/products")
    ResponseEntity<PageResponse<ProductDTO>> getProducts(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    );
}

