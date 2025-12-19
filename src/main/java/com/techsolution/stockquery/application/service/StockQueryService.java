package com.techsolution.stockquery.application.service;

import com.techsolution.stockquery.domain.model.StockView;
import com.techsolution.stockquery.infrastructure.client.ProductDTO;
import com.techsolution.stockquery.infrastructure.client.ProductServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class StockQueryService {

    private static final Logger logger = LoggerFactory.getLogger(StockQueryService.class);
    private static final int MINIMUM_STOCK_LIMIT = 10;

    private final ProductServiceClient productServiceClient;

    public StockQueryService(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
    }

    public Optional<StockView> findByProductId(UUID productId) {
        long startTime = System.currentTimeMillis();
        logger.info("--- Iniciando consulta ao product-service ---");
        logger.info("ProductId: {}", productId);
        
        try {
            logger.debug("Fazendo chamada HTTP GET /products/{}", productId);
            ResponseEntity<ProductDTO> response = productServiceClient.getProductById(productId);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("Resposta recebida do product-service - Status: {}, Tempo: {}ms", 
                    response.getStatusCode(), duration);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ProductDTO product = response.getBody();
                
                logger.info("Dados do produto recebidos:");
                logger.info("  - ID: {}", product.getId());
                logger.info("  - Nome: {}", product.getName());
                logger.info("  - Estoque: {}", product.getQuantityInStock());
                logger.info("  - Preço: {}", product.getPrice());
                
                if (product.getQuantityInStock() == null) {
                    logger.error("ATENÇÃO: quantityInStock é NULL! Verifique o mapeamento do campo 'stockQuantity'");
                }
                
                StockView stockView = new StockView(
                        product.getId(),
                        product.getName(),
                        product.getQuantityInStock(),
                        LocalDateTime.now()
                );
                
                boolean isBelowMinimum = stockView.isStockBelowMinimum(MINIMUM_STOCK_LIMIT);
                logger.info("Análise de estoque - Quantidade: {}, Limite mínimo: {}, Abaixo do mínimo: {}", 
                        product.getQuantityInStock(), MINIMUM_STOCK_LIMIT, isBelowMinimum);
                
                logger.info("--- Consulta ao product-service concluída com sucesso ---");
                return Optional.of(stockView);
            } else {
                logger.warn("--- Produto não encontrado no product-service ---");
                logger.warn("ProductId: {}, Status Code: {}", productId, response.getStatusCode());
                return Optional.empty();
            }
        } catch (feign.FeignException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("--- ERRO Feign ao consultar product-service ---");
            logger.error("ProductId: {}", productId);
            logger.error("Status Code: {}", e.status());
            logger.error("Mensagem: {}", e.getMessage());
            logger.error("Tempo até erro: {}ms", duration);
            logger.error("Request URL: GET {}/products/{}", 
                    System.getProperty("product.service.url", "http://localhost:8081"), productId);
            logger.error("Stack trace completo:", e);
            throw new RuntimeException("Erro ao consultar informações do produto no product-service", e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("--- ERRO inesperado ao consultar product-service ---");
            logger.error("ProductId: {}", productId);
            logger.error("Tipo de exceção: {}", e.getClass().getName());
            logger.error("Mensagem: {}", e.getMessage());
            logger.error("Tempo até erro: {}ms", duration);
            logger.error("Stack trace completo:", e);
            throw new RuntimeException("Erro ao consultar informações do produto", e);
        }
    }

    public int getMinimumStockLimit() {
        return MINIMUM_STOCK_LIMIT;
    }
}

