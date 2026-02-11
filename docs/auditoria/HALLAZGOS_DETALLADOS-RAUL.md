# Hallazgos Detallados de Auditoría
**Fecha:** 11 de febrero de 2026  
**Punto Base:** Commit `audit: snapshot post-mvp`  
**Total Hallazgos:** 23 (10 Críticos, 11 Moderados, 2 Leves)

---

## HALLAZGOS CRÍTICOS

### Hallazgo 1.1 - Violación SRP en OrderService

- **Componente:** OrderService
- **Ruta/Módulo:** `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`
- **Tipo:** SOLID
- **Principio/Smell:** Single Responsibility Principle (SRP)
- **Severidad:** Alta

#### Evidencia

- **Descripción:** La clase `OrderService` tiene 6+ responsabilidades: validación de negocio, persistencia de datos, mapeo DTO↔Entity, publicación de eventos, construcción de eventos y orquestación. Esto viola el principio de responsabilidad única.
- **Referencia técnica:** Líneas 1-280, método `createOrder()` ejecuta validación (88-103), persistencia (125), publicación eventos (133), mapeo (220-260)

#### Impacto

- **Mantenibilidad:** Cambios en validación afectan persistencia. Difícil crear tests unitarios aislados. Lógica de validación no reutilizable.
- **Escalabilidad:** Imposible cambiar estrategia de mapeo sin modificar servicio. Clase de 280 líneas dificulta colaboración en equipo.
- **Riesgo operativo:** Alto - Cambios riesgosos, testing incompleto, conflictos en merge frecuentes

#### Recomendación

- **Acción sugerida:** Separar en clases especializadas: `OrderValidator` (validación), `OrderMapper` (mapeo), `OrderEventBuilder` (eventos), `OrderService` (orquestación)
- **Patrón candidato Fase 2:** Facade Pattern + Strategy Pattern para validación
- **Punto de entrada para refactor Fase 3:** Implementar Clean Architecture con capa de dominio separada

---

### Hallazgo 1.2 - Violación DIP en OrderService

- **Componente:** OrderService
- **Ruta/Módulo:** `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`
- **Tipo:** SOLID
- **Principio/Smell:** Dependency Inversion Principle (DIP)
- **Severidad:** Alta

#### Evidencia

- **Descripción:** `OrderService` depende directamente de implementaciones concretas (`OrderRepository`, `ProductRepository`, `OrderEventPublisher`) en lugar de abstracciones/interfaces.
- **Referencia técnica:** Líneas 35-48, constructor con dependencias concretas de JPA

#### Impacto

- **Mantenibilidad:** Imposible mockear repositorios sin frameworks pesados. Cambios en repositorios afectan directamente al servicio.
- **Escalabilidad:** No se puede cambiar implementación de persistencia (Redis, MongoDB). Difícil migrar a arquitectura de microservicios.
- **Riesgo operativo:** Alto - Acoplamiento rígido limita flexibilidad tecnológica

#### Recomendación

- **Acción sugerida:** Crear interfaces `IOrderRepository`, `IProductRepository`, `IEventPublisher` en capa de dominio
- **Patrón candidato Fase 2:** Repository Pattern con interfaces + Adapter Pattern
- **Punto de entrada para refactor Fase 3:** Hexagonal Architecture con ports & adapters

---

### Hallazgo 1.5 - Eventos Perdidos sin Compensación

- **Componente:** OrderEventPublisher
- **Ruta/Módulo:** `order-service/src/main/java/com/restaurant/orderservice/service/OrderEventPublisher.java`
- **Tipo:** Code Smell
- **Principio/Smell:** Reliability & Consistency
- **Severidad:** Alta (Crítico)

#### Evidencia

- **Descripción:** El publicador silencia excepciones sin estrategia de retry o compensación. Eventos perdidos no se registran ni se reintentan.
- **Referencia técnica:** Líneas 38-50, catch block que solo logea error sin throw ni compensación

#### Impacto

- **Mantenibilidad:** Difícil rastrear pedidos "fantasma" que no llegan a cocina
- **Escalabilidad:** Consistencia eventual comprometida. Pedidos creados pero no procesados.
- **Riesgo operativo:** Crítico - Pérdida de datos, afecta SLA y experiencia de usuario

#### Recomendación

- **Acción sugerida:** Implementar Outbox Pattern con tabla `outbox_events` y worker dedicado
- **Patrón candidato Fase 2:** Transactional Outbox Pattern + Polling Publisher
- **Punto de entrada para refactor Fase 3:** Event Sourcing o CDC (Change Data Capture)

---

### Hallazgo 1.6 - Acoplamiento Temporal

- **Componente:** OrderService
- **Ruta/Módulo:** `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`
- **Tipo:** Code Smell
- **Principio/Smell:** Temporal Coupling
- **Severidad:** Alta

#### Evidencia

- **Descripción:** La creación de pedido está acoplada temporalmente a la publicación del evento. Si falla la publicación, la transacción ya se commitió.
- **Referencia técnica:** Líneas 125-135, `save()` seguido de `publishOrderPlacedEvent()` fuera de transacción

#### Impacto

- **Mantenibilidad:** No se puede deshacer pedido si falla publicación
- **Escalabilidad:** Cliente espera a RabbitMQ. Fallo en RabbitMQ afecta creación de pedidos.
- **Riesgo operativo:** Alto - Atomicidad comprometida, transacción DB ≠ publicación evento

#### Recomendación

- **Acción sugerida:** Usar `@TransactionalEventListener` o implementar Outbox Pattern
- **Patrón candidato Fase 2:** Spring TransactionalEventListener + Async Processing
- **Punto de entrada para refactor Fase 3:** SAGA Pattern para transacciones distribuidas

---

### Hallazgo 2.3 - Falta de Idempotencia

- **Componente:** OrderProcessingService
- **Ruta/Módulo:** `kitchen-worker/src/main/java/com/restaurant/kitchenworker/service/OrderProcessingService.java`
- **Tipo:** Code Smell
- **Principio/Smell:** Idempotency
- **Severidad:** Alta (Crítico)

#### Evidencia

- **Descripción:** El procesamiento de eventos no es idempotente. Procesar el mismo evento múltiples veces puede causar inconsistencias. No verifica estado actual antes de actualizar.
- **Referencia técnica:** Líneas 40-55, actualiza status sin verificar estado previo

#### Impacto

- **Mantenibilidad:** Difícil rastrear inconsistencias causadas por eventos duplicados
- **Escalabilidad:** Eventos duplicados causan estados inválidos. Reintentos pueden corromper datos.
- **Riesgo operativo:** Crítico - Corrupción de datos en sistemas distribuidos

#### Recomendación

- **Acción sugerida:** Verificar estado antes de actualizar: `if (order.getStatus() == OrderStatus.PENDING)`
- **Patrón candidato Fase 2:** State Machine Pattern + Idempotent Consumer
- **Punto de entrada para refactor Fase 3:** Event Deduplication con tabla de eventos procesados

---

### Hallazgo 3.1 - God Component en KitchenBoardPage

- **Componente:** KitchenBoardPage
- **Ruta/Módulo:** `src/pages/kitchen/KitchenBoardPage.tsx`
- **Tipo:** SOLID
- **Principio/Smell:** Single Responsibility Principle (SRP)
- **Severidad:** Alta

#### Evidencia

- **Descripción:** Componente de 150+ líneas con 6 responsabilidades: gestión de estado (7 estados), lógica de polling, llamadas API, renderizado UI, manejo de errores, agrupación de datos.
- **Referencia técnica:** Líneas 1-150, múltiples useState, useRef, useCallback, useMemo mezclados

#### Impacto

- **Mantenibilidad:** Imposible testear lógica sin renderizar componente. Lógica de polling no reutilizable.
- **Escalabilidad:** Re-renders innecesarios. Conflictos en merge frecuentes.
- **Riesgo operativo:** Alto - Componente frágil, cambios riesgosos

#### Recomendación

- **Acción sugerida:** Separar en custom hooks: `useOrderPolling`, `useGroupedOrders`, componentes UI
- **Patrón candidato Fase 2:** Custom Hooks Pattern + Presentational/Container Components
- **Punto de entrada para refactor Fase 3:** Feature-Sliced Design con features/orders

---

### Hallazgo 3.2 - Acoplamiento a API Concreta

- **Componente:** CartPage, MenuPage, KitchenBoardPage
- **Ruta/Módulo:** `src/pages/client/CartPage.tsx`, `src/pages/client/MenuPage.tsx`
- **Tipo:** SOLID
- **Principio/Smell:** Dependency Inversion Principle (DIP)
- **Severidad:** Alta

#### Evidencia

- **Descripción:** Componentes dependen directamente de funciones API concretas (`createOrder`, `listOrders`). Import directo sin abstracción.
- **Referencia técnica:** `import { createOrder } from '@/api/orders'` en múltiples componentes

#### Impacto

- **Mantenibilidad:** Imposible testear sin API real. Requiere interceptores HTTP complejos.
- **Escalabilidad:** No se puede cambiar implementación (REST → GraphQL). Difícil migrar a múltiples backends.
- **Riesgo operativo:** Alto - Testing imposible, migración bloqueada

#### Recomendación

- **Acción sugerida:** Crear interfaces `IOrderService`, `IMenuService` con implementaciones `RestOrderService`
- **Patrón candidato Fase 2:** Service Layer Pattern + Dependency Injection
- **Punto de entrada para refactor Fase 3:** Repository Pattern en frontend con React Context

---

### Hallazgo 3.3 - God Object en CartProvider

- **Componente:** CartProvider
- **Ruta/Módulo:** `src/store/cart.tsx`
- **Tipo:** SOLID
- **Principio/Smell:** Single Responsibility Principle (SRP)
- **Severidad:** Alta

#### Evidencia

- **Descripción:** 120 líneas manejando 5 responsabilidades: estado del carrito, persistencia localStorage, lógica de negocio, serialización, validación.
- **Referencia técnica:** Líneas 1-120, funciones `load()`, `save()`, `reducer()`, `cartTotals()` mezcladas

#### Impacto

- **Mantenibilidad:** Difícil testear lógica sin Context. Lógica de negocio acoplada a React.
- **Escalabilidad:** Re-renders en toda la app. No escalable a estados complejos.
- **Riesgo operativo:** Alto - Estado global frágil, performance comprometida

#### Recomendación

- **Acción sugerida:** Separar en `CartState` (lógica pura), `CartStorage` (persistencia), `CartProvider` (Context)
- **Patrón candidato Fase 2:** Domain Model + Repository Pattern + Context API
- **Punto de entrada para refactor Fase 3:** State Management con Zustand o Redux Toolkit

---

### Hallazgo 4.1 - Falta de Capas Arquitectónicas

- **Componente:** Sistema completo
- **Ruta/Módulo:** Backend: `order-service/`, Frontend: `src/`
- **Tipo:** Code Smell
- **Principio/Smell:** Separation of Concerns
- **Severidad:** Alta (Crítico)

#### Evidencia

- **Descripción:** No hay separación clara entre capas de presentación, lógica de negocio y persistencia. Backend mezcla service con persistencia. Frontend mezcla pages con lógica.
- **Referencia técnica:** Estructura de carpetas sin capas definidas

#### Impacto

- **Mantenibilidad:** Cambios en una capa afectan otras. Imposible testear capas aisladamente.
- **Escalabilidad:** Código no reutilizable. Difícil extraer microservicios. Equipos no pueden trabajar independientemente.
- **Riesgo operativo:** Crítico - Escalabilidad limitada, arquitectura no sostenible

#### Recomendación

- **Acción sugerida:** Implementar Clean Architecture (backend) y Feature-Sliced Design (frontend)
- **Patrón candidato Fase 2:** Layered Architecture con domain/application/infrastructure
- **Punto de entrada para refactor Fase 3:** Hexagonal Architecture completa con ports & adapters

---

### Hallazgo 4.2 - Sin Abstracción de Persistencia

- **Componente:** Repositorios
- **Ruta/Módulo:** `order-service/repository/`, `kitchen-worker/repository/`
- **Tipo:** SOLID
- **Principio/Smell:** Dependency Inversion Principle (DIP)
- **Severidad:** Alta

#### Evidencia

- **Descripción:** Repositorios no tienen interfaces de dominio. Servicios acoplados a implementaciones concretas de JPA (Spring Data).
- **Referencia técnica:** `OrderRepository extends JpaRepository` sin interface de dominio

#### Impacto

- **Mantenibilidad:** Requiere base de datos o mocks complejos para testing
- **Escalabilidad:** No se puede cambiar a MongoDB, Redis, etc. No se puede agregar caché sin modificar servicios.
- **Riesgo operativo:** Alto - Acoplamiento a tecnología específica, migración bloqueada

#### Recomendación

- **Acción sugerida:** Crear interfaces `IOrderRepository` en domain, implementar `JpaOrderRepository` en infrastructure
- **Patrón candidato Fase 2:** Repository Pattern con interfaces + Adapter Pattern
- **Punto de entrada para refactor Fase 3:** CQRS con repositorios separados para lectura/escritura

---

## HALLAZGOS MODERADOS

### Hallazgo 1.3 - N+1 Query Problem

- **Componente:** OrderService
- **Ruta/Módulo:** `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`
- **Tipo:** Code Smell
- **Principio/Smell:** Performance Best Practices
- **Severidad:** Media

#### Evidencia

- **Descripción:** Método `mapToOrderItemResponse` ejecuta una query por cada item del pedido para obtener nombre del producto. 1 pedido con 10 items = 11 queries.
- **Referencia técnica:** Líneas 250-260, `productRepository.findById()` dentro de loop

#### Impacto

- **Mantenibilidad:** Código ineficiente dificulta optimización futura
- **Escalabilidad:** Latencia aumenta linealmente con número de items. Sobrecarga en PostgreSQL.
- **Riesgo operativo:** Medio - Performance degradada, mayor uso de conexiones DB

#### Recomendación

- **Acción sugerida:** Usar `@EntityGraph` o fetch join: `@Query("SELECT o FROM Order o LEFT JOIN FETCH o.items")`
- **Patrón candidato Fase 2:** Eager Loading con EntityGraph + DTO Projection
- **Punto de entrada para refactor Fase 3:** GraphQL con DataLoader para batch loading

---

### Hallazgo 1.4 - God Class en OrderService

- **Componente:** OrderService
- **Ruta/Módulo:** `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`
- **Tipo:** Code Smell
- **Principio/Smell:** God Class
- **Severidad:** Media

#### Evidencia

- **Descripción:** Clase de 280 líneas con 8 métodos públicos, 3 privados, 3 dependencias, 6+ responsabilidades. Complejidad ciclomática alta.
- **Referencia técnica:** Líneas 1-280, múltiples métodos con lógica compleja

#### Impacto

- **Mantenibilidad:** Cambios riesgosos. Difícil de entender.
- **Escalabilidad:** Testing con cobertura incompleta. Conflictos en merge.
- **Riesgo operativo:** Medio - Complejidad alta dificulta mantenimiento

#### Recomendación

- **Acción sugerida:** Aplicar Facade Pattern y delegar a servicios especializados
- **Patrón candidato Fase 2:** Facade + Command Pattern para operaciones
- **Punto de entrada para refactor Fase 3:** CQRS con comandos y queries separados

---

### Hallazgo 2.1 - Lógica Mezclada con Logging

- **Componente:** OrderProcessingService
- **Ruta/Módulo:** `kitchen-worker/src/main/java/com/restaurant/kitchenworker/service/OrderProcessingService.java`
- **Tipo:** SOLID
- **Principio/Smell:** Single Responsibility Principle (SRP)
- **Severidad:** Media

#### Evidencia

- **Descripción:** Servicio mezcla lógica de procesamiento con manejo de errores y logging. Difícil separar concerns.
- **Referencia técnica:** Líneas 1-60, múltiples `log.info()` y `log.error()` mezclados con lógica

#### Impacto

- **Mantenibilidad:** Difícil testear lógica sin logging. Cambios en error handling afectan negocio.
- **Escalabilidad:** Observabilidad mezclada con negocio. Agregar validaciones complica método.
- **Riesgo operativo:** Medio - Testing difícil, extensibilidad limitada

#### Recomendación

- **Acción sugerida:** Separar en `OrderProcessor` (lógica pura), `OrderProcessingService` (orquestación), `OrderProcessingLogger`
- **Patrón candidato Fase 2:** Decorator Pattern para logging + Template Method
- **Punto de entrada para refactor Fase 3:** AOP (Aspect-Oriented Programming) para cross-cutting concerns

---

### Hallazgo 2.2 - Acoplamiento Directo en Listener

- **Componente:** OrderEventListener
- **Ruta/Módulo:** `kitchen-worker/src/main/java/com/restaurant/kitchenworker/listener/OrderEventListener.java`
- **Tipo:** SOLID
- **Principio/Smell:** Dependency Inversion Principle (DIP)
- **Severidad:** Media

#### Evidencia

- **Descripción:** Listener acoplado directamente a `OrderProcessingService`. No hay abstracción para procesamiento.
- **Referencia técnica:** Líneas 1-50, `@Autowired OrderProcessingService` con llamada directa

#### Impacto

- **Mantenibilidad:** Difícil testear listener sin servicio real
- **Escalabilidad:** No se puede cambiar implementación. Agregar procesadores requiere modificar listener.
- **Riesgo operativo:** Medio - Extensibilidad limitada, difícil distribuir procesamiento

#### Recomendación

- **Acción sugerida:** Usar patrón Command o Strategy: `EventProcessor<OrderPlacedEvent>`
- **Patrón candidato Fase 2:** Command Pattern + Chain of Responsibility
- **Punto de entrada para refactor Fase 3:** Event-Driven Architecture con múltiples handlers

---

### Hallazgo 3.4 - Duplicación de Error Handling

- **Componente:** Múltiples componentes frontend
- **Ruta/Módulo:** `src/pages/client/CartPage.tsx`, `src/pages/client/MenuPage.tsx`, `src/pages/kitchen/KitchenBoardPage.tsx`
- **Tipo:** Code Smell
- **Principio/Smell:** DRY (Don't Repeat Yourself)
- **Severidad:** Media

#### Evidencia

- **Descripción:** Lógica de manejo de errores duplicada en múltiples componentes. Mismo patrón repetido 3+ veces.
- **Referencia técnica:** Líneas 20-28 (CartPage), 30-40 (MenuPage), 35-45 (KitchenBoardPage)

#### Impacto

- **Mantenibilidad:** Cambios en error handling requieren modificar N archivos. Mensajes de error inconsistentes.
- **Escalabilidad:** Duplicación de tests. Agregar logging/tracking requiere cambios masivos.
- **Riesgo operativo:** Medio - Mantenimiento costoso, inconsistencia

#### Recomendación

- **Acción sugerida:** Crear hook reutilizable `useErrorHandler()` con logging centralizado
- **Patrón candidato Fase 2:** Custom Hook Pattern + Error Boundary
- **Punto de entrada para refactor Fase 3:** Global Error Handler con Sentry integration

---

### Hallazgo 3.5 - Acoplamiento a useNavigate

- **Componente:** Múltiples componentes frontend
- **Ruta/Módulo:** `src/pages/client/CartPage.tsx`, `src/pages/client/MenuPage.tsx`, `src/pages/kitchen/KitchenBoardPage.tsx`
- **Tipo:** SOLID
- **Principio/Smell:** Dependency Inversion Principle (DIP)
- **Severidad:** Media

#### Evidencia

- **Descripción:** Componentes acoplados a `react-router-dom` directamente. Import y uso directo de `useNavigate()`.
- **Referencia técnica:** Línea 11 (CartPage), 13 (MenuPage), 10 (KitchenBoardPage)

#### Impacto

- **Mantenibilidad:** Difícil testear sin router. Componentes no reutilizables fuera de router.
- **Escalabilidad:** No se puede cambiar librería de routing. Difícil migrar a Next.js o Remix.
- **Riesgo operativo:** Medio - Migración difícil, testing complicado

#### Recomendación

- **Acción sugerida:** Abstraer navegación con `useNavigation()` hook que expone `INavigationService`
- **Patrón candidato Fase 2:** Facade Pattern para routing + Service Locator
- **Punto de entrada para refactor Fase 3:** Navigation Service con history management

---

### Hallazgo 3.6 - Lógica de Negocio en Componentes

- **Componente:** MenuPage
- **Ruta/Módulo:** `src/pages/client/MenuPage.tsx`
- **Tipo:** Code Smell
- **Principio/Smell:** Separation of Concerns
- **Severidad:** Media

#### Evidencia

- **Descripción:** Lógica de negocio (agregar al carrito, calcular totales) mezclada con UI. Cálculos en JSX.
- **Referencia técnica:** Líneas 50-85, `cartTotals()` en componente, `find()` en JSX

#### Impacto

- **Mantenibilidad:** Imposible testear lógica sin renderizar. Lógica no reutilizable.
- **Escalabilidad:** Cálculos en cada render. Cambios en lógica requieren modificar UI.
- **Riesgo operativo:** Medio - Performance comprometida, testing imposible

#### Recomendación

- **Acción sugerida:** Mover lógica a hooks: `useCartOperations()` con `getItemQuantity()`, `getTotals()`
- **Patrón candidato Fase 2:** Custom Hooks Pattern + Business Logic Layer
- **Punto de entrada para refactor Fase 3:** Domain Services en frontend con pure functions

---

### Hallazgo 4.3 - Falta de Estrategia de Caché

- **Componente:** Sistema completo
- **Ruta/Módulo:** Backend: `order-service/service/`, Frontend: `src/api/`
- **Tipo:** Code Smell
- **Principio/Smell:** Performance Best Practices
- **Severidad:** Media

#### Evidencia

- **Descripción:** No hay caché implementado. Queries repetitivas a BD. Frontend no cachea respuestas API.
- **Referencia técnica:** `OrderService.mapToOrderItemResponse()` N+1, `MenuService.getMenu()` sin caché

#### Impacto

- **Mantenibilidad:** Código ineficiente dificulta optimización
- **Escalabilidad:** Latencia alta en endpoints. Sobrecarga en base de datos. Mayor uso de recursos.
- **Riesgo operativo:** Medio - Performance degradada, costo elevado

#### Recomendación

- **Acción sugerida:** Backend: `@Cacheable` con Redis. Frontend: configurar `staleTime` en React Query
- **Patrón candidato Fase 2:** Cache-Aside Pattern + TTL Strategy
- **Punto de entrada para refactor Fase 3:** Distributed Cache con invalidación inteligente

---

### Hallazgo 4.4 - Falta de Observabilidad

- **Componente:** Sistema completo
- **Ruta/Módulo:** Backend: `order-service/`, `kitchen-worker/`, Frontend: `src/`
- **Tipo:** Code Smell
- **Principio/Smell:** Monitoring & Observability
- **Severidad:** Media

#### Evidencia

- **Descripción:** No hay tracing distribuido, métricas ni logging estructurado. Logs sin contexto de correlación.
- **Referencia técnica:** `log.info()` sin trace ID, no hay métricas de performance

#### Impacto

- **Mantenibilidad:** Difícil rastrear requests entre servicios. No se pueden detectar cuellos de botella.
- **Escalabilidad:** No hay alertas proactivas. No se pueden medir tiempos de respuesta.
- **Riesgo operativo:** Medio - Debugging difícil, no hay SLA measurement

#### Recomendación

- **Acción sugerida:** Backend: Micrometer + Zipkin. Frontend: Sentry con tracing
- **Patrón candidato Fase 2:** Distributed Tracing + Metrics Collection
- **Punto de entrada para refactor Fase 3:** Full Observability Stack (Logs + Metrics + Traces)

---

### Hallazgo 4.5 - Validación Inconsistente

- **Componente:** Sistema completo
- **Ruta/Módulo:** Backend: `OrderService.java`, `CreateOrderRequest.java`, Frontend: `CartPage.tsx`
- **Tipo:** Code Smell
- **Principio/Smell:** Consistency
- **Severidad:** Media

#### Evidencia

- **Descripción:** Validación de datos inconsistente entre capas. Backend usa validación manual + anotaciones. Frontend validación manual sin runtime checks.
- **Referencia técnica:** Validación duplicada en servicio y DTO, frontend sin validación de tipos

#### Impacto

- **Mantenibilidad:** Reglas de validación duplicadas. Cambios requieren modificar múltiples lugares.
- **Escalabilidad:** Validación inconsistente permite datos inválidos. Mensajes de error inconsistentes.
- **Riesgo operativo:** Medio - Seguridad comprometida, UX inconsistente

#### Recomendación

- **Acción sugerida:** Backend: `OrderValidator` centralizado. Frontend: Zod schemas
- **Patrón candidato Fase 2:** Validator Pattern + Schema Validation
- **Punto de entrada para refactor Fase 3:** Shared validation schemas entre backend/frontend

---

### Hallazgo 4.6 - Sin Rate Limiting ni Throttling

- **Componente:** Sistema completo
- **Ruta/Módulo:** Backend: endpoints, Frontend: `KitchenBoardPage.tsx` polling
- **Tipo:** Code Smell
- **Principio/Smell:** Availability & Performance
- **Severidad:** Media

#### Evidencia

- **Descripción:** No hay rate limiting en APIs. Polling frontend sin throttling (cada 3 segundos sin control).
- **Referencia técnica:** Endpoints sin rate limiting, `setTimeout()` sin exponential backoff

#### Impacto

- **Mantenibilidad:** Código vulnerable a abuso
- **Escalabilidad:** Sobrecarga en backend. Posible DoS accidental. Batería y datos móviles consumidos.
- **Riesgo operativo:** Medio - Disponibilidad comprometida, costo elevado

#### Recomendación

- **Acción sugerida:** Backend: Bucket4j rate limiter. Frontend: exponential backoff en polling
- **Patrón candidato Fase 2:** Token Bucket Algorithm + Backoff Strategy
- **Punto de entrada para refactor Fase 3:** API Gateway con rate limiting centralizado

---

## HALLAZGOS LEVES

### Hallazgo 5.1 - Falta TypeScript Strict Mode

- **Componente:** Frontend
- **Ruta/Módulo:** `tsconfig.json`
- **Tipo:** Code Smell
- **Principio/Smell:** Type Safety
- **Severidad:** Baja

#### Evidencia

- **Descripción:** TypeScript no está en modo estricto. Permite errores de tipos en runtime.
- **Referencia técnica:** `tsconfig.json` sin `"strict": true`

#### Impacto

- **Mantenibilidad:** Errores de tipos no detectados en compilación
- **Escalabilidad:** Bugs en runtime por tipos incorrectos
- **Riesgo operativo:** Bajo - Errores evitables en producción

#### Recomendación

- **Acción sugerida:** Habilitar `"strict": true` en tsconfig.json
- **Patrón candidato Fase 2:** N/A - Configuración
- **Punto de entrada para refactor Fase 3:** Migración gradual a strict mode

---

### Hallazgo 5.2 - Cobertura de Tests Baja

- **Componente:** Sistema completo
- **Ruta/Módulo:** `order-service/src/test/`, `kitchen-worker/src/test/`, frontend sin tests
- **Tipo:** Code Smell
- **Principio/Smell:** Test Coverage
- **Severidad:** Baja

#### Evidencia

- **Descripción:** Cobertura de tests estimada <60%. Frontend sin tests unitarios.
- **Referencia técnica:** Pocos archivos de test, sin tests E2E

#### Impacto

- **Mantenibilidad:** Baja confianza en cambios. Refactorización riesgosa.
- **Escalabilidad:** Bugs no detectados hasta producción
- **Riesgo operativo:** Bajo - Calidad comprometida

#### Recomendación

- **Acción sugerida:** Aumentar cobertura a 80%+. Agregar tests unitarios, integración y E2E.
- **Patrón candidato Fase 2:** Test Pyramid Strategy
- **Punto de entrada para refactor Fase 3:** TDD (Test-Driven Development) para nuevas features

---

## RESUMEN ESTADÍSTICO

### Por Severidad
- **Críticos:** 10 hallazgos (43%)
- **Moderados:** 11 hallazgos (48%)
- **Leves:** 2 hallazgos (9%)

### Por Tipo
- **SOLID:** 10 hallazgos (43%)
- **Code Smell:** 13 hallazgos (57%)

### Por Componente
- **Backend Java:** 12 hallazgos (52%)
- **Frontend React:** 11 hallazgos (48%)

### Principios Más Vulnerados
1. **SRP (Single Responsibility):** 6 hallazgos
2. **DIP (Dependency Inversion):** 5 hallazgos
3. **Performance/Reliability:** 4 hallazgos
4. **Consistency:** 3 hallazgos
5. **Otros:** 5 hallazgos

---

**Documento generado:** 11 de febrero de 2026  
**Auditor:** Kiro AI  
**Versión:** 1.0
