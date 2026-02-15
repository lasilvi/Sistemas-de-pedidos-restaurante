# Evidencia de Implementacion - H-MEDIA-03

**Fecha:** 2026-02-13
**Hallazgo:** H-MEDIA-03 - Contrato de evento y modelo de integracion con baja resiliencia
**Rama:** `feature/auditoria-fase-1-ejecucion`
**Estado:** COMPLETADO

---

## Defecto

El contrato `order.placed` no tenia versionado formal ni validacion robusta en consumo, con riesgo de inconsistencias ante evolucion del payload.

## Resumen de solucion

Se reforzo el contrato de integracion:
- Productor publica envelope versionado con metadata (`eventId`, `eventType`, `eventVersion`, `occurredAt`).
- Consumidor valida contrato/version antes de procesar.
- Eventos invalidos o version no soportada se rechazan sin requeue para DLQ.

## Commits relacionados

- `81cc2ad` `refactor(auditoria): desacoplar DB por servicio y robustecer contrato de eventos`

## Archivos principales modificados

- `order-service/src/main/java/com/restaurant/orderservice/infrastructure/messaging/OrderPlacedEventMessage.java`
- `order-service/src/main/java/com/restaurant/orderservice/infrastructure/messaging/RabbitOrderPlacedEventPublisher.java`
- `kitchen-worker/src/main/java/com/restaurant/kitchenworker/event/OrderPlacedEvent.java`
- `kitchen-worker/src/main/java/com/restaurant/kitchenworker/event/OrderPlacedEventValidator.java`
- `kitchen-worker/src/main/java/com/restaurant/kitchenworker/listener/OrderEventListener.java`
- `kitchen-worker/src/main/java/com/restaurant/kitchenworker/exception/UnsupportedEventVersionException.java`
- `kitchen-worker/src/main/java/com/restaurant/kitchenworker/exception/InvalidEventContractException.java`

## Evidencia funcional

- El listener valida contrato antes de invocar `OrderProcessingService`.
- Version no soportada produce `AmqpRejectAndDontRequeueException`.
- Se mantiene compatibilidad controlada durante transicion con metodos `resolve*`.
- Hay pruebas unitarias para validacion y rechazo de contrato/version.

## Estado

- Mitigacion implementada y verificada en tests.
- Referenciada en `AUDITORIA.md`.
- Lista para merge a `develop`.
