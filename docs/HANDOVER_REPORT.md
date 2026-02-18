# 📋 Handover Report - Sistema de Pedidos de Restaurante

**Proyecto:** Restaurant Order System


## 🏗️ Arquitectura General

### Tipo de Proyecto
Sistema **Full-Stack** con arquitectura de **microservicios**

Componentes Principales

┌─────────────────┐       ┌──────────────────┐       ┌─────────────────┐
│  Frontend (SPA) │◄─────►│  Order Service   │◄─────►│   PostgreSQL    │
│  React + Vite   │  REST │  Spring Boot     │  JPA  │ restaurant_db   │
└─────────────────┘       └──────────────────┘       └─────────────────┘
                                    │
                                    │ AMQP (Events)
                                    ▼
                          ┌──────────────────┐
                          │    RabbitMQ      │
                          │  Message Broker  │
                          └──────────────────┘
                                    │
                                    │ Consumer
                                    ▼
                          ┌──────────────────┐       ┌─────────────────┐
                          │  Kitchen Worker  │◄─────►│   PostgreSQL    │
                          │  Spring Boot     │  JPA  │   kitchen_db    │
                          └──────────────────┘       └─────────────────┘

Backend: Arquitectura Java Multi-Módulo

1️⃣ Order Service (Servicio Principal)
Arquitectura en Capas:
📂 order-service/src/main/java/com/restaurant/orderservice/
├── 🚪 controller/          → API REST (Endpoints HTTP)
├── 🔧 service/             → Lógica de negocio
├── 🗄️  repository/         → Acceso a datos (Spring Data JPA)
├── 📦 entity/              → Entidades JPA (tablas DB)
├── 📝 dto/                 → Data Transfer Objects
├── 🏛️  domain/             → Modelos de dominio
├── 🔐 security/            → Autenticación (Kitchen Token)
├── ⚙️  config/             → Configuración Spring
├── 🚀 application/         → Casos de uso / Application Layer
├── 🏗️  infrastructure/     → Implementaciones técnicas
├── 🚨 exception/           → Manejo de errores
└── 🔢 enums/               → Enumeraciones (OrderStatus, etc.)

Dependencias Principales:

Spring Boot Web → REST API
Spring Data JPA → ORM para PostgreSQL
Spring AMQP → Publicación de eventos a RabbitMQ
Spring Validation → Validación de DTOs
Flyway → Migraciones de base de datos
SpringDoc OpenAPI → Documentación Swagger
Lombok → Reducción de boilerplate
Responsabilidades:

✅ Gestionar pedidos (CRUD)
✅ Exponer menú de productos
✅ Publicar eventos order.placed a RabbitMQ
✅ Validar token de cocina (X-Kitchen-Token)

2️⃣ Kitchen Worker (Event Consumer)
Arquitectura Event-Driven:

📂 kitchen-worker/src/main/java/
├── 📥 consumer/            → Listeners de RabbitMQ
├── 🔧 service/             → Procesamiento de eventos
├── 🗄️  repository/         → Persistencia en kitchen_db
├── 📦 entity/              → Entidades de cocina
├── 📝 dto/                 → Eventos recibidos
└── ⚙️  config/             → Configuración AMQP

Dependencias Principales:

Spring Boot (sin Web, solo worker)
Spring Data JPA → Base de datos independiente
Spring AMQP → Consumo de mensajes RabbitMQ
Flyway → Migraciones
Jackson → Deserialización JSON
Responsabilidades:

✅ Escuchar eventos order.placed desde RabbitMQ
✅ Procesar pedidos y cambiar estado a IN_PREPARATION
✅ Persistir en base de datos separada (kitchen_db)

⚛️ Frontend: Arquitectura React

Build Tools:

Vite → Bundler ultrarrápido
TypeScript → Tipado estático
Tailwind CSS → Utility-first CSS
ESLint → Linting
Arquitectura Frontend (Clean Architecture)

📂 src/
├── 📄 pages/
│   ├── client/          → Vistas del cliente (selección menú, carrito)
│   ├── kitchen/         → Dashboard de cocina
│   └── WelcomePage.tsx  → Página inicial
│
├── 🧩 components/       → Componentes reutilizables UI
│   ├── ui/              → Componentes base (Button, Card, etc.)
│   ├── AppLayout.tsx
│   ├── TopNav.tsx
│   └── RequireKitchenAuth.tsx
│
├── 🌐 api/              → Capa de comunicación con backend
│   ├── http.ts          → Cliente HTTP (fetch wrapper)
│   ├── orders.ts        → API de pedidos
│   ├── menu.ts          → API de menú
│   ├── mock.ts          → Mock data para desarrollo
│   ├── contracts.ts     → Tipos TypeScript (contratos)
│   └── env.ts           → Variables de entorno
│
├── 🏪 store/            → Estado Global
│   ├── cart.tsx         → Context API para carrito
│   └── kitchenAuth.ts   → Autenticación de cocina
│
├── 🧠 domain/           → Lógica de dominio
│   ├── orderStatus.ts   → Mapeo de estados
│   └── productLabel.ts  → Labels de productos
│
├── 🎨 assets/           → Imágenes y recursos
│   └── menu/            → Imágenes de platos
│
└── 📱 app/
    └── context.tsx      → Context providers globales


🔄 Flujo de Datos Completo
Escenario: Cliente hace un pedido

sequenceDiagram
    participant U as 👤 Usuario
    participant F as ⚛️ Frontend
    participant O as 🍽️ Order Service
    participant R as 🐰 RabbitMQ
    participant K as 👨‍🍳 Kitchen Worker
    participant D1 as 🗄️ restaurant_db
    participant D2 as 🗄️ kitchen_db

    U->>F: Selecciona platos + mesa
    F->>F: Actualiza carrito (Context)
    U->>F: Confirma pedido
    F->>O: POST /orders
    O->>D1: INSERT orden (PENDING)
    O->>R: Publish event: order.placed
    O-->>F: 201 Created {id, status: PENDING}
    F->>U: Mostrar confirmación
    
    R->>K: Deliver event: order.placed
    K->>K: Procesar orden
    K->>D2: INSERT orden cocina (IN_PREPARATION)
    K-->>R: ACK mensaje
    
    U->>F: Consultar estado
    F->>O: GET /orders/{id}
    O->>D1: SELECT orden
    O-->>F: {status: IN_PREPARATION}
    F->>U: Actualizar UI


🗄️ Capa de Persistencia
Bases de Datos PostgreSQL (Segregación de Datos)
┌─────────────────────┐         ┌─────────────────────┐
│   restaurant_db     │         │     kitchen_db      │
│  (Order Service)    │         │  (Kitchen Worker)   │
├─────────────────────┤         ├─────────────────────┤
│ • orders            │         │ • kitchen_orders    │
│ • menu_items        │         │ • order_items       │
│ • tables            │         │ • processing_logs   │
│ • order_items       │         │                     │
└─────────────────────┘         └─────────────────────┘

### Stack Tecnológico

**Backend:**
- Java 17
- Spring Boot 3.2.0
- Maven (Multi-módulo)
- PostgreSQL 42.7.1
- Lombok 1.18.30

**Frontend:**
- React 18+ con TypeScript
- Vite (Build tool)
- TailwindCSS
- ESLint

**Infraestructura:**
- Docker & Docker Compose
- Base de datos PostgreSQL

---

🎯 Patrones de Diseño Aplicados
Arquitectura Hexagonal (Ports & Adapters)

application/ → Casos de uso
domain/ → Lógica de negocio pura
infrastructure → Implementaciones técnicas
Event-Driven Architecture

Desacoplamiento mediante RabbitMQ
Asincronía entre Order Service y Kitchen Worker
Repository Pattern

Abstracción de acceso a datos con Spring Data JPA
DTO Pattern

Separación entre entidades y contratos API
Context API (React)

Estado global sin Redux (carrito, auth)


📊 Resumen de Capas

┌────────────────────────────────────────────────────────┐
│                  🌐 PRESENTATION                       │
│  React Components + Pages (Client & Kitchen)          │
└────────────────────────────────────────────────────────┘
                         ↕️
┌────────────────────────────────────────────────────────┐
│                   🔌 API LAYER                         │
│  HTTP Client (fetch) + TanStack Query                  │
└────────────────────────────────────────────────────────┘
                         ↕️
┌────────────────────────────────────────────────────────┐
│                 🍽️ REST API (Spring)                   │
│  Controllers + DTOs + OpenAPI Docs                     │
└────────────────────────────────────────────────────────┘
                         ↕️
┌────────────────────────────────────────────────────────┐
│               💼 BUSINESS LOGIC                        │
│  Services + Domain Models + Use Cases                  │
└────────────────────────────────────────────────────────┘
                         ↕️
┌────────────────────────────────────────────────────────┐
│              🗄️ DATA ACCESS LAYER                      │
│  Repositories (JPA) + Entities                         │
└────────────────────────────────────────────────────────┘
                         ↕️
┌────────────────────────────────────────────────────────┐
│                 💾 DATABASE                            │
│  PostgreSQL (restaurant_db + kitchen_db)               │
└────────────────────────────────────────────────────────┘

## ⚠️ Riesgos Técnicos

### 🔴 Alto Impacto

1. **Comunicación entre microservicios no documentada**
   - ¿Cómo se comunican `order-service` y `kitchen-worker`?
   - ¿REST, eventos, mensajería (RabbitMQ/Kafka)?
   - **Acción:** Documentar protocolo de comunicación

2. **Falta de especificación OpenAPI**
   - Carpeta [openspec/](openspec/) existe pero contenido desconocido
   - **Acción:** Verificar si existe especificación Swagger/OpenAPI actualizada

3. **Configuración de entorno incompleta**
   - [.env](.env) y [.env.example](.env.example) presentes
   - **Acción:** Validar variables de entorno necesarias

### 🟡 Medio Impacto

4. **Dockerización parcial**
   - Múltiples Dockerfiles: [Dockerfile](Dockerfile), [Dockerfile.frontend](Dockerfile.frontend)
   - ¿Existe docker-compose.yml funcional?
   - **Acción:** Verificar orquestación completa

5. **Testing avanzado sin cobertura conocida**
   - Uso de jqwik (Property-Based Testing) indica madurez
   - Cobertura actual desconocida
   - **Acción:** Generar reporte de cobertura

6. **Dependencia obsolescencia**
   - Spring Boot 3.2.0 (lanzado late 2023)
   - **Acción:** Revisar actualizaciones de seguridad

### 🟢 Bajo Impacto

7. **Documentación fragmentada**
   - Existe [docs/](docs/) con auditoría, desarrollo, calidad
   - [AI_WORKFLOW.md](AI_WORKFLOW.md) sugiere flujo con IA
   - **Acción:** Consolidar documentación técnica

---

## 📊 Nivel de Calidad Actual

### ✅ Fortalezas

1. **Arquitectura moderna y escalable**
   - Microservicios bien separados
   - Java 17 (LTS) y Spring Boot 3.x

2. **Testing de calidad**
   - Uso de **jqwik** para Property-Based Testing (avanzado)
   - Framework de testing robusto

3. **Tecnologías actuales**
   - React con TypeScript (type-safety)
   - Vite (build rápido, HMR)
   - TailwindCSS (desarrollo ágil de UI)

4. **Containerización**
   - Docker setup para backend y frontend
   - Base de datos dockerizada

5. **Estructura organizada**
   - Multi-módulo Maven lógico
   - Separación de responsabilidades

### ⚠️ Áreas de Mejora

1. **Documentación API**
   - OpenAPI spec requiere verificación

2. **Monitoreo y logging**
   - No se evidencia stack de observabilidad
   - Considerar: Spring Actuator, Micrometer, ELK

3. **CI/CD**
   - No hay evidencia de pipelines
   - [scripts/](scripts/) existe pero contenido desconocido

4. **Seguridad**
   - No se menciona Spring Security
   - Autenticación/Autorización no documentada

---

## 🚀 Oportunidades de Mejora

### Prioridad Alta

1. **Documentar API con OpenAPI 3.0**
   ```xml
   <dependency>
       <groupId>org.springdoc</groupId>
       <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
   </dependency>
   ```
   - Generar Swagger UI automático
   - Sincronizar con carpeta [openspec/](openspec/)

2. **Implementar Observabilidad**
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   ```
   - Health checks
   - Métricas de negocio
   - Logging estructurado (Logback con JSON)

3. **Agregar Spring Security**
   - JWT para autenticación stateless
   - CORS configuration para frontend

### Prioridad Media

4. **Migrar a arquitectura de eventos (opcional)**
   - Si `kitchen-worker` necesita escalabilidad
   - Considerar: Spring Cloud Stream + RabbitMQ/Kafka

5. **Tests E2E automatizados**
   - Backend: RestAssured + Testcontainers
   - Frontend: Playwright/Cypress

6. **Pipeline CI/CD**
   ```yaml
   # .github/workflows/ci.yml
   - Build Maven
   - Run tests + coverage
   - Build Docker images
   - Deploy to staging
   ```

7. **Frontend State Management**
   - Si la app crece: Redux Toolkit o Zustand
   - Gestión centralizada de estado

### Prioridad Baja

8. **Internacionalización (i18n)**
   - Frontend: react-i18next
   - Backend: ResourceBundle

9. **Rate Limiting**
   - Spring Cloud Gateway o Bucket4j

10. **Caché distribuido**
    - Redis para sesiones o caché de consultas frecuentes

---


## 👥Referencias

**Documentación Técnica:**
- [README.md](README.md) - Guía de inicio
- [docs/](docs/) - Documentación adicional
- [AI_WORKFLOW.md](AI_WORKFLOW.md) - Flujo de trabajo con IA

**Archivos Clave:**
- [pom.xml](pom.xml) - Configuración Maven padre
- [package.json](package.json) - Dependencias frontend
- [.env.example](.env.example) - Variables de entorno necesarias

---