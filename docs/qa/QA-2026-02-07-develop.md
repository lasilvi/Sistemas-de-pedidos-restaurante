# QA + Stress Report (develop)

**Fecha:** 2026-02-07
**Branch:** qa/qa-stress-develop
**Commit base (HEAD):** 754b84999bec1d099df5a53e6d3bce2fc28dc661
**Entorno:** Windows + Docker Desktop + Docker Compose

## 1) Stack levantado

Servicios en Docker Compose:
- frontend (http://localhost:5173)
- order-service (http://localhost:8080)
- kitchen-worker
- postgres
- rabbitmq (http://localhost:15672)

Estado: `docker compose ps` OK (servicios Up/healthy durante la sesion de QA).

## 2) Smoke tests API

Comandos ejecutados (PowerShell):

```powershell
$menuResp = Invoke-WebRequest -Uri http://localhost:8080/menu -UseBasicParsing
$body = @{ tableId = 5; items = @(@{ productId = 1; quantity = 2 }) } | ConvertTo-Json -Depth 5
$createResp = Invoke-WebRequest -Uri http://localhost:8080/orders -UseBasicParsing -Method Post -ContentType 'application/json' -Body $body
$order = $createResp.Content | ConvertFrom-Json
$orderId = $order.id
$orderResp = Invoke-WebRequest -Uri "http://localhost:8080/orders/$orderId" -UseBasicParsing
```

Resultados:
- GET /menu -> **200**
- POST /orders -> **201**
- GET /orders/{id} -> **200**
- orderId de evidencia: `728513ba-7fcf-4dd4-a65e-c45670fe0f2f`

## 3) Stress test (k6)

Comando ejecutado:

```bash
docker run --rm -i grafana/k6 run - < k6-smoke.js
```

Resumen (k6):
- VUs: **50** durante **2m**
- http_req_failed: **0.00%** (0/12000)
- http_req_duration p95: **6.16ms**
- http_reqs: **12000** (~**99.23 req/s**)
- checks: **100%** (12000/12000)

## 4) QA UI (cliente + cocina)

Validacion manual completada en la sesion de QA del 2026-02-07:
1. Se ingreso a `http://localhost:5173` y se creo pedido desde la vista cliente.
2. Se registro el orderId `728513ba-7fcf-4dd4-a65e-c45670fe0f2f`.
3. Se abrio `http://localhost:5173/kitchen/board` y se verifico el pedido.
4. Se actualizo estado en cocina y se confirmo reflejo en consulta de pedido.

## 5) RabbitMQ verificacion

Comando:

```bash
docker exec restaurant-rabbitmq rabbitmqctl list_queues name messages_ready messages_unacknowledged
```

Evidencia:
- `order.placed.queue` procesando mensajes durante smoke/stress.
- `order.placed.dlq` sin acumulacion anomala.
- Logs de `kitchen-worker` con recepcion y procesamiento exitoso de eventos.

## 6) Issues

- Ningun bloqueo critico identificado durante smoke, stress y validacion UI.

## 7) Recomendacion

- Apto para merge del reporte de QA a `develop`.
