# AUDITORIA_LUIS

## Hallazgo H-01

- Componente: Order Service
- Ruta/Modulo: `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`
- Tipo: SOLID
- Principio/Smell: SRP (Single Responsibility Principle)
- Severidad: Alta

### Evidencia

- Descripcion: `OrderService` concentra validacion, reglas de negocio, persistencia, mapeo de DTO y publicacion de eventos.
- Referencia tecnica: `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java:33`

### Impacto

- Mantenibilidad: Cada cambio funcional toca una clase critica con alto acoplamiento.
- Escalabilidad: Reduce capacidad de paralelizar trabajo entre equipos y aumenta tiempo de entrega.
- Riesgo operativo: Alta probabilidad de regresion por efectos cruzados.

### Recomendacion

- Accion sugerida: Separar casos de uso (`CreateOrder`, `GetOrders`, `UpdateStatus`) y extraer mapper/validator.
- Patron candidato Fase 2 (si aplica): Application Service + Domain Service.
- Punto de entrada para refactor Fase 3: `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`

## Hallazgo H-02

- Componente: Validacion de entrada de ordenes
- Ruta/Modulo: `order-service/src/main/java/com/restaurant/orderservice/dto/CreateOrderRequest.java`, `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`
- Tipo: SOLID
- Principio/Smell: SRP / Duplicated validation logic
- Severidad: Media

### Evidencia

- Descripcion: Se valida `tableId` e `items` tanto con anotaciones Bean Validation como con `if` manual en servicio.
- Referencia tecnica: `order-service/src/main/java/com/restaurant/orderservice/dto/CreateOrderRequest.java:22`, `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java:88`

### Impacto

- Mantenibilidad: Regla duplicada en multiples capas.
- Escalabilidad: Cambios de contrato requieren sincronizacion manual y mayor tiempo de evolucion.
- Riesgo operativo: Inconsistencias de validacion entre endpoints y servicios.

### Recomendacion

- Accion sugerida: Consolidar validacion en DTO + validador dedicado y remover duplicados.
- Patron candidato Fase 2 (si aplica): Validation pipeline.
- Punto de entrada para refactor Fase 3: `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`

## Hallazgo H-03

- Componente: Consulta y mapeo de pedidos
- Ruta/Modulo: `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`
- Tipo: Code Smell
- Principio/Smell: N+1 Query
- Severidad: Alta

### Evidencia

- Descripcion: Por cada `OrderItem` se ejecuta `productRepository.findById(...)` dentro del mapper de respuesta.
- Referencia tecnica: `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java:271`

### Impacto

- Mantenibilidad: El rendimiento depende de detalles de mapeo no evidentes.
- Escalabilidad: Latencia y carga de BD crecen en forma no lineal con el numero de items.
- Riesgo operativo: Degradacion de performance en horarios pico.

### Recomendacion

- Accion sugerida: Resolver nombres de producto en lote (consulta `IN`) o projection con join.
- Patron candidato Fase 2 (si aplica): Repository projection / query optimization.
- Punto de entrada para refactor Fase 3: `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`

## Hallazgo H-04

- Componente: Publicacion de eventos
- Ruta/Modulo: `order-service/src/main/java/com/restaurant/orderservice/service/OrderEventPublisher.java`
- Tipo: SOLID
- Principio/Smell: DIP / Swallowed exception
- Severidad: Alta

### Evidencia

- Descripcion: Se captura `Exception` y no se propaga ni se persiste para reproceso durable.
- Referencia tecnica: `order-service/src/main/java/com/restaurant/orderservice/service/OrderEventPublisher.java:47`

### Impacto

- Mantenibilidad: Oculta fallas reales de mensajeria.
- Escalabilidad: Riesgo de inconsistencia eventual entre servicios al subir volumen de eventos.
- Riesgo operativo: Perdida de eventos sin trazabilidad fuerte.

### Recomendacion

- Accion sugerida: Implementar Outbox + reintentos controlados y alertado.
- Patron candidato Fase 2 (si aplica): Transactional Outbox.
- Punto de entrada para refactor Fase 3: `order-service/src/main/java/com/restaurant/orderservice/service/OrderEventPublisher.java`

## Hallazgo H-05

- Componente: Modelo de datos y migraciones
- Ruta/Modulo: `bd/001_create_tables.sql`, `bd/002_constraints_indexes.sql`, `order-service/src/main/resources/db/migration/`
- Tipo: SOLID
- Principio/Smell: OCP + SRP / Schema drift
- Severidad: Alta

### Evidencia

- Descripcion: Diferencias de nombres de tablas (`product` vs `products`, `order_item` vs `order_items`) y de estados permitidos.
- Referencia tecnica: `bd/001_create_tables.sql:1`, `bd/002_constraints_indexes.sql:2`, `order-service/src/main/resources/db/migration/V1__create_products_table.sql:1`

### Impacto

- Mantenibilidad: Dos fuentes de verdad para el mismo modelo.
- Escalabilidad: Ambientes no reproducibles y mayor friccion en despliegues.
- Riesgo operativo: Fallos por incompatibilidad de esquema al promover cambios.

### Recomendacion

- Accion sugerida: Definir Flyway como unica fuente de verdad y alinear scripts auxiliares.
- Patron candidato Fase 2 (si aplica): Database migration governance.
- Punto de entrada para refactor Fase 3: `order-service/src/main/resources/db/migration/`

## Hallazgo H-06

- Componente: Contrato de evento entre microservicios
- Ruta/Modulo: `order-service/src/main/java/com/restaurant/orderservice/event/OrderPlacedEvent.java`, `kitchen-worker/src/main/java/com/restaurant/kitchenworker/event/OrderPlacedEvent.java`
- Tipo: SOLID
- Principio/Smell: OCP / Duplicated contract
- Severidad: Media

### Evidencia

- Descripcion: El DTO de evento esta duplicado en dos servicios, sin versionado compartido.
- Referencia tecnica: `order-service/src/main/java/com/restaurant/orderservice/event/OrderPlacedEvent.java:19`, `kitchen-worker/src/main/java/com/restaurant/kitchenworker/event/OrderPlacedEvent.java:19`

### Impacto

- Mantenibilidad: Cambios deben duplicarse manualmente en ambos lados.
- Escalabilidad: Releases independientes pueden romper compatibilidad de mensajes.
- Riesgo operativo: Errores de deserializacion y eventos rechazados.

### Recomendacion

- Accion sugerida: Versionar schema de eventos y/o extraer contrato compartido.
- Patron candidato Fase 2 (si aplica): Schema Registry / Shared contract package.
- Punto de entrada para refactor Fase 3: `order-service/src/main/java/com/restaurant/orderservice/event/`, `kitchen-worker/src/main/java/com/restaurant/kitchenworker/event/`

## Hallazgo H-07

- Componente: Kitchen Worker
- Ruta/Modulo: `kitchen-worker/src/main/java/com/restaurant/kitchenworker/service/OrderProcessingService.java`
- Tipo: SOLID
- Principio/Smell: DIP (field injection)
- Severidad: Media

### Evidencia

- Descripcion: Uso de `@Autowired` en campo en lugar de inyeccion por constructor.
- Referencia tecnica: `kitchen-worker/src/main/java/com/restaurant/kitchenworker/service/OrderProcessingService.java:27`

### Impacto

- Mantenibilidad: Dependencias menos explicitas y mas dificiles de testear.
- Escalabilidad: Mayor fragilidad del wiring al crecer componentes.
- Riesgo operativo: Errores de inicializacion mas dificiles de detectar tempranamente.

### Recomendacion

- Accion sugerida: Migrar a constructor injection en todos los beans del worker.
- Patron candidato Fase 2 (si aplica): Dependency inversion via constructor injection.
- Punto de entrada para refactor Fase 3: `kitchen-worker/src/main/java/com/restaurant/kitchenworker/service/OrderProcessingService.java`

## Hallazgo H-08

- Componente: Entidades JPA
- Ruta/Modulo: `order-service/src/main/java/com/restaurant/orderservice/entity/Order.java`, `order-service/src/main/java/com/restaurant/orderservice/entity/OrderItem.java`
- Tipo: Code Smell
- Principio/Smell: `@Data` en entidades con relaciones
- Severidad: Media

### Evidencia

- Descripcion: `@Data` genera `equals/hashCode/toString` genericos en entidades con relaciones bidireccionales.
- Referencia tecnica: `order-service/src/main/java/com/restaurant/orderservice/entity/Order.java:25`, `order-service/src/main/java/com/restaurant/orderservice/entity/OrderItem.java:18`

### Impacto

- Mantenibilidad: Comportamiento implicito dificil de controlar.
- Escalabilidad: Sobrecostos en memoria/CPU al crecer cardinalidad de relaciones.
- Riesgo operativo: Bugs sutiles en colecciones, logs o serializacion.

### Recomendacion

- Accion sugerida: Reemplazar `@Data` por getters/setters + `equals/hashCode` controlados.
- Patron candidato Fase 2 (si aplica): Rich domain entity with explicit identity semantics.
- Punto de entrada para refactor Fase 3: `order-service/src/main/java/com/restaurant/orderservice/entity/`

## Hallazgo H-09

- Componente: Contrato API Front-Back
- Ruta/Modulo: `src/api/contracts.ts`, `src/api/orders.ts`, `order-service/src/main/java/com/restaurant/orderservice/dto/UpdateStatusRequest.java`
- Tipo: SOLID
- Principio/Smell: ISP + DIP / Contract drift
- Severidad: Alta

### Evidencia

- Descripcion: Frontend envia `newStatus` y `status` a la vez; ademas tipa `productId` como `string` mientras backend usa `Long`.
- Referencia tecnica: `src/api/orders.ts:45`, `src/api/contracts.ts:15`, `order-service/src/main/java/com/restaurant/orderservice/dto/UpdateStatusRequest.java:19`

### Impacto

- Mantenibilidad: Acuerdos de contrato no son explicitos ni unicos.
- Escalabilidad: Mayor costo de coordinacion entre equipos y ramas paralelas.
- Riesgo operativo: Errores de integracion por desalineacion de tipos/payload.

### Recomendacion

- Accion sugerida: Definir contrato versionado y tipado comun (OpenAPI + codegen o paquete compartido).
- Patron candidato Fase 2 (si aplica): Contract-first API.
- Punto de entrada para refactor Fase 3: `src/api/`, `order-service/src/main/java/com/restaurant/orderservice/dto/`

## Hallazgo H-10

- Componente: UI de cocina
- Ruta/Modulo: `src/pages/kitchen/KitchenBoardPage.tsx`
- Tipo: SOLID
- Principio/Smell: SRP / Large component
- Severidad: Media

### Evidencia

- Descripcion: El componente mezcla render, estado asincrono, control de concurrencia, llamadas API y reglas de transicion.
- Referencia tecnica: `src/pages/kitchen/KitchenBoardPage.tsx:34`, `src/pages/kitchen/KitchenBoardPage.tsx:174`

### Impacto

- Mantenibilidad: Alto costo cognitivo para cambios y debugging.
- Escalabilidad: Dificulta agregar nuevas reglas de flujo o tiempo real (websocket/polling robusto).
- Riesgo operativo: Mayor probabilidad de regressiones funcionales en UI.

### Recomendacion

- Accion sugerida: Extraer hooks de datos, capa de acciones de estado y componentes presentacionales.
- Patron candidato Fase 2 (si aplica): Container/Presenter + custom hooks.
- Punto de entrada para refactor Fase 3: `src/pages/kitchen/KitchenBoardPage.tsx`
