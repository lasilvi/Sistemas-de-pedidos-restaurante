# Evidencia de Implementacion - H-MEDIA-03

## Defecto
`H-MEDIA-03 - Contrato de evento y modelo de integracion con baja resiliencia`

## Resumen de solucion
Se reforzo el contrato `order.placed` con versionado y validacion de consumidor:
- Productor publica envelope con metadata (`eventId`, `eventType`, `eventVersion`, `occurredAt`) y payload.
- Consumidor valida version/contrato antes de procesar.
- Version no soportada o contrato invalido se rechaza sin requeue para envio a DLQ.

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
- El listener ejecuta validacion de contrato antes de `OrderProcessingService`.
- Para version no soportada, el listener lanza `AmqpRejectAndDontRequeueException` y evita procesamiento parcial.
- El consumidor mantiene compatibilidad controlada en transicion mediante `resolveOrderId`, `resolveTableId`, `resolveCreatedAt`.
- Se agregaron pruebas unitarias para validacion de contrato y rechazo por version.
