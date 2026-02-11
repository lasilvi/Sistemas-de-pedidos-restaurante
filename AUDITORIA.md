# AUDITORIA Fase 1 - Consolidado (Borrador Colaborativo)

Estado: EN CONSOLIDACION (faltan aportes de Companero 1 y Companero 2)

## Baseline y contexto

- Snapshot base post-MVP: `51b8f5d` (`audit: snapshot post-mvp`)
- Rama de trabajo actual: `feature/auditoria-fase-1-ejecucion`
- Alcance oficial: `docs/auditoria/ALCANCE_FASE1.md` (solo lectura)
- Fuentes individuales:
  - `docs/auditoria/hallazgos-nico.md`
  - `docs/auditoria/hallazgos-companero-1.md` (pendiente)
  - `docs/auditoria/hallazgos-companero-2.md` (pendiente)

## Reglas de consolidacion aplicadas

- Se consolidan por severidad y dominio tecnico, no por autor.
- Hallazgos duplicados por misma causa raiz se fusionan en una sola entrada maestra.
- Cada hallazgo incluye trazabilidad a su archivo fuente.

## Hallazgos consolidados (actuales)

### Severidad Alta

#### H-ALTA-01 - OrderService con responsabilidades mezcladas y riesgo N+1

- Dominio: Backend
- Tipo: SOLID (SRP) + Code Smell
- Descripcion consolidada: La clase `OrderService` concentra validacion, persistencia, mapeo y publicacion de eventos; adicionalmente consulta producto por cada item durante el mapeo de respuesta.
- Evidencia:
  - `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java:84-138`
  - `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java:250-284`
- Impacto: eleva costo de cambio y puede degradar rendimiento en consultas de pedidos con muchos items.
- Fuentes: `docs/auditoria/hallazgos-nico.md#hallazgo-nico-001`

#### H-ALTA-02 - Gap de consistencia entre persistencia y publicacion de eventos

- Dominio: Integracion/Eventos
- Tipo: Code Smell
- Descripcion consolidada: El publisher captura excepciones de RabbitMQ y no corta el flujo, dejando orden persistida pero potencialmente no publicada.
- Evidencia:
  - `order-service/src/main/java/com/restaurant/orderservice/service/OrderEventPublisher.java:42-51`
  - `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java:127-135`
- Impacto: inconsistencias entre Order Service y Kitchen Worker ante fallos de broker.
- Fuentes: `docs/auditoria/hallazgos-nico.md#hallazgo-nico-002`

#### H-ALTA-03 - Microservicios acoplados por base de datos/tabla compartida

- Dominio: Integracion/Arquitectura
- Tipo: Code Smell
- Descripcion consolidada: Order Service y Kitchen Worker usan misma DB y tabla `orders`, debilitando independencia de servicios.
- Evidencia:
  - `docker-compose.yml:48-50`
  - `docker-compose.yml:70-72`
  - `kitchen-worker/src/main/java/com/restaurant/kitchenworker/entity/Order.java:21-23`
- Impacto: migraciones acopladas, riesgo de regresiones cruzadas, menor autonomia de despliegue.
- Fuentes: `docs/auditoria/hallazgos-nico.md#hallazgo-nico-004`

#### H-ALTA-04 - Contrato tipo `productId` inconsistente entre frontend y backend

- Dominio: Frontend + API Contract
- Tipo: Code Smell
- Descripcion consolidada: Frontend usa `productId` string y backend espera Long.
- Evidencia:
  - `src/api/contracts.ts:15`
  - `src/api/contracts.ts:36`
  - `src/pages/client/CartPage.tsx:50`
  - `order-service/src/main/java/com/restaurant/orderservice/dto/OrderItemRequest.java:19`
- Impacto: potenciales errores 400 en entorno real y deuda de conversiones ad-hoc.
- Fuentes: `docs/auditoria/hallazgos-nico.md#hallazgo-nico-005`

#### H-ALTA-05 - Seguridad de cocina no aplicada de extremo a extremo

- Dominio: Frontend + Backend Security
- Tipo: Code Smell
- Descripcion consolidada: Existe mecanismo de token/header en frontend, pero login de cocina entra directo y backend no valida cabecera de autorizacion en endpoints criticos.
- Evidencia:
  - `src/pages/kitchen/KitchenLoginPage.tsx:7-9`
  - `src/api/env.ts:17-19`
  - `src/api/http.ts:27-30`
  - `order-service/src/main/java/com/restaurant/orderservice/controller/OrderController.java`
- Impacto: operaciones de cocina sin control de acceso efectivo.
- Fuentes: `docs/auditoria/hallazgos-nico.md#hallazgo-nico-006`

### Severidad Media

#### H-MEDIA-01 - Inyeccion por campo en kitchen-worker (DIP debilitado)

- Dominio: Backend
- Tipo: SOLID (DIP)
- Descripcion consolidada: Dependencias de listener y servicio se inyectan por campo con `@Autowired` en lugar de constructor.
- Evidencia:
  - `kitchen-worker/src/main/java/com/restaurant/kitchenworker/listener/OrderEventListener.java:30-31`
  - `kitchen-worker/src/main/java/com/restaurant/kitchenworker/service/OrderProcessingService.java:27-28`
- Impacto: menor testabilidad y mayor acoplamiento al contenedor.
- Fuentes: `docs/auditoria/hallazgos-nico.md#hallazgo-nico-003`

#### H-MEDIA-02 - KitchenBoardPage concentra demasiadas responsabilidades

- Dominio: Frontend
- Tipo: SOLID (SRP) + Code Smell
- Descripcion consolidada: Un componente maneja polling, fetch, agrupacion, mutacion y render extenso.
- Evidencia:
  - `src/pages/kitchen/KitchenBoardPage.tsx:14-52`
  - `src/pages/kitchen/KitchenBoardPage.tsx:64-72`
  - `src/pages/kitchen/KitchenBoardPage.tsx:152-167`
  - `src/pages/kitchen/KitchenBoardPage.tsx:183-248`
- Impacto: fragilidad ante cambios y mayor riesgo de regresion UI/estado.
- Fuentes: `docs/auditoria/hallazgos-nico.md#hallazgo-nico-007`

### Severidad Baja

#### H-BAJA-01 - Drift documental en workflow de IA/OpenSpec

- Dominio: Workflow/Documentacion
- Tipo: Code Smell
- Descripcion consolidada: El documento operativo tiene comandos desalineados y codificacion degradada.
- Evidencia:
  - `AI_WORKFLOW.md:27-35`
  - `AI_WORKFLOW.md:2-4`
- Impacto: ruido operativo y errores de uso para nuevos colaboradores.
- Fuentes: `docs/auditoria/hallazgos-nico.md#hallazgo-nico-008`

## Deduplicacion aplicada

- No se detectaron duplicados reales entre fuentes, porque solo `hallazgos-nico.md` contiene hallazgos actualmente.
- Duplicados potenciales se reevaluaran cuando se incorporen los hallazgos de los otros dos auditores.

## Mapeo hacia Fase 2 (patrones candidatos)

- H-ALTA-01 -> Facade (orquestacion de creacion de orden) + Strategy (validaciones de entrada).
- H-ALTA-02 -> Outbox pattern + Retry policy explicita.
- H-ALTA-03 -> Anti-Corruption Layer / Event-carried state transfer.
- H-MEDIA-02 -> Observer/Strategy para transiciones y polling desacoplado.

## Mapeo hacia Fase 3 (entry points de refactor)

- `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`
- `order-service/src/main/java/com/restaurant/orderservice/service/OrderEventPublisher.java`
- `kitchen-worker/src/main/java/com/restaurant/kitchenworker/listener/OrderEventListener.java`
- `kitchen-worker/src/main/java/com/restaurant/kitchenworker/service/OrderProcessingService.java`
- `src/pages/kitchen/KitchenBoardPage.tsx`
- `src/api/contracts.ts`
- `src/pages/client/CartPage.tsx`
- `AI_WORKFLOW.md`

## Pendientes para cierre final del reporte

- Integrar hallazgos de `hallazgos-companero-1.md`.
- Integrar hallazgos de `hallazgos-companero-2.md`.
- Ejecutar revision final de equipo (3/3) y congelar version final de Fase 1.
