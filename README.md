# Stock Query Service

Microserviço responsável exclusivamente por consultas de estoque, seguindo os princípios de CQRS (lado Query) e DDD (Domain-Driven Design).

## Arquitetura

Este serviço implementa uma arquitetura baseada em DDD com separação clara de responsabilidades:

- **Domain**: Modelos e interfaces de repositório (sem dependências do Spring)
- **Application**: Serviços de aplicação e DTOs
- **Infrastructure**: Implementações de persistência (JPA) e configurações
- **Interfaces**: Controllers REST

## Tecnologias

- Java 21
- Spring Boot 4.0.1
- PostgreSQL
- Docker & Docker Compose
- JPA/Hibernate

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
    │   ├── model
    │   │   └── StockView.java
    │   └── repository
    │       └── StockQueryRepository.java
    ├── infrastructure
    │   ├── config
    │   │   ├── GlobalExceptionHandler.java
    │   │   └── JpaConfig.java
    │   └── persistence
    │       ├── JpaStockQueryRepository.java
    │       └── StockViewEntity.java
    └── interfaces
        └── controller
            └── StockQueryController.java
```

## Banco de Dados

O serviço utiliza PostgreSQL. A tabela `stock_view` é criada automaticamente pelo Hibernate na inicialização da aplicação.

### Estrutura da Tabela

- `product_id` (UUID, PK)
- `product_name` (VARCHAR)
- `quantity_available` (INTEGER)
- `last_updated` (TIMESTAMP)

## API REST

### Endpoints

#### GET /stocks
Retorna todos os estoques disponíveis.

**Resposta:**
```json
[
  {
    "productId": "uuid",
    "productName": "Nome do Produto",
    "quantityAvailable": 15,
    "lastUpdated": "2024-01-01T10:00:00",
    "stockBelowMinimum": false
  }
]
```

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

**Nota:** A tabela `stock_view` será criada automaticamente pelo Hibernate na primeira inicialização.

### Localmente

1. Certifique-se de ter PostgreSQL rodando na porta 5433 (ou ajuste as configurações)
2. Execute:
```bash
./mvnw spring-boot:run
```

## Configurações

As configurações principais estão em `application.properties`:

- Porta: 8082
- Banco de dados: `stock_db`
- Hibernate: `ddl-auto=update` (cria/atualiza tabelas automaticamente)

## Características

- ✅ READ ONLY: Nenhuma operação de escrita
- ✅ DDD: Separação clara de camadas
- ✅ Clean Code: Código limpo e bem organizado
- ✅ Exception Handling: Tratamento global de erros
- ✅ Logging: Logs estruturados
- ✅ Docker: Containerização completa
- ✅ Hibernate: Criação automática de tabelas

## Limite Mínimo de Estoque

O serviço considera estoque abaixo do mínimo quando a quantidade disponível é menor que **10 unidades**. Este valor está configurado no `StockQueryService`.

