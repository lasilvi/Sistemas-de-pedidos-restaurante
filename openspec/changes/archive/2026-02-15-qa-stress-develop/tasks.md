## 1. Preparacion de QA

- [x] 1.1 Actualizar `develop` y crear branch `qa/qa-stress-develop`
- [x] 1.2 Levantar stack con Docker Compose (reset limpio)

## 2. Pruebas de API y Stress

- [x] 2.1 Ejecutar smoke tests API (GET /menu, POST /orders, GET /orders/{id})
- [x] 2.2 Ejecutar stress test agresivo con k6 y capturar resultados

## 3. QA UI y RabbitMQ

- [x] 3.1 Validar flujos UI (cliente + cocina) y registrar orderId
- [x] 3.2 Verificar RabbitMQ (colas + logs del worker)

## 4. Reporte y PR

- [x] 4.1 Crear reporte QA en `docs/qa/QA-YYYY-MM-DD-develop.md`
- [x] 4.2 Commit/push del reporte y PR a `develop`
