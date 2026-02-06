# Sistema de Pedidos de Restaurante

Stack full-stack con frontend, backend y RabbitMQ. La ejecucion recomendada para QA/local es via Docker Compose.

## Servicios
- Frontend (Vite)
- Order Service (Spring Boot)
- Kitchen Worker (Spring Boot)
- PostgreSQL (una sola instancia)
- RabbitMQ (broker + UI)

## Puertos estandarizados
| Servicio | Puerto |
|---|---|
| Frontend | 5173 |
| Backend API | 8080 |
| PostgreSQL | 5432 |
| RabbitMQ AMQP | 5672 |
| RabbitMQ Management UI | 15672 |

## Inicio rapido (Docker Compose)
Copia `.env.example` a `.env` y ajusta si necesitas:
```bash
cp .env.example .env
```
En PowerShell:
```powershell
Copy-Item .env.example .env
```

Levantar:
```bash
docker compose up -d --build
```

Ver estado:
```bash
docker compose ps
```

Apagar:
```bash
docker compose down
```

## URLs
- Frontend: http://localhost:5173
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- RabbitMQ UI: http://localhost:15672 (user: guest / pass: guest)

## Verificacion basica
1) Menu:
```bash
curl http://localhost:8080/menu
```

2) Crear pedido:
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"tableId": 5, "items": [{"productId": 1, "quantity": 2}]}'
```

3) Consultar pedido:
```bash
curl http://localhost:8080/orders/{id}
```

## Verificar RabbitMQ
Opcion A (UI):
- Entra a http://localhost:15672
- Revisa la cola `order.placed.queue` (mensajes publicados/consumidos)

Opcion B (CLI):
```bash
docker exec restaurant-rabbitmq rabbitmqctl list_queues name messages_ready messages_unacknowledged
```

## Variables de entorno
Las variables estan en `.env.example` y son cargadas por Docker Compose.

## Notas
- El frontend se ejecuta dentro de Docker Compose con `VITE_USE_MOCK=false`.
- El backend y el worker usan la misma base de datos para QA local.
