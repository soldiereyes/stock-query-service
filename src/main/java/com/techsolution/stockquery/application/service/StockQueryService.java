package com.techsolution.stockquery.application.service;

import com.techsolution.stockquery.domain.model.StockView;
import com.techsolution.stockquery.infrastructure.client.PageResponse;
import com.techsolution.stockquery.infrastructure.client.ProductDTO;
import com.techsolution.stockquery.infrastructure.client.ProductServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Busca todos os estoques paginados do product-service
     * Itera sobre todas as páginas para retornar todos os produtos
     * 
     * @param page Número da página (começa em 0)
     * @param size Tamanho da página (máximo: 100)
     * @return Lista de StockView de todos os produtos
     */
    public List<StockView> findAllStocks(Integer page, Integer size) {
        long startTime = System.currentTimeMillis();
        logger.info("--- Iniciando busca de todos os estoques (paginado) ---");
        logger.info("Parâmetros - page: {}, size: {}", page, size);
        
        List<StockView> allStocks = new ArrayList<>();
        int currentPage = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        
        // Limitar size ao máximo permitido (100)
        if (pageSize > 100) {
            logger.warn("Size {} excede o máximo permitido (100). Usando 100.", pageSize);
            pageSize = 100;
        }
        
        boolean hasMore = true;
        int totalPagesProcessed = 0;
        
        try {
            while (hasMore) {
                logger.info("Buscando página {} com tamanho {}", currentPage, pageSize);
                
                ResponseEntity<PageResponse<ProductDTO>> response = 
                        productServiceClient.getProducts(currentPage, pageSize);
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    PageResponse<ProductDTO> pageResponse = response.getBody();
                    
                    logger.info("Página {} recebida - Total de elementos: {}, Total de páginas: {}, É última: {}", 
                            currentPage, pageResponse.getTotalElements(), 
                            pageResponse.getTotalPages(), pageResponse.getLast());
                    
                    if (pageResponse.getContent() != null && !pageResponse.getContent().isEmpty()) {
                        logger.info("Processando {} produtos da página {}", 
                                pageResponse.getContent().size(), currentPage);
                        
                        for (ProductDTO product : pageResponse.getContent()) {
                            StockView stockView = createStockViewFromProduct(product);
                            allStocks.add(stockView);
                        }
                        
                        logger.info("Página {} processada - {} estoques adicionados (total acumulado: {})", 
                                currentPage, pageResponse.getContent().size(), allStocks.size());
                    } else {
                        logger.warn("Página {} retornou sem conteúdo", currentPage);
                    }
                    
                    // Verificar se há mais páginas
                    hasMore = !pageResponse.getLast();
                    if (hasMore) {
                        currentPage++;
                    }
                    totalPagesProcessed++;
                } else {
                    logger.warn("Resposta inválida do product-service - Status: {}", 
                            response.getStatusCode());
                    hasMore = false;
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("--- Busca de todos os estoques concluída ---");
            logger.info("Total de páginas processadas: {}", totalPagesProcessed);
            logger.info("Total de estoques retornados: {}", allStocks.size());
            logger.info("Tempo total: {}ms", duration);
            
            return allStocks;
        } catch (feign.FeignException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("--- ERRO Feign ao buscar produtos paginados ---");
            logger.error("Página: {}, Size: {}", currentPage, pageSize);
            logger.error("Status Code: {}", e.status());
            logger.error("Mensagem: {}", e.getMessage());
            logger.error("Tempo até erro: {}ms", duration);
            logger.error("Stack trace completo:", e);
            throw new RuntimeException("Erro ao buscar produtos paginados do product-service", e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("--- ERRO inesperado ao buscar produtos paginados ---");
            logger.error("Página: {}, Size: {}", currentPage, pageSize);
            logger.error("Tipo de exceção: {}", e.getClass().getName());
            logger.error("Mensagem: {}", e.getMessage());
            logger.error("Tempo até erro: {}ms", duration);
            logger.error("Stack trace completo:", e);
            throw new RuntimeException("Erro ao buscar produtos paginados", e);
        }
    }

    /**
     * Busca estoques paginados (retorna apenas uma página)
     * 
     * @param page Número da página (começa em 0)
     * @param size Tamanho da página (máximo: 100)
     * @return PageResponse com StockView da página solicitada
     */
    public PageResponse<StockView> findStocksPaginated(Integer page, Integer size) {
        long startTime = System.currentTimeMillis();
        logger.info("--- Iniciando busca paginada de estoques ---");
        logger.info("Parâmetros - page: {}, size: {}", page, size);
        
        int currentPage = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        
        // Limitar size ao máximo permitido (100)
        if (pageSize > 100) {
            logger.warn("Size {} excede o máximo permitido (100). Usando 100.", pageSize);
            pageSize = 100;
        }
        
        try {
            logger.info("Buscando página {} com tamanho {}", currentPage, pageSize);
            
            ResponseEntity<PageResponse<ProductDTO>> response = 
                    productServiceClient.getProducts(currentPage, pageSize);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                PageResponse<ProductDTO> productPageResponse = response.getBody();
                
                logger.info("Página {} recebida do product-service - Total: {}, Páginas: {}", 
                        currentPage, productPageResponse.getTotalElements(), 
                        productPageResponse.getTotalPages());
                
                List<StockView> stockViews = new ArrayList<>();
                
                if (productPageResponse.getContent() != null) {
                    for (ProductDTO product : productPageResponse.getContent()) {
                        StockView stockView = createStockViewFromProduct(product);
                        stockViews.add(stockView);
                    }
                }
                
                PageResponse<StockView> stockPageResponse = new PageResponse<>(
                        stockViews,
                        productPageResponse.getPage(),
                        productPageResponse.getSize(),
                        productPageResponse.getTotalElements(),
                        productPageResponse.getTotalPages(),
                        productPageResponse.getFirst(),
                        productPageResponse.getLast()
                );
                
                long duration = System.currentTimeMillis() - startTime;
                logger.info("--- Busca paginada concluída ---");
                logger.info("Estoques retornados: {} de {}", stockViews.size(), 
                        productPageResponse.getTotalElements());
                logger.info("Tempo: {}ms", duration);
                
                return stockPageResponse;
            } else {
                logger.warn("Resposta inválida do product-service - Status: {}", 
                        response.getStatusCode());
                throw new RuntimeException("Resposta inválida do product-service");
            }
        } catch (feign.FeignException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("--- ERRO Feign ao buscar produtos paginados ---");
            logger.error("Página: {}, Size: {}", currentPage, pageSize);
            logger.error("Status Code: {}", e.status());
            logger.error("Mensagem: {}", e.getMessage());
            logger.error("Tempo até erro: {}ms", duration);
            logger.error("Stack trace completo:", e);
            throw new RuntimeException("Erro ao buscar produtos paginados do product-service", e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("--- ERRO inesperado ao buscar produtos paginados ---");
            logger.error("Página: {}, Size: {}", currentPage, pageSize);
            logger.error("Tipo de exceção: {}", e.getClass().getName());
            logger.error("Mensagem: {}", e.getMessage());
            logger.error("Tempo até erro: {}ms", duration);
            logger.error("Stack trace completo:", e);
            throw new RuntimeException("Erro ao buscar produtos paginados", e);
        }
    }

    /**
     * Cria um StockView a partir de um ProductDTO
     */
    private StockView createStockViewFromProduct(ProductDTO product) {
        if (product.getQuantityInStock() == null) {
            logger.warn("ATENÇÃO: quantityInStock é NULL para produto {} - {}", 
                    product.getId(), product.getName());
        }
        
        StockView stockView = new StockView(
                product.getId(),
                product.getName(),
                product.getQuantityInStock(),
                LocalDateTime.now()
        );
        
        boolean isBelowMinimum = stockView.isStockBelowMinimum(MINIMUM_STOCK_LIMIT);
        logger.debug("Produto {} - Estoque: {}, Abaixo do mínimo: {}", 
                product.getId(), product.getQuantityInStock(), isBelowMinimum);
        
        return stockView;
    }
}

