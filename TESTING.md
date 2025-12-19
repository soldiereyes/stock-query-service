# Guia de Testes - Comunicação entre Serviços

Este documento explica como testar a comunicação entre o `stock-query-service` e o `product-service`.

## Arquitetura de Comunicação

```
┌─────────────────────┐         ┌──────────────────────┐
│  product-service   │◄────────│ stock-query-service │
│  (Porta 8081)      │  HTTP   │  (Porta 8082)       │
│                    │         │                     │
│  GET /products/{id}│         │  GET /stocks/{id}   │
└─────────────────────┘         └──────────────────────┘
```

## Pré-requisitos

1. **product-service** deve estar rodando na porta 8081
2. **stock-query-service** deve estar rodando na porta 8082
3. Ambos devem estar na mesma rede Docker (se usando containers)

## Testes Manuais

### 1. Teste Básico de Comunicação

```bash
curl -X POST http://localhost:8081/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Produto Teste",
    "description": "Descrição do produto",
    "price": 99.90,
    "quantityInStock": 15
  }'

# Anote o ID retornado (ex: 550e8400-e29b-41d4-a716-446655440000)

# 2. Consultar estoque no stock-query-service
curl http://localhost:8082/stocks/550e8400-e29b-41d4-a716-446655440000
```

**Resposta esperada:**
```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "productName": "Produto Teste",
  "quantityAvailable": 15,
  "lastUpdated": "2025-12-19T16:30:00",
  "stockBelowMinimum": false
}
```

### 2. Teste com Estoque Abaixo do Mínimo

```bash
# 1. Criar produto com estoque baixo
curl -X POST http://localhost:8081/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Produto Estoque Baixo",
    "description": "Produto com estoque abaixo do mínimo",
    "price": 49.90,
    "quantityInStock": 5
  }'

# 2. Consultar estoque
curl http://localhost:8082/stocks/{productId}
```

**Resposta esperada:**
```json
{
  "productId": "...",
  "productName": "Produto Estoque Baixo",
  "quantityAvailable": 5,
  "lastUpdated": "2025-12-19T16:30:00",
  "stockBelowMinimum": true
}
```

### 3. Teste de Produto Não Encontrado

```bash
# Consultar com ID inexistente
curl http://localhost:8082/stocks/00000000-0000-0000-0000-000000000000
```

**Resposta esperada:**
```
HTTP 404 Not Found
```

### 4. Script de Teste Completo

```bash
#!/bin/bash

echo "=== Teste de Integração entre Serviços ==="

# 1. Verificar se os serviços estão rodando
echo "1. Verificando health dos serviços..."
curl -s http://localhost:8081/actuator/health | jq
curl -s http://localhost:8082/actuator/health | jq

# 2. Criar produto no product-service
echo -e "\n2. Criando produto no product-service..."
PRODUCT_RESPONSE=$(curl -s -X POST http://localhost:8081/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Produto Integração",
    "description": "Teste de integração",
    "price": 99.90,
    "quantityInStock": 8
  }')

echo $PRODUCT_RESPONSE | jq
PRODUCT_ID=$(echo $PRODUCT_RESPONSE | jq -r '.id')
echo "Product ID: $PRODUCT_ID"

# 3. Consultar estoque no stock-query-service
echo -e "\n3. Consultando estoque no stock-query-service..."
STOCK_RESPONSE=$(curl -s http://localhost:8082/stocks/$PRODUCT_ID)
echo $STOCK_RESPONSE | jq

# 4. Verificar se stockBelowMinimum está correto
STOCK_BELOW=$(echo $STOCK_RESPONSE | jq -r '.stockBelowMinimum')
if [ "$STOCK_BELOW" = "true" ]; then
  echo "✅ Estoque abaixo do mínimo detectado corretamente!"
else
  echo "⚠️  Estoque acima do mínimo"
fi
```

## Testes com Docker Compose

### Configuração

Certifique-se de que ambos os serviços estão na mesma rede Docker:

```yaml
networks:
  microservices-network:
    driver: bridge
```

### Executar Testes

```bash
# 1. Subir ambos os serviços
docker compose up -d

# 2. Aguardar inicialização
sleep 10

# 3. Executar testes
./test-integration.sh
```

## Testes com Postman

### Coleção de Testes

```json
{
  "info": {
    "name": "Stock Query Service - Integração",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. Criar Produto",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"name\": \"Produto Teste\",\n  \"description\": \"Descrição\",\n  \"price\": 99.90,\n  \"quantityInStock\": 5\n}"
        },
        "url": {
          "raw": "http://localhost:8081/products",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8081",
          "path": ["products"]
        }
      }
    },
    {
      "name": "2. Consultar Estoque",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8082/stocks/{{productId}}",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8082",
          "path": ["stocks", "{{productId}}"]
        }
      }
    }
  ]
}
```

## Verificação de Logs

### Ver logs de comunicação

```bash
# Logs do stock-query-service
docker logs stock-query-service | grep -i "product-service"

# Deve mostrar:
# "Consultando produto {id} no product-service"
# "Produto encontrado: {nome} - Estoque: {quantidade}"
```

## Troubleshooting

### Erro: Connection refused

**Problema:** stock-query-service não consegue conectar ao product-service

**Solução:**
1. Verificar se product-service está rodando: `curl http://localhost:8081/actuator/health`
2. Verificar URL configurada: `echo $PRODUCT_SERVICE_URL`
3. Se usando Docker, verificar se estão na mesma rede

### Erro: 404 Not Found

**Problema:** Produto não existe no product-service

**Solução:**
1. Criar o produto primeiro no product-service
2. Verificar se o ID está correto

### Erro: 503 Service Unavailable

**Problema:** product-service não está respondendo

**Solução:**
1. Verificar se product-service está rodando
2. Verificar logs do product-service
3. Verificar conectividade de rede


