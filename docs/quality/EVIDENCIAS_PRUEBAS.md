# EVIDECIAS_PRUEBAS.md

Fecha: 2026-02-08
Entorno: Docker Compose local
Base URLs: http://localhost:8080 (API), http://localhost:5173 (Frontend)

Resumen rapido
- Orden de prueba principal: 367c91b5-13a5-436b-b554-97e98a660ab0
- Resultado general: varios escenarios OK, algunos no aplican o fallan por diferencias con el MVP (estados soportados)

## 1. Escenarios de Prueba Funcionales (Happy Path)

F-01: Seleccion valida de mesa (US-01, RF-01)
- Metodo: via API (creacion de pedido con mesa 5)
- Evidencia:
  - POST /orders con tableId=5 retorna 200 y crea orden
- Resultado: OK (validacion indirecta via API, UI no probada)

F-02: Visualizacion del menu (US-02, RF-02)
- Evidencia:
  - GET /menu retorna 3 productos
- Resultado: OK

F-03: Creacion de pedido exitosa (US-03, RF-03, RF-04, RF-05)
- Evidencia:
  - POST /orders crea orderId unico
  - Estado inicial observado: PENDING (no SUBMITTED)
  - Log order-service: "Successfully published order.placed event"
  - DB: registro en orders y order_items
- Resultado: OK con diferencia respecto al requisito (estado inicial PENDING)

F-04: Visualizacion de pedidos en cocina (US-04, RF-06)
- Evidencia:
  - GET /orders lista la orden en estado activo
- Resultado: OK (validacion via API; UI no probada)

F-05: Cambio valido de estado del pedido (US-05, RF-07, RF-08)
- Evidencia:
  - PATCH /orders/{id}/status -> READY retorna 200
- Resultado: OK (evento order.status.changed no evidenciado en logs)

F-06: Consulta de estado por cliente (US-06, RF-09)
- Evidencia:
  - GET /orders/{id} retorna el pedido con estado actual
- Resultado: OK

## 2. Escenarios de Reglas de Negocio

RB-01: Mesa fuera de rango
- Evidencia:
  - POST /orders con tableId=0 retorna HTTP 400
- Resultado: OK

RB-02: Producto inexistente
- Evidencia:
  - POST /orders con productId=999 retorna HTTP 404
- Resultado: OK

RB-03: Transicion de estado invalida (READY -> IN_PREPARATION)
- Evidencia:
  - PATCH /orders/{id}/status a IN_PREPARATION retorna 200
- Resultado: FAIL (la transicion invalida fue permitida)

RB-04: Pedido finalizado no modificable
- Evidencia:
  - PATCH /orders/{id}/status con DELIVERED retorna HTTP 500
  - Log order-service indica enum de estados soportados: [READY, IN_PREPARATION, PENDING]
- Resultado: NO APLICA / FAIL (DELIVERED no soportado en la API actual)

## 3. Escenarios de Eventos (RabbitMQ)

E-01: Publicacion de evento order.placed
- Evidencia:
  - Log order-service: "Successfully published order.placed event" para orderId=367c...
- Resultado: OK

E-02: Consumo de evento por Kitchen Worker
- Evidencia:
  - Log kitchen-worker: "Received order placed event" y "Order processed successfully" para orderId=367c...
- Resultado: OK

E-03: Evento order.status.changed
- Evidencia:
  - No se encontro log de publicacion para cambios de estado
- Resultado: NO VERIFICADO

## 4. Escenarios de Persistencia

P-01: Persistencia del pedido
- Evidencia:
  - DB orders: registro para orderId=367c...
  - DB order_items: 2 items asociados
- Resultado: OK

P-02: Historial de estados
- Evidencia:
  - No existe tabla de historial (solo orders, order_items, products)
- Resultado: NO APLICA / NO IMPLEMENTADO

## 5. Escenarios No Funcionales

NF-01: Concurrencia minima
- Evidencia:
  - 30 pedidos concurrentes creados exitosamente (30/30)
- Resultado: OK

NF-02: Seguridad minima cocina
- Evidencia:
  - Requiere prueba en UI (PIN)
- Resultado: NO VERIFICADO

NF-03: Observabilidad
- Evidencia:
  - Logs incluyen orderId en order-service y kitchen-worker
- Resultado: OK

## 6. Smoke Tests

S-01: Levante completo del sistema
- Evidencia:
  - docker compose ps muestra servicios Up/healthy
- Resultado: OK

S-02: Flujo End-to-End
- Evidencia:
  - Flujo parcial: crear pedido -> worker actualiza a IN_PREPARATION -> cambio manual a READY
  - Estado DELIVERED no soportado por la API
- Resultado: PARCIAL

## Comandos usados (resumen)

```powershell
Invoke-RestMethod http://localhost:8080/menu
Invoke-RestMethod http://localhost:8080/orders -Method Post -Body <json>
Invoke-RestMethod http://localhost:8080/orders/{id}
Invoke-RestMethod http://localhost:8080/orders/{id}/status -Method Patch -Body <json>

docker exec restaurant-postgres psql -U restaurant_user -d restaurant_db -c "SELECT ..."
docker exec restaurant-rabbitmq rabbitmqctl list_queues name messages_ready messages_unacknowledged

docker compose ps
```

## Observaciones relevantes
- El estado inicial del pedido es PENDING (no SUBMITTED).
- Los estados soportados por la API son PENDING, IN_PREPARATION y READY.
- DELIVERED/CANCELED no existen en la API actual, por lo que RB-04 y parte de S-02 no aplican.
- No hay persistencia de historial de estados.
- Se observan problemas de encoding en textos del menu (acentos).
