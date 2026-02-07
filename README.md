# 🍽️ Sistema de Pedidos de Restaurante

Sistema completo de gestión de pedidos para restaurantes con arquitectura de microservicios, frontend React y comunicación asíncrona mediante eventos.

## 📋 Descripción

Sistema fullstack para gestionar pedidos de restaurante que incluye:
- **Frontend React**: Interfaz moderna para clientes y cocina (React + TypeScript + Vite + TailwindCSS)
- **Order Service**: API REST para crear y gestionar pedidos (Spring Boot)
- **Kitchen Worker**: Servicio de procesamiento asíncrono de pedidos (Spring Boot)
- **RabbitMQ**: Mensajería asíncrona entre microservicios
- **PostgreSQL**: Base de datos relacional
- **Docker Compose**: Orquestación completa de todos los servicios

## 🏗️ Arquitectura

```
┌─────────────────┐         ┌──────────────┐         ┌─────────────────┐
│    Frontend     │────────▶│ Order Service│────────▶│   RabbitMQ      │
│  React + Vite   │         │  (Port 8080) │         │  (Port 5672)    │
│  (Port 5173)    │         └──────┬───────┘         └────────┬────────┘
└─────────────────┘                │                          │
                                   │                          │
                                   ▼                          ▼
                          ┌─────────────────┐       ┌─────────────────┐
                          │   PostgreSQL    │       │ Kitchen Worker  │
                          │  (Port 5432)    │       │  (Async Worker) │
                          └─────────────────┘       └─────────────────┘
```

### Flujo de Datos

1. **Cliente** → Selecciona mesa, ve menú, agrega productos al carrito
2. **Frontend** → Envía pedido al Order Service vía API REST
3. **Order Service** → Guarda pedido en PostgreSQL con estado PENDING
4. **Order Service** → Publica evento `order.placed` en RabbitMQ
5. **Kitchen Worker** → Consume evento y actualiza pedido a IN_PREPARATION
6. **Cocina** → Ve pedidos en tiempo real y actualiza estados

## 🚀 Inicio Rápido

### Prerrequisitos

- **Docker Desktop** instalado y corriendo
- **Git** para clonar el repositorio

### Opción 1: Docker Compose (RECOMENDADO)

Esta es la forma más fácil y rápida de ejecutar todo el sistema.

#### 1. Clonar el repositorio

```bash
git clone https://github.com/Luis-Ospino/Sistemas-de-pedidos-restaurante.git
cd Sistemas-de-pedidos-restaurante
```

#### 2. Configurar variables de entorno

```bash
# Linux/Mac
cp .env.example .env

# Windows PowerShell
Copy-Item .env.example .env
```

El archivo `.env` ya viene con valores por defecto que funcionan. No necesitas modificarlo.

#### 3. Iniciar todos los servicios

```bash
docker-compose up --build
```

O en modo detached (segundo plano):

```bash
docker-compose up -d --build
```

#### 4. Esperar a que todo esté listo

Verás mensajes como:
- ✅ `restaurant-postgres | database system is ready to accept connections`
- ✅ `restaurant-rabbitmq | Server startup complete`
- ✅ `restaurant-order-service | Started OrderServiceApplication`
- ✅ `restaurant-kitchen-worker | Started KitchenWorkerApplication`
- ✅ `restaurant-frontend | VITE ready in XXX ms`

#### 5. Acceder a las aplicaciones

- **Frontend Cliente**: http://localhost:5173
- **Frontend Cocina**: http://localhost:5173/kitchen (PIN: 1234)
- **API Backend**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

### Opción 2: Ejecución Manual (Desarrollo)

Si prefieres ejecutar los servicios individualmente para desarrollo:

#### 1. Iniciar servicios de infraestructura

```powershell
# PostgreSQL
docker run -d --name restaurant-postgres `
  -e POSTGRES_USER=restaurant_user `
  -e POSTGRES_PASSWORD=restaurant_pass `
  -e POSTGRES_DB=restaurant_db `
  -p 5432:5432 postgres:15

# RabbitMQ
docker run -d --name restaurant-rabbitmq `
  -p 5672:5672 `
  -p 15672:15672 `
  rabbitmq:3-management

# Esperar 10 segundos
timeout /t 10 /nobreak
```

#### 2. Iniciar Order Service

```powershell
cd order-service
mvn spring-boot:run
```

#### 3. Iniciar Kitchen Worker (nueva terminal)

```powershell
cd kitchen-worker
mvn spring-boot:run
```

#### 4. Iniciar Frontend (nueva terminal)

```powershell
# Instalar dependencias (solo la primera vez)
npm install

# Iniciar servidor de desarrollo
npm run dev
```

## 📱 Uso de la Aplicación

### Interfaz de Cliente

1. **Seleccionar Mesa**: Ingresa el número de mesa (1-20)
2. **Ver Menú**: Explora los productos disponibles
3. **Agregar al Carrito**: Selecciona productos y cantidades
4. **Realizar Pedido**: Confirma y envía el pedido
5. **Seguimiento**: Ve el estado de tu pedido en tiempo real

### Interfaz de Cocina

1. **Login**: Ingresa el PIN (por defecto: 1234)
2. **Ver Pedidos**: Lista de pedidos pendientes y en preparación
3. **Actualizar Estado**: Marca pedidos como listos
4. **Filtros**: Filtra por estado (Pendiente, En Preparación, Listo)

## 🔌 API Endpoints

### Menú

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/menu` | Obtener lista de productos disponibles |

### Pedidos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/orders` | Crear nuevo pedido |
| GET | `/orders` | Listar todos los pedidos |
| GET | `/orders?status=PENDING` | Filtrar pedidos por estado |
| GET | `/orders/{id}` | Obtener pedido por ID |
| PATCH | `/orders/{id}/status` | Actualizar estado del pedido |

### Ejemplos de Uso

#### Obtener Menú

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/menu" -Method Get
```

#### Crear Pedido

```powershell
$body = @{
    tableId = 5
    items = @(
        @{
            productId = 1
            quantity = 2
            note = "Sin cebolla"
        },
        @{
            productId = 3
            quantity = 1
            note = "Extra aderezo"
        }
    )
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/orders" `
  -Method Post `
  -Body $body `
  -ContentType "application/json"
```

#### Consultar Pedido

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/orders/{orderId}" -Method Get
```

#### Actualizar Estado

```powershell
$statusUpdate = @{ status = "READY" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/orders/{orderId}/status" `
  -Method Patch `
  -Body $statusUpdate `
  -ContentType "application/json"
```

## 🛠️ Comandos Útiles

### Docker Compose

```bash
# Ver estado de los contenedores
docker-compose ps

# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f frontend
docker-compose logs -f order-service
docker-compose logs -f kitchen-worker

# Reiniciar un servicio
docker-compose restart frontend

# Reiniciar todos los servicios
docker-compose restart

# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes (limpieza completa)
docker-compose down -v

# Reconstruir e iniciar
docker-compose up --build
```

### Verificación de RabbitMQ

```bash
# Ver colas y mensajes
docker exec restaurant-rabbitmq rabbitmqctl list_queues name messages_ready messages_unacknowledged

# Ver conexiones activas
docker exec restaurant-rabbitmq rabbitmqctl list_connections

# Ver exchanges
docker exec restaurant-rabbitmq rabbitmqctl list_exchanges
```

### Verificación de Base de Datos

```bash
# Conectar a PostgreSQL
docker exec -it restaurant-postgres psql -U restaurant_user -d restaurant_db

# Ver pedidos
docker exec -it restaurant-postgres psql -U restaurant_user -d restaurant_db -c "SELECT id, table_id, status, created_at FROM orders ORDER BY created_at DESC LIMIT 10;"

# Ver productos
docker exec -it restaurant-postgres psql -U restaurant_user -d restaurant_db -c "SELECT * FROM products;"
```

## 🧪 Testing

### Tests Unitarios

```bash
# Order Service
cd order-service
mvn test

# Kitchen Worker
cd kitchen-worker
mvn test

# Frontend
npm test
```

### Tests Incluidos

- ✅ 29 tests unitarios en Order Service
- ✅ 9 tests unitarios en Kitchen Worker
- ✅ Tests de controladores
- ✅ Tests de servicios
- ✅ Tests de manejo de excepciones
- ✅ Property-based testing con jqwik

## 🔧 Configuración

### Puertos

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| Frontend | 5173 | Aplicación React |
| Order Service | 8080 | API REST |
| Kitchen Worker | - | Servicio interno (sin puerto expuesto) |
| PostgreSQL | 5432 | Base de datos |
| RabbitMQ AMQP | 5672 | Protocolo de mensajería |
| RabbitMQ Management | 15672 | UI de administración |

### Variables de Entorno

El archivo `.env` contiene todas las configuraciones necesarias:

```env
# Frontend
VITE_USE_MOCK=false                          # Usar datos mock o API real
VITE_API_BASE_URL=http://localhost:8080      # URL del backend
VITE_KITCHEN_PIN=1234                        # PIN de acceso a cocina

# PostgreSQL
POSTGRES_DB=restaurant_db
POSTGRES_USER=restaurant_user
POSTGRES_PASSWORD=restaurant_pass

# Backend
DB_URL=jdbc:postgresql://postgres:5432/restaurant_db
DB_USER=restaurant_user
DB_PASS=restaurant_pass

# RabbitMQ
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASS=guest
```

## 🛠️ Tecnologías

### Frontend
- **React 18**: Biblioteca de UI
- **TypeScript**: Tipado estático
- **Vite**: Build tool y dev server
- **TailwindCSS**: Framework de CSS
- **React Router**: Navegación
- **TanStack Query**: Gestión de estado del servidor

### Backend
- **Spring Boot 3.2.0**: Framework principal
- **Spring Data JPA**: Persistencia de datos
- **Spring AMQP**: Integración con RabbitMQ
- **Flyway**: Migraciones de base de datos
- **Lombok**: Reducción de código boilerplate
- **SpringDoc OpenAPI**: Documentación Swagger

### Base de Datos
- **PostgreSQL 15**: Base de datos relacional
- **Hibernate**: ORM

### Mensajería
- **RabbitMQ 3**: Broker de mensajes
- **Jackson**: Serialización JSON

### Testing
- **JUnit 5**: Framework de testing
- **Mockito**: Mocking
- **jqwik**: Property-based testing

### DevOps
- **Docker**: Contenedorización
- **Docker Compose**: Orquestación
- **Maven**: Gestión de dependencias Java
- **npm**: Gestión de dependencias Node.js

## 📁 Estructura del Proyecto

```
restaurant-order-system/
├── src/                              # Frontend React
│   ├── api/                          # Llamadas HTTP y contratos
│   ├── components/                   # Componentes reutilizables
│   ├── pages/                        # Páginas de la aplicación
│   │   ├── client/                   # Páginas del cliente
│   │   └── kitchen/                  # Páginas de cocina
│   ├── store/                        # Estado global (carrito, auth)
│   ├── domain/                       # Lógica de dominio
│   ├── App.tsx                       # Componente principal
│   └── main.tsx                      # Punto de entrada
│
├── order-service/                    # Backend Order Service
│   ├── src/main/java/
│   │   └── com/restaurant/orderservice/
│   │       ├── config/               # Configuración
│   │       ├── controller/           # REST Controllers
│   │       ├── dto/                  # Data Transfer Objects
│   │       ├── entity/               # Entidades JPA
│   │       ├── enums/                # Enumeraciones
│   │       ├── event/                # Eventos de dominio
│   │       ├── exception/            # Excepciones personalizadas
│   │       ├── repository/           # Repositorios JPA
│   │       └── service/              # Lógica de negocio
│   ├── src/main/resources/
│   │   ├── db/migration/             # Migraciones Flyway
│   │   └── application.yml           # Configuración
│   └── src/test/                     # Tests unitarios
│
├── kitchen-worker/                   # Backend Kitchen Worker
│   ├── src/main/java/
│   │   └── com/restaurant/kitchenworker/
│   │       ├── config/               # Configuración RabbitMQ
│   │       ├── entity/               # Entidades JPA
│   │       ├── listener/             # Listeners de RabbitMQ
│   │       ├── repository/           # Repositorios JPA
│   │       └── service/              # Lógica de negocio
│   └── src/test/                     # Tests unitarios
│
├── docker-compose.yml                # Orquestación completa
├── Dockerfile.frontend               # Dockerfile del frontend
├── order-service/Dockerfile          # Dockerfile del order-service
├── kitchen-worker/Dockerfile         # Dockerfile del kitchen-worker
├── .env.example                      # Variables de entorno de ejemplo
├── package.json                      # Dependencias frontend
├── pom.xml                           # POM padre Maven
└── README.md                         # Este archivo
```

## 🐛 Solución de Problemas

### Docker Desktop no está corriendo

```powershell
# Verifica que Docker Desktop esté iniciado
docker ps
```

Si ves un error, inicia Docker Desktop desde el menú de inicio.

### Puerto ya en uso

```bash
# Detén los contenedores existentes
docker-compose down

# Si persiste, encuentra y detén el proceso que usa el puerto
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

### Error de conexión al iniciar servicios

```bash
# Espera 10-15 segundos después de iniciar los contenedores
# Los servicios necesitan tiempo para inicializarse

# Verifica los logs
docker-compose logs -f
```

### Frontend no se conecta al backend

1. Verifica que el backend esté corriendo: http://localhost:8080/menu
2. Revisa la variable `VITE_API_BASE_URL` en `.env`
3. Verifica que CORS esté configurado correctamente en el backend

### RabbitMQ no procesa mensajes

```bash
# Verifica que RabbitMQ esté corriendo
docker-compose logs rabbitmq

# Verifica las colas
docker exec restaurant-rabbitmq rabbitmqctl list_queues

# Verifica que kitchen-worker esté conectado
docker-compose logs kitchen-worker
```

### Limpiar todo y empezar de cero

```bash
# Detener y eliminar todo
docker-compose down -v

# Eliminar imágenes
docker-compose down --rmi all

# Reconstruir desde cero
docker-compose up --build
```

## 📖 Documentación Adicional

- [SISTEMA_FUNCIONANDO.md](SISTEMA_FUNCIONANDO.md) - Guía completa de verificación y pruebas
- [AI_WORKFLOW.md](AI_WORKFLOW.md) - Flujo de trabajo con IA
- [.kiro/specs/restaurant-order-system/](./kiro/specs/restaurant-order-system/) - Especificaciones técnicas detalladas

## 🤝 Contribuir

Este proyecto fue desarrollado siguiendo metodología Spec-Driven Development con:
- Especificaciones formales de requisitos
- Diseño detallado con propiedades de correctitud
- Tests unitarios y property-based testing
- Documentación completa

## 📄 Licencia

Este proyecto es un ejemplo educativo de arquitectura de microservicios.

## ✨ Características Destacadas

- ✅ Arquitectura de microservicios con comunicación asíncrona
- ✅ Frontend moderno con React + TypeScript + TailwindCSS
- ✅ Interfaz dual: Cliente y Cocina
- ✅ Comunicación en tiempo real mediante eventos
- ✅ Dead Letter Queue para manejo de errores
- ✅ Documentación interactiva con Swagger
- ✅ Migraciones de base de datos con Flyway
- ✅ Tests unitarios completos
- ✅ Property-based testing
- ✅ Manejo robusto de excepciones
- ✅ Validación de datos
- ✅ Logging estructurado
- ✅ Docker Compose para fácil despliegue
- ✅ Variables de entorno configurables

---

**Desarrollado con ❤️ usando Spring Boot, React y arquitectura de microservicios**
