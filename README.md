# Restaurant Order System

Full-stack restaurant ordering system with a React frontend, a Spring Boot API, a background worker, and RabbitMQ.

**Components**
- Frontend: React + Vite + TypeScript
- Order Service: Spring Boot REST API
- Kitchen Worker: Spring Boot consumer
- RabbitMQ: message broker
- PostgreSQL: relational database

**Runtime flow**
Frontend -> Order Service -> RabbitMQ -> Kitchen Worker  
Order Service -> PostgreSQL

## Quickstart (Docker Compose)

1. Clone the repo  
   `git clone https://github.com/Luis-Ospino/Sistemas-de-pedidos-restaurante.git`  
   `cd Sistemas-de-pedidos-restaurante`
2. Create env file  
   `Copy-Item .env.example .env`
3. Start the stack  
   `docker compose up -d --build`
4. Open the app  
   Frontend: `http://localhost:5173`  
   Kitchen: `http://localhost:5173/kitchen` (PIN from `VITE_KITCHEN_PIN`)  
   API: `http://localhost:8080`  
   Swagger: `http://localhost:8080/swagger-ui.html`  
   RabbitMQ UI: `http://localhost:15672` (guest/guest)
5. Stop  
   `docker compose down`

## Configuration

The default `.env.example` works out of the box for local compose.

| Variable | Purpose |
|---|---|
| `VITE_USE_MOCK` | `false` to use real API |
| `VITE_API_BASE_URL` | API base URL for frontend |
| `VITE_KITCHEN_PIN` | Kitchen access PIN |
| `VITE_ALLOWED_HOSTS` | Optional, allowed hosts for Vite (e.g. `.trycloudflare.com`) |
| `CORS_ALLOWED_ORIGIN_PATTERNS` | Allowed origins for API CORS |
| `DB_URL` | JDBC URL for Order Service |
| `DB_USER` / `DB_PASS` | DB credentials |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | RabbitMQ connection |

## Demo (optional, public)

Use only for short-lived demos. Do not commit `.env`.

1. Start the stack  
   `docker compose up -d --build`
2. Open tunnels in two terminals  
   Backend: `cloudflared tunnel --url http://localhost:8080`  
   Frontend: `cloudflared tunnel --url http://localhost:5173`
3. Update `.env`  
   `VITE_API_BASE_URL=https://<backend-tunnel>`  
   `VITE_ALLOWED_HOSTS=.trycloudflare.com`  
   `CORS_ALLOWED_ORIGIN_PATTERNS=https://*.trycloudflare.com`
4. Rebuild frontend  
   `docker compose up -d --build frontend`

Stop demo: `docker compose down` and close the tunnel terminals.

## Local development (without compose)

Prereqs: Node 18+, JDK 17, Maven, Docker (for infra).

1. Infra only  
   `docker compose up -d postgres rabbitmq`
2. Order Service  
   `cd order-service`  
   `mvn spring-boot:run`
3. Kitchen Worker  
   `cd kitchen-worker`  
   `mvn spring-boot:run`
4. Frontend  
   `npm install`  
   `npm run dev`

## Smoke checks

`curl http://localhost:8080/menu`  
`curl http://localhost:8080/orders?status=PENDING`  
Create a pedido in the UI and confirm it appears in Kitchen.

Kitchen refresh behavior: it refreshes on new orders created from the frontend.  
If orders are created outside the frontend, use the **Actualizar** button.

## Testing

Order Service: `mvn test`  
Kitchen Worker: `mvn test`

## Ports

Frontend: `5173`  
Order Service: `8080`  
PostgreSQL: `5432`  
RabbitMQ AMQP: `5672`  
RabbitMQ UI: `15672`

## Repository structure (high level)

`src/` frontend  
`order-service/` backend API  
`kitchen-worker/` async worker  
`docker-compose.yml` full stack

## Notes for production

- `VITE_USE_MOCK=false`
- Set `VITE_API_BASE_URL` to your real domain
- Set `CORS_ALLOWED_ORIGIN_PATTERNS` to your real domain
