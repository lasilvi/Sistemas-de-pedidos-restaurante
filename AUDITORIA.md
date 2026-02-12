# AUDITORIA Fase 1 - Consolidado Final de Auditoria

Estado: CONSOLIDADO (3 de 3 aportes integrados)

## Baseline y contexto

- Snapshot base post-MVP: `51b8f5d` (`audit: snapshot post-mvp`)
- Rama de trabajo actual: `feature/auditoria-fase-1-diagnostico`
- Alcance oficial: `docs/auditoria/ALCANCE_FASE1.md` (solo lectura)
- Fuentes individuales:
  - `docs/auditoria/hallazgos-nico.md`
  - `docs/auditoria/hallazgos-companero-1.md` (Raul)
  - `docs/auditoria/hallazgos-luis.md` (Luis)

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
- Aciertos identificados (fragmentos ya existentes):
  - `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java:85`: `createOrder` ya opera con `@Transactional`, base correcta para rollback transaccional.
  - `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java:133`: el evento se construye desde la entidad persistida, evitando payload incompleto.
  - `order-service/src/main/java/com/restaurant/orderservice/service/OrderEventPublisher.java:42`: la publicacion ya estaba encapsulada en un servicio dedicado, facilitando introducir un comando sin romper el controlador.

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

#### H-ALTA-06 - Ausencia de capas arquitectonicas claras entre dominio/aplicacion/infraestructura

- Dominio: Arquitectura global
- Tipo: Code Smell
- Descripcion consolidada: La estructura actual mezcla logica de negocio, orquestacion y detalles de infraestructura sin fronteras explicitas por capa.
- Evidencia:
  - `docs/auditoria/hallazgos-companero-1.md` (secciones 4.1 y 4.2)
  - `docs/auditoria/hallazgos-luis.md` (hallazgos H-01, H-04, H-07)
- Impacto: incrementa acoplamiento transversal y costo de evolucion.
- Fuentes: `docs/auditoria/hallazgos-companero-1.md`, `docs/auditoria/hallazgos-luis.md`

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

#### H-MEDIA-03 - Contrato de evento y modelo de integracion con baja resiliencia

- Dominio: Integracion/Eventos
- Tipo: SOLID + Code Smell
- Descripcion consolidada: La publicacion/consumo de eventos carece de una estrategia robusta de recuperacion y versionado de contrato.
- Evidencia:
  - `docs/auditoria/hallazgos-luis.md` (H-04, H-06)
  - `docs/auditoria/hallazgos-companero-1.md` (1.5, 2.3)
- Impacto: riesgo de inconsistencia entre servicios cuando falla el broker o evoluciona el payload.
- Fuentes: `docs/auditoria/hallazgos-luis.md`, `docs/auditoria/hallazgos-companero-1.md`

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

#### H-BAJA-02 - Brechas de calidad no funcional (observabilidad, cobertura, hardening)

- Dominio: Calidad transversal
- Tipo: Code Smell
- Descripcion consolidada: Se reportan gaps en observabilidad centralizada, cobertura de tests y endurecimiento (ej. rate limiting / control de abuso).
- Evidencia:
  - `docs/auditoria/hallazgos-companero-1.md` (4.4, 4.6, 5.2)
  - `docs/auditoria/hallazgos-luis.md` (hallazgos de calidad no funcional)
- Impacto: deteccion tardia de incidentes y mayor riesgo operativo en crecimiento.
- Fuentes: `docs/auditoria/hallazgos-companero-1.md`, `docs/auditoria/hallazgos-luis.md`

## Deduplicacion aplicada

- Se fusionaron hallazgos equivalentes en estas lineas maestras:
  - `OrderService` sobredimensionado (SRP/God class/N+1) reportado por Nico, Raul y Luis.
  - Riesgo de consistencia por eventos (publisher + worker) reportado por Raul y Luis, y parcialmente por Nico.
  - Acoplamiento arquitectonico y ausencia de fronteras limpias reportado por Raul y Luis.
  - Debilidades de seguridad de cocina reportadas por Nico y validadas por criterios de Luis.

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

## Cierre de Fase 1

- Consolidacion de hallazgos completada con aporte de 3 auditores.
- Reporte listo para transicion a Fase 2 (patrones) y Fase 3 (refactor dirigido).
