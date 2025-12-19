# Stock Query Service

Microserviço responsável exclusivamente por consultas de estoque, seguindo os princípios de CQRS (lado Query) e DDD (Domain-Driven Design).

## Arquitetura

Este serviço implementa uma arquitetura baseada em DDD com separação clara de responsabilidades:

- **Domain**: Modelos de domínio (sem dependências do Spring)
- **Application**: Serviços de aplicação e DTOs
- **Infrastructure**: Cliente HTTP (Feign) e configurações
- **Interfaces**: Controllers REST

**Nota:** Este serviço não possui banco de dados próprio. Ele consulta o `product-service` em tempo real via HTTP para obter informações dos produtos.

## Tecnologias

- Java 21
- Spring Boot 4.0.1
- Spring Cloud OpenFeign (comunicação HTTP entre serviços)
- Docker & Docker Compose

## Estrutura do Projeto

```
stock-query-service
└── src/main/java/com/techsolution/stockquery
    ├── application
    │   ├── dto
    │   │   └── StockViewDTO.java
    │   └── service
    │       └── StockQueryService.java
    ├── domain
    │   └── model
    │       └── StockView.java
    ├── infrastructure
    │   ├── client
    │   │   ├── ProductDTO.java
    │   │   └── ProductServiceClient.java
    │   └── config
    │       └── GlobalExceptionHandler.java
    └── interfaces
        └── controller
            └── StockQueryController.java
```

## Comunicação entre Serviços

Este serviço se comunica com o **product-service** para obter informações dos produtos:

1. Recebe requisições com o ID do produto
2. Consulta o **product-service** via HTTP (Feign Client) para obter dados do produto
3. Retorna os dados agregados de estoque e informa se está abaixo do limite mínimo (< 10 unidades)

### Arquitetura de Comunicação

```
Cliente → stock-query-service (8082) → product-service (8081)
         GET /stocks/{productId}      GET /products/{productId}
```

## API REST

### Endpoints

#### GET /stocks/{productId}
Retorna o estoque de um produto específico.

**Resposta:**
```json
{
  "productId": "uuid",
  "productName": "Nome do Produto",
  "quantityAvailable": 5,
  "lastUpdated": "2024-01-01T10:00:00",
  "stockBelowMinimum": true
}
```

**Status Codes:**
- `200 OK`: Produto encontrado
- `404 Not Found`: Produto não encontrado

## Executando o Serviço

### Com Docker Compose

```bash
docker compose up --build
```

O serviço estará disponível em `http://localhost:8082`

**Importante:** Certifique-se de que o `product-service` está rodando e acessível na URL configurada (padrão: `http://localhost:8081`).

### Localmente

Execute:
```bash
./mvnw spring-boot:run
```

## Configurações

As configurações principais estão em `application.properties`:

- Porta: 8082
- Product Service URL: `product.service.url` (padrão: `http://localhost:8081`)
- Configurável via variável de ambiente: `PRODUCT_SERVICE_URL`

## Características

- ✅ READ ONLY: Nenhuma operação de escrita
- ✅ DDD: Separação clara de camadas
- ✅ Clean Code: Código limpo e bem organizado
- ✅ Exception Handling: Tratamento global de erros
- ✅ Logging: Logs estruturados
- ✅ Docker: Containerização completa
- ✅ Feign Client: Comunicação HTTP com product-service
- ✅ Integração: Consulta product-service em tempo real para obter dados de produtos
- ✅ Sem Banco de Dados: Não requer banco próprio, consulta product-service diretamente

## Limite Mínimo de Estoque

O serviço considera estoque abaixo do mínimo quando a quantidade disponível é menor que **10 unidades**. Este valor está configurado no `StockQueryService`.

