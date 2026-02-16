# 📋 CALIDAD - Documentación de Pruebas de Calidad

> **Proyecto**: Sistema de Pedidos de Restaurante  
> **Última actualización**: 2026-02-13  
> **Versión**: 1.0

## 📑 Índice

1. [Introducción](#introducción)
2. [Niveles de Pruebas](#niveles-de-pruebas)
3. [Pruebas Unitarias](#1-pruebas-unitarias)
4. [Pruebas de Integración](#2-pruebas-de-integración)
5. [Pruebas Funcionales](#3-pruebas-funcionales)
6. [Pruebas de Contrato](#4-pruebas-de-contrato)
7. [Pruebas de Calidad No Funcional](#5-pruebas-de-calidad-no-funcional)
8. [Pruebas de Infraestructura](#6-pruebas-de-infraestructura)
9. [Estrategia de Testing](#estrategia-de-testing)
10. [Ejecución Completa](#ejecución-completa)
11. [Métricas de Calidad](#métricas-de-calidad)

---

## Introducción

Este documento describe todas las pruebas de calidad implementadas para el Sistema de Pedidos de Restaurante, un sistema fullstack con arquitectura de microservicios que incluye:

- **Frontend React**: Interfaz de cliente y cocina
- **Order Service**: API REST para gestión de pedidos (Spring Boot)
- **Kitchen Worker**: Procesamiento asíncrono de pedidos (Spring Boot)
- **RabbitMQ**: Mensajería asíncrona entre servicios
- **PostgreSQL**: Base de datos relacional

### Objetivo de las Pruebas

Garantizar que el sistema cumple con todos los requisitos funcionales y no funcionales, mantiene la integridad de los datos, y proporciona una experiencia confiable para usuarios y cocina.

---

## Niveles de Pruebas

El sistema implementa una estrategia de pruebas en múltiples niveles siguiendo la pirámide de testing:

```
                    /\
                   /  \
                  / E2E \
                 /--------\
                /Integración\
               /-------------\
              /   Unitarias   \
             /-----------------\
```

1. **Pruebas Unitarias**: Validan componentes individuales aislados
2. **Pruebas de Integración**: Validan interacción entre componentes
3. **Pruebas Funcionales**: Validan flujos de negocio completos
4. **Pruebas de Contrato**: Validan interfaces entre servicios
5. **Pruebas No Funcionales**: Validan rendimiento, seguridad, resiliencia
6. **Pruebas de Infraestructura**: Validan despliegue y configuración

---

## 1. Pruebas Unitarias

### 1.1 Order Service - Pruebas Unitarias

**Ubicación**: `order-service/src/test/java/com/restaurant/orderservice/`

#### 1.1.1 OrderServiceTest

**Archivo**: `service/OrderServiceTest.java`

**Cobertura**: 
- Creación de pedidos
- Validación de datos de entrada
- Manejo de productos inexistentes
- Actualización de estados
- Recuperación y filtrado de pedidos

**Casos de Prueba**:

```java
✅ testCreateOrder_Success() - Creación exitosa de pedido
✅ testCreateOrder_InvalidTableId() - Validación de mesa inválida
✅ testCreateOrder_ProductNotFound() - Producto no existente
✅ testCreateOrder_EmptyItems() - Items vacíos
✅ testGetAllOrders_Success() - Listar todos los pedidos
✅ testGetOrdersByStatus_Success() - Filtrar por estado
✅ testGetOrderById_Success() - Obtener pedido por ID
✅ testGetOrderById_NotFound() - Pedido no encontrado
✅ testUpdateOrderStatus_Success() - Actualizar estado
✅ testUpdateOrderStatus_NotFound() - Actualizar estado de pedido inexistente
```

**Ejecución**:
```bash
cd order-service
mvn test -Dtest=OrderServiceTest
```

**Resultado Esperado**: 10/10 pruebas pasan

#### 1.1.2 MenuServiceTest

**Archivo**: `service/MenuServiceTest.java`

**Cobertura**: 
- Obtención del menú de productos
- Cache y rendimiento

**Casos de Prueba**:
```java
✅ testGetAllProducts_Success() - Obtener lista completa de productos
✅ testGetAllProducts_EmptyList() - Lista vacía
```

**Ejecución**:
```bash
cd order-service
mvn test -Dtest=MenuServiceTest
```

#### 1.1.3 OrderControllerTest

**Archivo**: `controller/OrderControllerTest.java`

**Cobertura**: 
- Endpoints REST
- Serialización/deserialización JSON
- Códigos de estado HTTP
- Manejo de errores

**Casos de Prueba**:
```java
✅ testCreateOrder_Returns201() - POST /orders retorna 201
✅ testCreateOrder_Returns400_InvalidInput() - Validación de entrada
✅ testGetAllOrders_Returns200() - GET /orders retorna 200
✅ testGetOrderById_Returns200() - GET /orders/{id} retorna 200
✅ testGetOrderById_Returns404() - Pedido no encontrado retorna 404
✅ testUpdateOrderStatus_Returns200() - PATCH /orders/{id}/status retorna 200
```

**Ejecución**:
```bash
cd order-service
mvn test -Dtest=OrderControllerTest
```

#### 1.1.4 MenuControllerTest

**Archivo**: `controller/MenuControllerTest.java`

**Cobertura**: Endpoint de menú

**Ejecución**:
```bash
cd order-service
mvn test -Dtest=MenuControllerTest
```

#### 1.1.5 GlobalExceptionHandlerTest

**Archivo**: `exception/GlobalExceptionHandlerTest.java`

**Cobertura**: 
- Manejo centralizado de excepciones
- Códigos de error apropiados
- Mensajes de error descriptivos

**Casos de Prueba**:
```java
✅ testHandleOrderNotFoundException() - 404 para pedido no encontrado
✅ testHandleProductNotFoundException() - 404 para producto no encontrado
✅ testHandleInvalidOrderException() - 400 para datos inválidos
✅ testHandleEventPublicationException() - 500 para error de publicación
✅ testHandleGenericException() - 500 para errores genéricos
```

**Ejecución**:
```bash
cd order-service
mvn test -Dtest=GlobalExceptionHandlerTest
```

#### 1.1.6 PublishOrderPlacedEventCommandTest

**Archivo**: `service/command/PublishOrderPlacedEventCommandTest.java`

**Cobertura**: 
- Publicación de eventos de dominio
- Patrón Command

**Ejecución**:
```bash
cd order-service
mvn test -Dtest=PublishOrderPlacedEventCommandTest
```

#### 1.1.7 RabbitOrderPlacedEventPublisherTest

**Archivo**: `infrastructure/messaging/RabbitOrderPlacedEventPublisherTest.java`

**Cobertura**: 
- Integración con RabbitMQ
- Serialización de eventos
- Manejo de errores de publicación

**Ejecución**:
```bash
cd order-service
mvn test -Dtest=RabbitOrderPlacedEventPublisherTest
```

#### 1.1.8 KitchenSecurityInterceptorTest

**Archivo**: `security/KitchenSecurityInterceptorTest.java`

**Cobertura**: 
- Autenticación de cocina
- Validación de token/PIN
- Control de acceso

**Ejecución**:
```bash
cd order-service
mvn test -Dtest=KitchenSecurityInterceptorTest
```

#### 1.1.9 OrderServiceApplicationTests

**Archivo**: `OrderServiceApplicationTests.java`

**Cobertura**: 
- Carga del contexto Spring
- Configuración de beans

**Ejecución**:
```bash
cd order-service
mvn test -Dtest=OrderServiceApplicationTests
```

### 1.2 Kitchen Worker - Pruebas Unitarias

**Ubicación**: `kitchen-worker/src/test/java/com/restaurant/kitchenworker/`

#### 1.2.1 OrderProcessingServiceTest

**Archivo**: `service/OrderProcessingServiceTest.java`

**Cobertura**: 
- Procesamiento de eventos de pedido
- Actualización de estado a IN_PREPARATION
- Manejo de pedidos no existentes

**Casos de Prueba**:
```java
✅ testProcessOrderPlaced_Success() - Procesamiento exitoso
✅ testProcessOrderPlaced_OrderNotFound() - Pedido no encontrado
✅ testProcessOrderPlaced_UpdatesStatusToInPreparation() - Estado correcto
✅ testProcessOrderPlaced_UpdatesProcessingTimestamp() - Timestamp actualizado
```

**Ejecución**:
```bash
cd kitchen-worker
mvn test -Dtest=OrderProcessingServiceTest
```

#### 1.2.2 OrderEventListenerTest

**Archivo**: `listener/OrderEventListenerTest.java`

**Cobertura**: 
- Consumo de mensajes de RabbitMQ
- Deserialización de eventos
- Delegación al servicio

**Ejecución**:
```bash
cd kitchen-worker
mvn test -Dtest=OrderEventListenerTest
```

#### 1.2.3 OrderPlacedEventValidatorTest

**Archivo**: `event/OrderPlacedEventValidatorTest.java`

**Cobertura**: 
- Validación de eventos entrantes
- Campos requeridos
- Formato de datos

**Ejecución**:
```bash
cd kitchen-worker
mvn test -Dtest=OrderPlacedEventValidatorTest
```

#### 1.2.4 KitchenWorkerApplicationTests

**Archivo**: `KitchenWorkerApplicationTests.java`

**Cobertura**: 
- Carga del contexto Spring
- Configuración de RabbitMQ

**Ejecución**:
```bash
cd kitchen-worker
mvn test -Dtest=KitchenWorkerApplicationTests
```

### 1.3 Ejecución de Todas las Pruebas Unitarias

```bash
# Order Service
cd order-service
mvn test

# Kitchen Worker
cd kitchen-worker
mvn test

# Desde la raíz del proyecto
mvn test
```

**Resultados Esperados**:
- Order Service: 9 clases de prueba, ~30+ pruebas
- Kitchen Worker: 4 clases de prueba, ~10+ pruebas
- Total: **40+ pruebas unitarias** ✅

---

## 2. Pruebas de Integración

### 2.1 Pruebas de Integración de Base de Datos

#### 2.1.1 Migración de Base de Datos (Flyway)

**Ubicación**: `order-service/src/main/resources/db/migration/`

**Archivos**:
- `V1__create_products_table.sql`
- `V2__create_orders_table.sql`
- `V3__create_order_items_table.sql`
- `V4__insert_sample_products.sql`

**Validación**:
```bash
# Iniciar PostgreSQL
docker-compose up -d postgres

# Verificar migraciones
docker exec -it restaurant-postgres psql -U restaurant_user -d restaurant_db -c "\dt"

# Verificar productos
docker exec -it restaurant-postgres psql -U restaurant_user -d restaurant_db -c "SELECT * FROM products;"
```

**Resultado Esperado**:
```
✅ Tabla products creada
✅ Tabla orders creada
✅ Tabla order_items creada
✅ Datos de ejemplo insertados (3 productos)
```

#### 2.1.2 Repositorios JPA

**Pruebas**:
```bash
# Validar repositorios con el sistema completo
docker-compose up -d postgres order-service

# Crear pedido y verificar en BD
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": 5,
    "items": [
      {"productId": 1, "quantity": 2, "note": "Sin cebolla"}
    ]
  }'

# Verificar en base de datos
docker exec -it restaurant-postgres psql -U restaurant_user -d restaurant_db \
  -c "SELECT id, table_id, status, created_at FROM orders ORDER BY created_at DESC LIMIT 1;"
```

### 2.2 Pruebas de Integración con RabbitMQ

#### 2.2.1 Publicación y Consumo de Eventos

**Componentes**:
- Publisher: `OrderEventPublisher` (Order Service)
- Consumer: `OrderEventListener` (Kitchen Worker)
- Queue: `orders.placed`
- Exchange: `orders.exchange`

**Validación Manual**:
```bash
# 1. Iniciar sistema completo
docker-compose up -d

# 2. Crear un pedido
ORDER_ID=$(curl -s -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": 5,
    "items": [{"productId": 1, "quantity": 1}]
  }' | jq -r '.id')

echo "Order ID: $ORDER_ID"

# 3. Verificar logs de publicación
docker-compose logs order-service | grep "Successfully published"

# 4. Verificar logs de consumo
docker-compose logs kitchen-worker | grep "Received order placed event"

# 5. Verificar estado actualizado
curl http://localhost:8080/orders/$ORDER_ID | jq '.status'
# Debe ser "IN_PREPARATION"

# 6. Verificar colas en RabbitMQ
docker exec restaurant-rabbitmq rabbitmqctl list_queues name messages_ready messages_unacknowledged
```

**Resultado Esperado**:
```
✅ Evento publicado a RabbitMQ
✅ Evento consumido por Kitchen Worker
✅ Estado del pedido actualizado a IN_PREPARATION
✅ Cola vacía (mensajes procesados)
```

#### 2.2.2 Dead Letter Queue

**Validación**:
```bash
# Verificar configuración de DLQ
docker exec restaurant-rabbitmq rabbitmqctl list_queues name arguments

# Simular error de procesamiento (requiere modificación temporal del código)
# La DLQ debe capturar mensajes fallidos
```

### 2.3 Pruebas de Integración de APIs REST

#### 2.3.1 Swagger/OpenAPI

**Acceso**: http://localhost:8080/swagger-ui.html

**Validación**:
```bash
# Iniciar Order Service
docker-compose up -d order-service

# Acceder a Swagger UI
curl http://localhost:8080/v3/api-docs | jq '.'
```

**Resultado Esperado**:
✅ Documentación OpenAPI disponible
✅ Todos los endpoints documentados

#### 2.3.2 Endpoints REST

**Menu API**:
```bash
# GET /menu - Obtener lista de productos
curl http://localhost:8080/menu | jq '.'
```

**Order API**:
```bash
# POST /orders - Crear pedido
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": 5,
    "items": [
      {"productId": 1, "quantity": 2, "note": "Sin cebolla"},
      {"productId": 3, "quantity": 1}
    ]
  }' | jq '.'

# GET /orders - Listar todos los pedidos
curl http://localhost:8080/orders | jq '.'

# GET /orders?status=PENDING - Filtrar por estado
curl "http://localhost:8080/orders?status=PENDING" | jq '.'

# GET /orders/{id} - Obtener pedido específico
curl http://localhost:8080/orders/{ORDER_ID} | jq '.'

# PATCH /orders/{id}/status - Actualizar estado
curl -X PATCH http://localhost:8080/orders/{ORDER_ID}/status \
  -H "Content-Type: application/json" \
  -d '{"status": "READY"}' | jq '.'
```

---

## 3. Pruebas Funcionales

### 3.1 Flujo Completo del Cliente

#### F-01: Selección de Mesa

**Requisito**: US-01, RF-01

**Pasos**:
1. Acceder a http://localhost:5173
2. Ingresar número de mesa (1-20)
3. Clic en "Continuar"

**Validación API**:
```bash
# Mesa válida (1-20)
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"tableId": 5, "items": [{"productId": 1, "quantity": 1}]}'
# Esperado: 200 OK

# Mesa inválida (<1 o >20)
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"tableId": 0, "items": [{"productId": 1, "quantity": 1}]}'
# Esperado: 400 Bad Request
```

**Resultado Esperado**: ✅ Validación correcta de rango de mesa

#### F-02: Visualización del Menú

**Requisito**: US-02, RF-02

**Validación**:
```bash
# Obtener menú
curl http://localhost:8080/menu | jq '.[] | {id, name, price}'
```

**Resultado Esperado**:
```json
✅ Lista de 3 productos:
[
  {"id": 1, "name": "Hamburguesa Clásica", "price": 12000},
  {"id": 2, "name": "Pizza Margarita", "price": 18000},
  {"id": 3, "name": "Ensalada César", "price": 9500}
]
```

#### F-03: Creación de Pedido

**Requisito**: US-03, RF-03, RF-04, RF-05

**Validación**:
```bash
# Crear pedido con múltiples items
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": 5,
    "items": [
      {"productId": 1, "quantity": 2, "note": "Sin cebolla"},
      {"productId": 3, "quantity": 1, "note": "Aderezo aparte"}
    ]
  }')

ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.id')
echo "Order ID: $ORDER_ID"
echo $ORDER_RESPONSE | jq '.'

# Verificar estado inicial
curl http://localhost:8080/orders/$ORDER_ID | jq '.status'
# Esperado: "PENDING"

# Esperar procesamiento asíncrono (2-3 segundos)
sleep 3

# Verificar estado actualizado por Kitchen Worker
curl http://localhost:8080/orders/$ORDER_ID | jq '.status'
# Esperado: "IN_PREPARATION"
```

**Resultado Esperado**:
```
✅ Pedido creado con ID único (UUID)
✅ Estado inicial: PENDING
✅ Evento publicado a RabbitMQ
✅ Estado actualizado a IN_PREPARATION por Kitchen Worker
✅ Items guardados en order_items
```

#### F-04: Consulta de Estado por Cliente

**Requisito**: US-06, RF-09

**Validación**:
```bash
# Obtener estado actual del pedido
curl http://localhost:8080/orders/$ORDER_ID | jq '{
  id: .id,
  status: .status,
  tableId: .tableId,
  createdAt: .createdAt,
  items: .items
}'
```

**Resultado Esperado**: ✅ Estado actualizado correctamente

### 3.2 Flujo Completo de Cocina

#### F-05: Login de Cocina

**Requisito**: NF-02 (Seguridad)

**Validación Manual**:
1. Acceder a http://localhost:5173/kitchen
2. Ingresar PIN: 1234
3. Verificar acceso

**Validación API** (con header de autorización):
```bash
# Con token válido
curl http://localhost:8080/orders \
  -H "X-Kitchen-Auth: kitchen-token-1234"
# Esperado: 200 OK

# Sin token
curl http://localhost:8080/orders
# Nota: Actualmente el backend no valida completamente, ver AUDITORIA.md H-ALTA-05
```

#### F-06: Visualización de Pedidos en Cocina

**Requisito**: US-04, RF-06

**Validación**:
```bash
# Listar pedidos activos
curl http://localhost:8080/orders | jq '.[] | {
  id: .id,
  tableId: .tableId,
  status: .status,
  createdAt: .createdAt
}'

# Filtrar por estado
curl "http://localhost:8080/orders?status=IN_PREPARATION" | jq '.'
```

**Resultado Esperado**: ✅ Lista de pedidos con filtros funcionales

#### F-07: Actualización de Estado de Pedido

**Requisito**: US-05, RF-07, RF-08

**Validación**:
```bash
# Actualizar a READY
curl -X PATCH http://localhost:8080/orders/$ORDER_ID/status \
  -H "Content-Type: application/json" \
  -d '{"status": "READY"}' | jq '.'

# Verificar actualización
curl http://localhost:8080/orders/$ORDER_ID | jq '.status'
# Esperado: "READY"
```

**Resultado Esperado**: ✅ Estado actualizado correctamente

### 3.3 Flujo End-to-End Completo

**Script de Prueba Completa**:
```bash
#!/bin/bash
set -e

echo "=== PRUEBA E2E COMPLETA ==="

# 1. Obtener menú
echo -e "\n1. Obteniendo menú..."
curl -s http://localhost:8080/menu | jq '.'

# 2. Crear pedido
echo -e "\n2. Creando pedido..."
ORDER_ID=$(curl -s -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": 7,
    "items": [
      {"productId": 1, "quantity": 2, "note": "Sin cebolla"},
      {"productId": 2, "quantity": 1}
    ]
  }' | jq -r '.id')
echo "Order ID: $ORDER_ID"

# 3. Verificar estado inicial
echo -e "\n3. Verificando estado inicial..."
sleep 1
STATUS=$(curl -s http://localhost:8080/orders/$ORDER_ID | jq -r '.status')
echo "Estado: $STATUS"
[[ "$STATUS" == "PENDING" ]] && echo "✅ Estado inicial correcto"

# 4. Esperar procesamiento por Kitchen Worker
echo -e "\n4. Esperando procesamiento asíncrono..."
sleep 3

# 5. Verificar estado actualizado
echo -e "\n5. Verificando estado actualizado..."
STATUS=$(curl -s http://localhost:8080/orders/$ORDER_ID | jq -r '.status')
echo "Estado: $STATUS"
[[ "$STATUS" == "IN_PREPARATION" ]] && echo "✅ Procesado por Kitchen Worker"

# 6. Cocina marca como listo
echo -e "\n6. Actualizando a READY..."
curl -s -X PATCH http://localhost:8080/orders/$ORDER_ID/status \
  -H "Content-Type: application/json" \
  -d '{"status": "READY"}' | jq '.'

# 7. Verificar estado final
echo -e "\n7. Verificando estado final..."
STATUS=$(curl -s http://localhost:8080/orders/$ORDER_ID | jq -r '.status')
echo "Estado: $STATUS"
[[ "$STATUS" == "READY" ]] && echo "✅ Pedido listo"

echo -e "\n=== PRUEBA E2E COMPLETADA ✅ ==="
```

**Guardar y ejecutar**:
```bash
chmod +x scripts/e2e-test.sh
./scripts/e2e-test.sh
```

---

## 4. Pruebas de Contrato

### 4.1 Contrato de API REST

#### 4.1.1 Schema de Request/Response

**Order Request Schema**:
```json
{
  "tableId": "number (1-20)",
  "items": [
    {
      "productId": "number",
      "quantity": "number (>0)",
      "note": "string (opcional)"
    }
  ]
}
```

**Order Response Schema**:
```json
{
  "id": "uuid",
  "tableId": "number",
  "status": "enum (PENDING|IN_PREPARATION|READY)",
  "createdAt": "ISO-8601 datetime",
  "items": [
    {
      "id": "uuid",
      "product": {
        "id": "number",
        "name": "string",
        "price": "number"
      },
      "quantity": "number",
      "note": "string"
    }
  ]
}
```

**Validación**:
```bash
# Crear pedido y validar schema
curl -s -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": 5,
    "items": [{"productId": 1, "quantity": 2}]
  }' | jq 'has("id") and has("status") and has("createdAt")'
# Esperado: true
```

### 4.2 Contrato de Eventos (RabbitMQ)

#### 4.2.1 Event Schema: order.placed

**Exchange**: `orders.exchange`  
**Routing Key**: `order.placed`  
**Queue**: `orders.placed`

**Event Payload**:
```json
{
  "orderId": "uuid",
  "tableId": "number",
  "items": [
    {
      "productId": "number",
      "productName": "string",
      "quantity": "number",
      "price": "number",
      "note": "string"
    }
  ],
  "timestamp": "ISO-8601 datetime"
}
```

**Validación**:
```bash
# Monitorear cola
docker exec restaurant-rabbitmq rabbitmqctl list_queues name messages_ready messages_unacknowledged

# Ver contenido (requiere habilitación de plugin de trace)
# Los logs de Kitchen Worker muestran el evento recibido:
docker-compose logs kitchen-worker | grep "Received order placed event"
```

---

## 5. Pruebas de Calidad No Funcional

### 5.1 Pruebas de Rendimiento

#### 5.1.1 Carga Concurrente

**Herramienta**: Apache Bench (ab) o wrk

**Prueba de Carga Ligera**:
```bash
# Instalar Apache Bench
sudo apt-get install apache2-utils

# 100 requests, 10 concurrentes
ab -n 100 -c 10 http://localhost:8080/menu
```

**Resultado Esperado**:
```
✅ Requests per second: >50
✅ Time per request: <200ms
✅ Failed requests: 0
```

**Prueba de Creación Concurrente**:
```bash
# Script para crear múltiples pedidos concurrentes
for i in {1..30}; do
  curl -s -X POST http://localhost:8080/orders \
    -H "Content-Type: application/json" \
    -d "{
      \"tableId\": $((1 + RANDOM % 20)),
      \"items\": [{\"productId\": 1, \"quantity\": 1}]
    }" > /dev/null &
done
wait

# Verificar que todos se crearon
curl -s http://localhost:8080/orders | jq 'length'
```

**Resultado Esperado**: ✅ 30/30 pedidos creados exitosamente (ver EVIDECIAS_PRUEBAS.md)

#### 5.1.2 Prueba de Estrés de Base de Datos

**Query N+1 Detection**:
```bash
# Crear pedido con múltiples items
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": 5,
    "items": [
      {"productId": 1, "quantity": 1},
      {"productId": 2, "quantity": 1},
      {"productId": 3, "quantity": 1}
    ]
  }'

# Monitorear logs SQL (habilitar logging.level.org.hibernate.SQL=DEBUG)
docker-compose logs order-service | grep "select"
```

**Nota**: Se identificó potencial problema N+1 en AUDITORIA.md H-ALTA-01

### 5.2 Pruebas de Seguridad

#### 5.2.1 Validación de Entrada

**SQL Injection**:
```bash
# Intentar inyección SQL (debe fallar)
curl "http://localhost:8080/orders?status=PENDING';DROP TABLE orders;--"
# Esperado: Error de validación o ignorado
```

**XSS en Notas**:
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "tableId": 5,
    "items": [{
      "productId": 1,
      "quantity": 1,
      "note": "<script>alert(\"XSS\")</script>"
    }]
  }'

# Verificar que el script no se ejecuta en frontend
```

#### 5.2.2 Autenticación de Cocina

**Prueba de Bypass**:
```bash
# Acceder sin token (actualmente permite, ver H-ALTA-05)
curl http://localhost:8080/orders

# Acceder con token inválido
curl http://localhost:8080/orders \
  -H "X-Kitchen-Auth: invalid-token"
```

**Nota**: La seguridad de cocina requiere refuerzo (ver AUDITORIA.md)

### 5.3 Pruebas de Resiliencia

#### 5.3.1 Caída de RabbitMQ

**Escenario**: RabbitMQ no disponible

```bash
# 1. Detener RabbitMQ
docker-compose stop rabbitmq

# 2. Intentar crear pedido
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"tableId": 5, "items": [{"productId": 1, "quantity": 1}]}'

# 3. Verificar comportamiento
# - Pedido debe guardarse en BD
# - Error al publicar evento (ver logs)
# - ¿Se retorna 500 o 200?

# 4. Reiniciar RabbitMQ
docker-compose start rabbitmq
```

**Resultado Actual**: ⚠️ Inconsistencia identificada en AUDITORIA.md H-ALTA-02

#### 5.3.2 Caída de PostgreSQL

```bash
# 1. Detener PostgreSQL
docker-compose stop postgres

# 2. Intentar crear pedido
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"tableId": 5, "items": [{"productId": 1, "quantity": 1}]}'
# Esperado: 500 Internal Server Error

# 3. Reiniciar PostgreSQL
docker-compose start postgres
```

#### 5.3.3 Reinicio de Kitchen Worker

```bash
# 1. Crear pedido
ORDER_ID=$(curl -s -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"tableId": 5, "items": [{"productId": 1, "quantity": 1}]}' \
  | jq -r '.id')

# 2. Detener Kitchen Worker inmediatamente
docker-compose stop kitchen-worker

# 3. Verificar cola de RabbitMQ
docker exec restaurant-rabbitmq rabbitmqctl list_queues
# Mensaje debe estar en la cola

# 4. Reiniciar Kitchen Worker
docker-compose start kitchen-worker

# 5. Verificar que el mensaje se procesa
sleep 3
curl http://localhost:8080/orders/$ORDER_ID | jq '.status'
# Esperado: "IN_PREPARATION"
```

**Resultado Esperado**: ✅ Mensaje persiste y se procesa tras reinicio

### 5.4 Pruebas de Observabilidad

#### 5.4.1 Logging

**Verificar logs estructurados**:
```bash
# Order Service
docker-compose logs order-service | grep "orderId"

# Kitchen Worker
docker-compose logs kitchen-worker | grep "orderId"

# RabbitMQ
docker-compose logs rabbitmq | grep "connection"
```

**Resultado Esperado**: ✅ Logs incluyen orderId para trazabilidad (ver EVIDECIAS_PRUEBAS.md NF-03)

#### 5.4.2 Health Checks

```bash
# Verificar health checks de contenedores
docker-compose ps

# PostgreSQL health
docker exec restaurant-postgres pg_isready

# RabbitMQ health
docker exec restaurant-rabbitmq rabbitmqctl status
```

---

## 6. Pruebas de Infraestructura

### 6.1 Docker Compose

#### 6.1.1 Levantamiento Completo

```bash
# Limpiar ambiente
docker-compose down -v

# Levantar todos los servicios
docker-compose up -d

# Verificar estado
docker-compose ps

# Verificar logs
docker-compose logs -f
```

**Resultado Esperado**:
```
✅ postgres: healthy
✅ rabbitmq: healthy
✅ order-service: running
✅ kitchen-worker: running
✅ frontend: running
```

#### 6.1.2 Orden de Inicio

**Dependencias** (ver docker-compose.yml):
1. postgres
2. rabbitmq
3. order-service (depende de postgres y rabbitmq)
4. kitchen-worker (depende de postgres, rabbitmq, order-service)
5. frontend (independiente)

**Validación**:
```bash
docker-compose config --services
docker-compose up --no-start
docker-compose start postgres rabbitmq
sleep 10
docker-compose start order-service
sleep 10
docker-compose start kitchen-worker frontend
```

### 6.2 Smoke Tests

#### 6.2.1 Smoke Test del Frontend

**Script**: `scripts/smoke.sh`

```bash
#!/usr/bin/env bash
set -euo pipefail

URL="${1:-http://localhost:5173}"

echo "Smoke test: $URL"
for i in {1..30}; do
  if curl -fsS "$URL" >/dev/null 2>&1; then
    echo "OK: frontend responde"
    exit 0
  fi
  sleep 1
done

echo "ERROR: frontend no respondió en 30s"
exit 1
```

**Ejecución**:
```bash
bash scripts/smoke.sh
```

**Resultado Esperado**: ✅ Frontend responde en <30s

#### 6.2.2 Smoke Test Completo

```bash
#!/bin/bash
set -e

echo "=== SMOKE TEST COMPLETO ==="

# 1. Frontend
echo -n "Frontend (5173): "
curl -fsS http://localhost:5173 >/dev/null && echo "✅" || echo "❌"

# 2. Order Service
echo -n "Order Service (8080): "
curl -fsS http://localhost:8080/menu >/dev/null && echo "✅" || echo "❌"

# 3. Swagger UI
echo -n "Swagger UI: "
curl -fsS http://localhost:8080/swagger-ui.html >/dev/null && echo "✅" || echo "❌"

# 4. RabbitMQ Management
echo -n "RabbitMQ Management (15672): "
curl -fsS -u guest:guest http://localhost:15672/api/overview >/dev/null && echo "✅" || echo "❌"

# 5. PostgreSQL
echo -n "PostgreSQL (5432): "
docker exec restaurant-postgres pg_isready -q && echo "✅" || echo "❌"

# 6. Crear pedido de prueba
echo -n "Crear pedido: "
ORDER_ID=$(curl -fsS -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"tableId": 1, "items": [{"productId": 1, "quantity": 1}]}' \
  | jq -r '.id')
[[ -n "$ORDER_ID" ]] && echo "✅ ($ORDER_ID)" || echo "❌"

# 7. Verificar procesamiento
echo -n "Procesamiento asíncrono: "
sleep 3
STATUS=$(curl -fsS http://localhost:8080/orders/$ORDER_ID | jq -r '.status')
[[ "$STATUS" == "IN_PREPARATION" ]] && echo "✅" || echo "⚠️  (estado: $STATUS)"

echo "=== SMOKE TEST COMPLETADO ==="
```

### 6.3 Pruebas de Volúmenes y Persistencia

```bash
# 1. Crear pedido
ORDER_ID=$(curl -s -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"tableId": 5, "items": [{"productId": 1, "quantity": 1}]}' \
  | jq -r '.id')

# 2. Detener contenedores
docker-compose down

# 3. Reiniciar (sin -v para mantener volúmenes)
docker-compose up -d

# 4. Verificar que el pedido persiste
curl http://localhost:8080/orders/$ORDER_ID | jq '.'
```

**Resultado Esperado**: ✅ Datos persisten tras reinicio

---

## Estrategia de Testing

### Pirámide de Testing

```
Cantidad de Pruebas (más → menos):
Velocidad (rápida → lenta):
Costo (bajo → alto):

    E2E (5%)        ← 1-2 flujos críticos
    ────────
  Integración (20%) ← APIs, BD, RabbitMQ
  ──────────────
 Unitarias (75%)    ← Lógica de negocio
────────────────────
```

### Cobertura por Componente

| Componente | Unitarias | Integración | E2E | Cobertura |
|------------|-----------|-------------|-----|-----------|
| Order Service | ✅ 9 clases | ✅ API + BD | ✅ | ~75% |
| Kitchen Worker | ✅ 4 clases | ✅ RabbitMQ | ✅ | ~70% |
| Frontend | ⚠️ Ninguna | ⚠️ Manual | ✅ | N/A |
| Base de Datos | ✅ Migrations | ✅ Repositories | ✅ | 100% |
| RabbitMQ | ✅ Publisher/Consumer | ✅ Queues | ✅ | 100% |

**Nota**: Frontend no tiene pruebas automatizadas (React Testing Library/Jest pendiente)

### Cuándo Ejecutar Cada Tipo

| Fase | Pruebas a Ejecutar | Tiempo |
|------|-------------------|--------|
| Desarrollo local | Unitarias del componente modificado | <1 min |
| Pre-commit | Unitarias completas | ~2 min |
| Pre-push | Unitarias + Linting | ~3 min |
| CI/CD | Todas (Unitarias + Integración + E2E) | ~10 min |
| Pre-release | Todas + Calidad No Funcional | ~30 min |

---

## Ejecución Completa

### Script Maestro de Pruebas

```bash
#!/bin/bash
set -e

echo "╔════════════════════════════════════════════╗"
echo "║  SUITE COMPLETA DE PRUEBAS DE CALIDAD     ║"
echo "╚════════════════════════════════════════════╝"

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Contadores
PASSED=0
FAILED=0

test_suite() {
  local name=$1
  local command=$2
  
  echo -e "\n${YELLOW}[TEST]${NC} $name"
  if eval $command; then
    echo -e "${GREEN}[✅ PASS]${NC} $name"
    ((PASSED++))
  else
    echo -e "${RED}[❌ FAIL]${NC} $name"
    ((FAILED++))
  fi
}

echo -e "\n📦 FASE 1: PRUEBAS UNITARIAS"
echo "════════════════════════════════════"

test_suite "Order Service - Pruebas Unitarias" \
  "cd order-service && mvn -q test"

test_suite "Kitchen Worker - Pruebas Unitarias" \
  "cd kitchen-worker && mvn -q test"

echo -e "\n🔧 FASE 2: INFRAESTRUCTURA"
echo "════════════════════════════════════"

test_suite "Docker Compose - Levantar servicios" \
  "docker-compose up -d && sleep 20"

test_suite "Smoke Test - Frontend" \
  "bash scripts/smoke.sh http://localhost:5173"

test_suite "Smoke Test - Backend" \
  "curl -fsS http://localhost:8080/menu >/dev/null"

echo -e "\n🔌 FASE 3: PRUEBAS DE INTEGRACIÓN"
echo "════════════════════════════════════"

test_suite "API - Obtener Menú" \
  "curl -fsS http://localhost:8080/menu | jq -e 'length > 0'"

test_suite "API - Crear Pedido" \
  "curl -fsS -X POST http://localhost:8080/orders \
    -H 'Content-Type: application/json' \
    -d '{\"tableId\": 5, \"items\": [{\"productId\": 1, \"quantity\": 1}]}' \
    | jq -e 'has(\"id\")'"

test_suite "Base de Datos - Verificar Tablas" \
  "docker exec restaurant-postgres psql -U restaurant_user -d restaurant_db \
    -c '\dt' | grep -q orders"

test_suite "RabbitMQ - Verificar Colas" \
  "docker exec restaurant-rabbitmq rabbitmqctl list_queues | grep -q orders"

echo -e "\n🔄 FASE 4: PRUEBAS FUNCIONALES"
echo "════════════════════════════════════"

# Crear pedido y guardar ID
ORDER_ID=$(curl -s -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"tableId": 7, "items": [{"productId": 1, "quantity": 1}]}' \
  | jq -r '.id')

test_suite "F-03: Pedido creado con UUID" \
  "[[ -n '$ORDER_ID' ]]"

test_suite "F-03: Estado inicial PENDING" \
  "curl -s http://localhost:8080/orders/$ORDER_ID | jq -e '.status == \"PENDING\"'"

sleep 3

test_suite "F-03: Kitchen Worker actualiza a IN_PREPARATION" \
  "curl -s http://localhost:8080/orders/$ORDER_ID | jq -e '.status == \"IN_PREPARATION\"'"

test_suite "F-07: Actualizar estado a READY" \
  "curl -fsS -X PATCH http://localhost:8080/orders/$ORDER_ID/status \
    -H 'Content-Type: application/json' \
    -d '{\"status\": \"READY\"}' | jq -e '.status == \"READY\"'"

echo -e "\n⚡ FASE 5: PRUEBAS NO FUNCIONALES"
echo "════════════════════════════════════"

test_suite "Rendimiento - Carga Ligera" \
  "ab -n 50 -c 5 -q http://localhost:8080/menu 2>&1 | grep -q 'Complete requests'"

test_suite "Resiliencia - Logs con orderId" \
  "docker-compose logs order-service | grep -q 'orderId'"

# Resumen final
echo -e "\n╔════════════════════════════════════════════╗"
echo -e "║          ${GREEN}RESUMEN DE PRUEBAS${NC}                 ║"
echo "╚════════════════════════════════════════════╝"
echo -e "Pruebas ejecutadas: $((PASSED + FAILED))"
echo -e "${GREEN}✅ Pasadas: $PASSED${NC}"
echo -e "${RED}❌ Fallidas: $FAILED${NC}"

if [ $FAILED -eq 0 ]; then
  echo -e "\n${GREEN}🎉 TODAS LAS PRUEBAS PASARON 🎉${NC}"
  exit 0
else
  echo -e "\n${RED}⚠️  ALGUNAS PRUEBAS FALLARON ⚠️${NC}"
  exit 1
fi
```

**Guardar y ejecutar**:
```bash
chmod +x scripts/test-all.sh
./scripts/test-all.sh
```

### Ejecución Rápida por Nivel

```bash
# Solo unitarias (~2 min)
mvn test

# Unitarias + Infraestructura (~5 min)
mvn test && docker-compose up -d && bash scripts/smoke.sh

# Completas (~10 min)
bash scripts/test-all.sh
```

---

## Métricas de Calidad

### Métricas Actuales

| Métrica | Valor | Objetivo | Estado |
|---------|-------|----------|--------|
| Cobertura de código (Backend) | ~75% | >80% | 🟡 Aceptable |
| Pruebas unitarias | 40+ | - | ✅ Bueno |
| Pruebas de integración | 10+ | - | ✅ Bueno |
| Pruebas E2E | 2 | >3 | 🟡 Aceptable |
| Tiempo de ejecución (Unitarias) | ~2 min | <5 min | ✅ Excelente |
| Tiempo de ejecución (Completas) | ~10 min | <15 min | ✅ Bueno |
| Bugs conocidos (Alta prioridad) | 6 | 0 | 🔴 Ver AUDITORIA.md |
| Deuda técnica | Media | Baja | 🟡 En progreso |

### Cobertura de Requisitos

| Requisito | Pruebas | Estado |
|-----------|---------|--------|
| RF-01: Selección de mesa | ✅ Unitaria + Funcional | ✅ |
| RF-02: Visualización de menú | ✅ Unitaria + Funcional | ✅ |
| RF-03: Creación de pedido | ✅ Unitaria + Funcional + E2E | ✅ |
| RF-04: Generación de ID único | ✅ Unitaria + Funcional | ✅ |
| RF-05: Registro en BD | ✅ Integración | ✅ |
| RF-06: Lista de pedidos | ✅ Unitaria + Funcional | ✅ |
| RF-07: Actualización de estado | ✅ Unitaria + Funcional | ✅ |
| RF-08: Publicación de evento | ✅ Integración | ✅ |
| RF-09: Consulta de estado | ✅ Funcional | ✅ |
| NF-01: Concurrencia | ✅ Rendimiento | ✅ |
| NF-02: Seguridad cocina | ⚠️ Parcial | 🟡 Ver H-ALTA-05 |
| NF-03: Observabilidad | ✅ Logging | ✅ |

### Issues Conocidos

Ver [AUDITORIA.md](AUDITORIA.md) para lista completa de hallazgos y plan de remediación:

- **H-ALTA-01**: OrderService con responsabilidades mezcladas
- **H-ALTA-02**: Gap de consistencia entre persistencia y eventos
- **H-ALTA-03**: Acoplamiento por base de datos compartida
- **H-ALTA-04**: Contrato tipo productId inconsistente
- **H-ALTA-05**: Seguridad de cocina no aplicada E2E
- **H-ALTA-06**: Ausencia de capas arquitectónicas claras

---

## Apéndices

### A. Comandos de Referencia Rápida

```bash
# Levantar sistema
docker-compose up -d

# Ver logs
docker-compose logs -f [servicio]

# Ejecutar pruebas
mvn test                              # Todas las pruebas Java
mvn test -Dtest=OrderServiceTest      # Prueba específica

# Crear pedido
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"tableId": 5, "items": [{"productId": 1, "quantity": 1}]}'

# Listar pedidos
curl http://localhost:8080/orders | jq '.'

# Verificar RabbitMQ
docker exec restaurant-rabbitmq rabbitmqctl list_queues

# Verificar BD
docker exec -it restaurant-postgres psql -U restaurant_user -d restaurant_db
```

### B. Herramientas Requeridas

- **Java 17+**: Para Order Service y Kitchen Worker
- **Maven 3.8+**: Para gestión de dependencias y pruebas
- **Node.js 18+**: Para frontend
- **Docker Desktop**: Para contenedores
- **curl**: Para pruebas de API
- **jq**: Para procesamiento de JSON
- **Apache Bench (ab)**: Para pruebas de carga (opcional)

### C. Referencias

- [README.md](README.md) - Documentación principal del proyecto
- [EVIDECIAS_PRUEBAS.md](EVIDECIAS_PRUEBAS.md) - Evidencias de pruebas ejecutadas
- [AUDITORIA.md](AUDITORIA.md) - Reporte de auditoría de código
- [SISTEMA_FUNCIONANDO.md](SISTEMA_FUNCIONANDO.md) - Guía de verificación del sistema
- [Swagger UI](http://localhost:8080/swagger-ui.html) - Documentación interactiva de API

---

**Última revisión**: 2026-02-13  
**Responsable**: Equipo de Desarrollo  
**Próxima revisión**: Al agregar nuevas funcionalidades

---

## Cambios y Mejoras Planificadas

### Corto Plazo
- [ ] Agregar pruebas unitarias para frontend (React Testing Library)
- [ ] Implementar pruebas de contrato con Pact
- [ ] Mejorar cobertura de código a >85%

### Mediano Plazo
- [ ] Agregar pruebas de performance automatizadas (JMeter/Gatling)
- [ ] Implementar pruebas de seguridad automatizadas (OWASP ZAP)
- [ ] CI/CD con ejecución automática de pruebas

### Largo Plazo
- [ ] Pruebas de UI automatizadas (Playwright/Cypress)
- [ ] Pruebas de accesibilidad (a11y)
- [ ] Monitoreo en producción con tests sintéticos
