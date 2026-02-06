# 🍽️ Sistema de Pedidos de Restaurante

Sistema de gestión de pedidos para restaurantes implementado con arquitectura de microservicios, comunicación asíncrona mediante eventos y bases de datos separadas por servicio.

## 📋 Descripción

Sistema completo para gestionar pedidos de restaurante que incluye:
- **Order Service**: API REST para crear y gestionar pedidos
- **Kitchen Worker**: Servicio de procesamiento asíncrono de pedidos
- **Comunicación mediante eventos**: RabbitMQ para mensajería asíncrona
- **Bases de datos separadas**: PostgreSQL independiente para cada servicio

## 🏗️ Arquitectura

### Microservicios

```
┌─────────────────┐         ┌──────────────┐         ┌─────────────────┐
│  Order Service  │────────▶│   RabbitMQ   │────────▶│ Kitchen Worker  │
│   (Port 8080)   │         │ (Port 5672)  │         │   (Port 8081)   │
└────────┬────────┘         └──────────────┘         └────────┬────────┘
         │                                                     │
         │                                                     │
         ▼                                                     ▼
┌─────────────────┐                                  ┌─────────────────┐
│   PostgreSQL    │                                  │   PostgreSQL    │
│ restaurant_db   │                                  │kitchen_worker_db│
│   (Port 5433)   │                                  │   (Port 5434)   │
└─────────────────┘                                  └─────────────────┘
```

### Componentes

1. **Order Service**
   - API REST para gestión de pedidos
   - Base de datos: `restaurant_db` (Puerto 5433)
   - Publica eventos a RabbitMQ
   - Documentación: Swagger UI

2. **Kitchen Worker**
   - Consumidor de eventos de RabbitMQ
   - Base de datos: `kitchen_worker_db` (Puerto 5434)
   - Procesa pedidos asíncronamente
   - Actualiza estado a IN_PREPARATION

3. **RabbitMQ**
   - Broker de mensajería
   - Puerto AMQP: 5672
   - Management UI: 15672
   - Dead Letter Queue para manejo de errores

4. **PostgreSQL (2 instancias)**
   - Contenedor separado por servicio
   - Aislamiento completo de datos
   - Patrón "Database per Service"

## 🚀 Inicio Rápido

### Prerrequisitos

- Java 17+
- Maven 3.8+
- Docker Desktop
- PowerShell (Windows)

### 1. Iniciar Contenedores Docker

```powershell
# PostgreSQL para Order Service
docker run -d --name order-service-postgres `
  -e POSTGRES_USER=restaurant_user `
  -e POSTGRES_PASSWORD=restaurant_pass `
  -e POSTGRES_DB=restaurant_db `
  -p 5433:5432 postgres:15

# PostgreSQL para Kitchen Worker
docker run -d --name kitchen-worker-postgres `
  -e POSTGRES_USER=restaurant_user `
  -e POSTGRES_PASSWORD=restaurant_pass `
  -e POSTGRES_DB=kitchen_worker_db `
  -p 5434:5432 postgres:15

# RabbitMQ
docker run -d --name restaurant-rabbitmq `
  -p 5672:5672 `
  -p 15672:15672 `
  rabbitmq:3-management

# Esperar a que los servicios estén listos
timeout /t 10 /nobreak
```

### 2. Iniciar Kitchen Worker

```powershell
cd kitchen-worker
mvn spring-boot:run
```

### 3. Iniciar Order Service (en otra terminal)

```powershell
cd order-service
mvn spring-boot:run
```

### 4. Verificar que todo funciona

```powershell
# Obtener menú
Invoke-RestMethod -Uri "http://localhost:8080/menu" -Method Get

# Crear pedido
$body = '{"tableId": 5, "items": [{"productId": 1, "quantity": 2, "note": "Sin cebolla"}]}'
Invoke-RestMethod -Uri "http://localhost:8080/orders" -Method Post -Body $body -ContentType "application/json"
```

## 📚 Documentación

### Swagger UI
Accede a la documentación interactiva de la API:
```
http://localhost:8080/swagger-ui.html
```

### RabbitMQ Management
Monitorea colas y mensajes:
```
http://localhost:15672
Usuario: guest
Contraseña: guest
```

## 🔌 API Endpoints

### Order Service (Puerto 8080)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/menu` | Obtener lista de productos |
| POST | `/orders` | Crear nuevo pedido |
| GET | `/orders` | Listar todos los pedidos |
| GET | `/orders?status=PENDING` | Filtrar pedidos por estado |
| GET | `/orders/{id}` | Obtener pedido por ID |
| PATCH | `/orders/{id}/status` | Actualizar estado del pedido |

### Ejemplos de Uso

#### Crear Pedido
```powershell
$body = @{
    tableId = 5
    items = @(
        @{
            productId = 1
            quantity = 2
            note = "Sin cebolla"
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

## 🧪 Testing

### Ejecutar Tests Unitarios

```powershell
# Order Service
cd order-service
mvn test

# Kitchen Worker
cd kitchen-worker
mvn test
```

### Tests Incluidos
- ✅ 29 tests unitarios en Order Service
- ✅ 9 tests unitarios en Kitchen Worker
- ✅ Tests de controladores
- ✅ Tests de servicios
- ✅ Tests de manejo de excepciones

## 🛠️ Tecnologías

### Backend
- **Spring Boot 3.2.0**: Framework principal
- **Spring Data JPA**: Persistencia de datos
- **Spring AMQP**: Integración con RabbitMQ
- **Flyway**: Migraciones de base de datos
- **Lombok**: Reducción de código boilerplate

### Base de Datos
- **PostgreSQL 15**: Base de datos relacional
- **Hibernate**: ORM

### Mensajería
- **RabbitMQ 3**: Broker de mensajes
- **Jackson**: Serialización JSON

### Documentación
- **SpringDoc OpenAPI**: Generación de documentación Swagger

### Testing
- **JUnit 5**: Framework de testing
- **Mockito**: Mocking
- **jqwik**: Property-based testing

## 📁 Estructura del Proyecto

```
restaurant-order-system/
├── order-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/restaurant/orderservice/
│   │   │   │       ├── config/          # Configuración
│   │   │   │       ├── controller/      # REST Controllers
│   │   │   │       ├── dto/             # Data Transfer Objects
│   │   │   │       ├── entity/          # Entidades JPA
│   │   │   │       ├── enums/           # Enumeraciones
│   │   │   │       ├── event/           # Eventos de dominio
│   │   │   │       ├── exception/       # Excepciones personalizadas
│   │   │   │       ├── repository/      # Repositorios JPA
│   │   │   │       └── service/         # Lógica de negocio
│   │   │   └── resources/
│   │   │       ├── db/migration/        # Migraciones Flyway
│   │   │       └── application.yml      # Configuración
│   │   └── test/                        # Tests unitarios
│   └── pom.xml
│
├── kitchen-worker/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/restaurant/kitchenworker/
│   │   │   │       ├── config/          # Configuración RabbitMQ
│   │   │   │       ├── entity/          # Entidades JPA
│   │   │   │       ├── enums/           # Enumeraciones
│   │   │   │       ├── event/           # Eventos de dominio
│   │   │   │       ├── listener/        # Listeners de RabbitMQ
│   │   │   │       ├── repository/      # Repositorios JPA
│   │   │   │       └── service/         # Lógica de negocio
│   │   │   └── resources/
│   │   │       └── application.yml      # Configuración
│   │   └── test/                        # Tests unitarios
│   └── pom.xml
│
├── .kiro/specs/restaurant-order-system/ # Especificaciones
├── pom.xml                               # POM padre
└── README.md
```

## 🔧 Configuración

### Puertos

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| Order Service | 8080 | API REST |
| Kitchen Worker | 8081 | Servicio interno |
| PostgreSQL (Order) | 5433 | Base de datos Order Service |
| PostgreSQL (Kitchen) | 5434 | Base de datos Kitchen Worker |
| RabbitMQ AMQP | 5672 | Protocolo de mensajería |
| RabbitMQ Management | 15672 | UI de administración |

### Variables de Entorno

Las credenciales por defecto son:

```yaml
# PostgreSQL
POSTGRES_USER: restaurant_user
POSTGRES_PASSWORD: restaurant_pass

# RabbitMQ
RABBITMQ_USER: guest
RABBITMQ_PASSWORD: guest
```

## 🐛 Solución de Problemas

### Error: "Cannot connect to Docker"
```powershell
# Asegúrate de que Docker Desktop está corriendo
docker ps
```

### Error: "Port already in use"
```powershell
# Detén los contenedores existentes
docker stop order-service-postgres kitchen-worker-postgres restaurant-rabbitmq
docker rm order-service-postgres kitchen-worker-postgres restaurant-rabbitmq
```

### Error: "Connection refused" al iniciar servicios
```powershell
# Espera 10-15 segundos después de iniciar los contenedores Docker
timeout /t 15 /nobreak
```

### Ver logs de contenedores
```powershell
docker logs order-service-postgres
docker logs kitchen-worker-postgres
docker logs restaurant-rabbitmq
```

## 📖 Documentación Adicional

- [SISTEMA_FUNCIONANDO.md](SISTEMA_FUNCIONANDO.md) - Guía completa de verificación y pruebas
- [.kiro/specs/restaurant-order-system/](./kiro/specs/restaurant-order-system/) - Especificaciones técnicas detalladas

## 🤝 Contribuir

Este proyecto fue desarrollado siguiendo metodología Spec-Driven Development con:
- Especificaciones formales de requisitos
- Diseño detallado con propiedades de correctitud
- Tests unitarios y de integración
- Documentación completa

## 📄 Licencia

Este proyecto es un ejemplo educativo de arquitectura de microservicios.

## ✨ Características Destacadas

- ✅ Arquitectura de microservicios con bases de datos separadas
- ✅ Comunicación asíncrona mediante eventos
- ✅ Dead Letter Queue para manejo de errores
- ✅ Documentación interactiva con Swagger
- ✅ Migraciones de base de datos con Flyway
- ✅ Tests unitarios completos
- ✅ Manejo robusto de excepciones
- ✅ Validación de datos
- ✅ Logging estructurado

---

**Desarrollado con ❤️ usando Spring Boot y arquitectura de microservicios**
