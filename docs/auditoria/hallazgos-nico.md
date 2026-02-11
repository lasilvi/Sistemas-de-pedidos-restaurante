# Hallazgos de Auditoria - Nico

Auditor: Nico
Fecha: 2026-02-11
Scope revisado: backend (`order-service/`, `kitchen-worker/`), frontend (`src/`), integracion/eventos (`RabbitMQ` + contratos), workflow documental.

## Hallazgo NICO-001

- Componente: Order Service
- Ruta/Modulo: `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`
- Tipo: SOLID / Code Smell
- Principio/Smell: Violacion de SRP + riesgo de N+1 queries en mapeo
- Severidad: Alta

### Evidencia

- Descripcion: `OrderService` mezcla validaciones, persistencia, mapping DTO, lookup de productos y publicacion de eventos en una sola clase. Adicionalmente, al mapear items del pedido consulta `productRepository` por cada item.
- Referencia tecnica:
  - `OrderService.java:84-138` (createOrder con responsabilidades multiples)
  - `OrderService.java:250-284` (mapeo + consulta por item)
  - `OrderService.java:273-275` (lookup por item)

### Impacto

- Mantenibilidad: Cambios funcionales y de integracion se concentran en una clase grande.
- Escalabilidad: Listados de pedidos con muchos items pueden disparar consultas adicionales por item.
- Riesgo operativo: Mayor probabilidad de regresiones al tocar logica no aislada.

### Recomendacion

- Accion sugerida: Separar validacion, construccion de entidades, mapping y publicacion en servicios dedicados. Pre-cargar nombres de productos por lote para evitar consultas repetidas.
- Patron candidato Fase 2 (si aplica): Facade (orquestacion), Strategy (validaciones), Repository optimization.
- Punto de entrada para refactor Fase 3: `OrderService#createOrder`, `OrderService#mapToOrderItemResponse`.

## Hallazgo NICO-002

- Componente: Integracion Order Service -> Kitchen Worker
- Ruta/Modulo: `order-service/src/main/java/com/restaurant/orderservice/service/OrderEventPublisher.java`
- Tipo: SOLID / Code Smell
- Principio/Smell: Manejo de errores que oculta fallos de integracion (consistency gap)
- Severidad: Alta

### Evidencia

- Descripcion: Si falla RabbitMQ, el publisher captura la excepcion y no la propaga; la orden queda persistida pero el evento puede perderse.
- Referencia tecnica:
  - `OrderEventPublisher.java:42-51` (catch y log sin fallback)
  - `OrderService.java:127-135` (persistencia + publish en flujo secuencial)

### Impacto

- Mantenibilidad: Dificulta detectar estados inconsistentes entre servicios.
- Escalabilidad: Bajo carga o degradacion de broker, aumentan ordenes no procesadas por cocina.
- Riesgo operativo: Inconsistencia eventual: orden creada pero no visible/procesada en worker.

### Recomendacion

- Accion sugerida: Implementar outbox pattern o reintentos transaccionales con monitorizacion de eventos pendientes.
- Patron candidato Fase 2 (si aplica): Publisher-Subscriber robusto + Outbox.
- Punto de entrada para refactor Fase 3: `OrderEventPublisher#publishOrderPlacedEvent`.

## Hallazgo NICO-003

- Componente: Kitchen Worker
- Ruta/Modulo: `kitchen-worker/src/main/java/com/restaurant/kitchenworker/listener/OrderEventListener.java`, `kitchen-worker/src/main/java/com/restaurant/kitchenworker/service/OrderProcessingService.java`
- Tipo: SOLID / Code Smell
- Principio/Smell: DIP debilitado por field injection
- Severidad: Media

### Evidencia

- Descripcion: Se usa `@Autowired` sobre campos en lugar de inyeccion por constructor, ocultando dependencias y dificultando pruebas aisladas.
- Referencia tecnica:
  - `OrderEventListener.java:30-31`
  - `OrderProcessingService.java:27-28`

### Impacto

- Mantenibilidad: Menor claridad de dependencias y mayor acoplamiento al contenedor.
- Escalabilidad: Tests y refactors de infraestructura se vuelven mas costosos.
- Riesgo operativo: Mayor posibilidad de errores por dependencias no inicializadas en escenarios no estandar.

### Recomendacion

- Accion sugerida: Migrar a constructor injection con campos `final`.
- Patron candidato Fase 2 (si aplica): Dependency Injection explicita (constructor-based).
- Punto de entrada para refactor Fase 3: constructors de `OrderEventListener` y `OrderProcessingService`.

## Hallazgo NICO-004

- Componente: Integracion de servicios / arquitectura de datos
- Ruta/Modulo: `docker-compose.yml`, `kitchen-worker/src/main/java/com/restaurant/kitchenworker/entity/Order.java`
- Tipo: Code Smell
- Principio/Smell: Acoplamiento rigido entre microservicios por base de datos compartida
- Severidad: Alta

### Evidencia

- Descripcion: Ambos servicios usan la misma DB y misma tabla `orders`, rompiendo aislamiento por servicio.
- Referencia tecnica:
  - `docker-compose.yml:48-50` y `docker-compose.yml:70-72` (mismo DB_URL/credenciales)
  - `kitchen-worker/entity/Order.java:21-23` (`@Table(name = "orders")`)

### Impacto

- Mantenibilidad: Cambios de esquema en un servicio impactan al otro directamente.
- Escalabilidad: Limita independencia de despliegue y evolucion de modelos.
- Riesgo operativo: Mayor riesgo de conflictos de datos y migraciones acopladas.

### Recomendacion

- Accion sugerida: Definir ownership de datos por servicio y sincronizar por eventos/DTOs en lugar de escritura compartida.
- Patron candidato Fase 2 (si aplica): Anti-Corruption Layer / Event-carried state transfer.
- Punto de entrada para refactor Fase 3: limite de persistencia de `kitchen-worker` y contrato de eventos.

## Hallazgo NICO-005

- Componente: Contrato Frontend-Backend (creacion de pedidos)
- Ruta/Modulo: `src/api/contracts.ts`, `src/pages/client/CartPage.tsx`, `order-service/src/main/java/com/restaurant/orderservice/dto/OrderItemRequest.java`
- Tipo: Code Smell
- Principio/Smell: Inconsistencia de tipos en contrato (falta de abstraccion de contrato compartido)
- Severidad: Alta

### Evidencia

- Descripcion: Frontend modela `productId` como `string` y lo envia como string; backend espera `Long`.
- Referencia tecnica:
  - `contracts.ts:15` y `contracts.ts:36` (`productId: string`)
  - `CartPage.tsx:50` (envio directo de `productId`)
  - `OrderItemRequest.java:19` (`Long productId`)

### Impacto

- Mantenibilidad: Contratos divergentes obligan conversiones ad-hoc.
- Escalabilidad: Mayor probabilidad de errores de serializacion al crecer endpoints.
- Riesgo operativo: Posibles respuestas 400 en entorno real (`USE_MOCK=false`).

### Recomendacion

- Accion sugerida: Unificar tipo de `productId` en contratos compartidos (preferiblemente numerico) y validar en frontend antes de enviar.
- Patron candidato Fase 2 (si aplica): Adapter para contrato API si se mantiene tipado interno distinto.
- Punto de entrada para refactor Fase 3: `src/api/contracts.ts`, `src/pages/client/CartPage.tsx`.

## Hallazgo NICO-006

- Componente: Seguridad de flujo cocina
- Ruta/Modulo: `src/pages/kitchen/KitchenLoginPage.tsx`, `src/api/env.ts`, `src/api/http.ts`, `order-service/src/main/java/com/restaurant/orderservice/controller/OrderController.java`
- Tipo: Code Smell
- Principio/Smell: Seguridad incompleta (autenticacion simulada sin enforcement backend)
- Severidad: Alta

### Evidencia

- Descripcion: Frontend define token/PIN y header, pero login cocina entra directo y backend no valida token en endpoints sensibles.
- Referencia tecnica:
  - `KitchenLoginPage.tsx:7-9` (acceso directo)
  - `env.ts:17-19` (header/PIN/token fijo)
  - `http.ts:27-30` (envio header)
  - `OrderController.java` (sin validacion de header/token en endpoints)

### Impacto

- Mantenibilidad: Comportamiento de seguridad no centralizado ni verificable.
- Escalabilidad: Riesgo creciente al abrir entorno demo/publico.
- Riesgo operativo: Actualizaciones de estado potencialmente invocables sin control real.

### Recomendacion

- Accion sugerida: Implementar guardas backend (filtro/interceptor/security config) para operaciones de cocina y alinear login frontend con token real.
- Patron candidato Fase 2 (si aplica): Facade de autenticacion + middleware chain.
- Punto de entrada para refactor Fase 3: capa de seguridad en `order-service` y flujo `KitchenLoginPage`.

## Hallazgo NICO-007

- Componente: Frontend Kitchen Board
- Ruta/Modulo: `src/pages/kitchen/KitchenBoardPage.tsx`
- Tipo: SOLID / Code Smell
- Principio/Smell: Violacion de SRP en componente UI (fetching + polling + domain logic + render)
- Severidad: Media

### Evidencia

- Descripcion: Un unico componente concentra polling, gestion de errores, agrupacion por estado, patch de estado y renderizado complejo de filas/items.
- Referencia tecnica:
  - `KitchenBoardPage.tsx:14-52` (estado + polling)
  - `KitchenBoardPage.tsx:64-72` (agrupacion dominio)
  - `KitchenBoardPage.tsx:152-167` (mutacion de estado y refetch)
  - `KitchenBoardPage.tsx:183-248` (subcomponente acoplado en mismo archivo)

### Impacto

- Mantenibilidad: Cambios de UX o de integracion afectan un archivo grande.
- Escalabilidad: Dificulta pruebas unitarias finas por responsabilidad cruzada.
- Riesgo operativo: Mayor chance de regresion en refresco/estado al introducir nuevas reglas.

### Recomendacion

- Accion sugerida: Extraer hook de polling (`useKitchenOrders`) y separar presentacionales (`OrderRow`, `OrderGroup`).
- Patron candidato Fase 2 (si aplica): Observer (estado reactivo), Strategy (transiciones).
- Punto de entrada para refactor Fase 3: `KitchenBoardPage.tsx`.

## Hallazgo NICO-008

- Componente: Workflow documental
- Ruta/Modulo: `AI_WORKFLOW.md`
- Tipo: Code Smell
- Principio/Smell: Drift documental / inconsistencia operativa
- Severidad: Baja

### Evidencia

- Descripcion: El documento muestra comandos legacy (`/openspec ...`) y texto con codificacion degradada (mojibake), lo que introduce ambiguedad operativa.
- Referencia tecnica:
  - `AI_WORKFLOW.md:27-35` (comandos legacy)
  - `AI_WORKFLOW.md:2-4`, `AI_WORKFLOW.md:63-71` (codificacion degradada)

### Impacto

- Mantenibilidad: Onboarding mas lento y errores de ejecucion por comandos ambiguos.
- Escalabilidad: Equipos paralelos pueden divergir de un flujo unico.
- Riesgo operativo: Errores de proceso al ejecutar fases/skills.

### Recomendacion

- Accion sugerida: Normalizar codificacion UTF-8 y actualizar comandos al estandar acordado por el equipo para esta auditoria.
- Patron candidato Fase 2 (si aplica): N/A (proceso).
- Punto de entrada para refactor Fase 3: `AI_WORKFLOW.md`.
