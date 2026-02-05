# Frontend — Sistema de Pedidos (MVP)

Este frontend implementa:
- **UI Cliente**: mesa → menú → carrito → confirmación → consulta de estado.
- **UI Cocina**: listado de pedidos + acciones de cambio de estado (con PIN/token simple).

Basado en el documento de requerimientos del MVP (2 días).

## Requisitos
- Node.js 18+ (recomendado 20+)
- Docker + Docker Compose (para smoke test)

## Variables de entorno
Crea un archivo `.env` (puedes copiar desde `.env.example`).

## Ejecutar en desarrollo (hot reload)
```bash
npm i
npm run dev
```
Abre: http://localhost:5173

## Build + preview (modo “producción local”)
```bash
npm run build
npm run preview
```
Abre: http://localhost:8080

## Smoke test con Docker Compose
1) Construir y levantar el frontend:
```bash
docker compose -f docker-compose.frontend.yml up -d --build
```

2) Probar que sirve HTML:
```bash
npm run smoke
```

3) Apagar:
```bash
docker compose -f docker-compose.frontend.yml down
```

## Integración con API
El frontend consume:
- `GET /menu`
- `POST /orders`
- `GET /orders/{orderId}`
- `GET /orders?status=...` (cocina)
- `PATCH /orders/{orderId}/status` (cocina)

Se configura con `VITE_API_BASE_URL` (ver `.env.example`).
