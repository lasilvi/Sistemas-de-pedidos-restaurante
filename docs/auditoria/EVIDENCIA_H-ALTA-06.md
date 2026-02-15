# Evidencia de Implementacion - H-ALTA-06

**Fecha:** 2026-02-13
**Hallazgo:** H-ALTA-06 - Ausencia de capas arquitectonicas claras entre dominio/aplicacion/infraestructura
**Rama:** `feature/auditoria-fase-1-ejecucion`
**Estado:** COMPLETADO

---

## Defecto

El flujo de pedidos mezclaba reglas de negocio con detalles de infraestructura, dificultando evolucion y pruebas aisladas.

## Resumen de solucion

Se aplico enfoque `Ports and Adapters` para el flujo de eventos de pedido:
- Dominio: `OrderPlacedDomainEvent`.
- Aplicacion: `OrderPlacedEventPublisherPort`.
- Infraestructura: adaptador Rabbit y mapper de mensaje.

## Commits relacionados

- `81cc2ad` `refactor(auditoria): desacoplar DB por servicio y robustecer contrato de eventos`

## Archivos principales modificados

- `order-service/src/main/java/com/restaurant/orderservice/domain/event/OrderPlacedDomainEvent.java`
- `order-service/src/main/java/com/restaurant/orderservice/application/port/out/OrderPlacedEventPublisherPort.java`
- `order-service/src/main/java/com/restaurant/orderservice/infrastructure/messaging/RabbitOrderPlacedEventPublisher.java`
- `order-service/src/main/java/com/restaurant/orderservice/infrastructure/messaging/OrderPlacedEventMessageMapper.java`
- `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`
- `order-service/src/main/java/com/restaurant/orderservice/service/command/PublishOrderPlacedEventCommand.java`

## Evidencia funcional

- `OrderService` delega publicacion al puerto de aplicacion.
- El comando usa abstraccion de salida y no depende de clase concreta de transporte.
- Ajustes de serializacion/headers se encapsulan en infraestructura.

## Estado

- Mitigacion implementada y cubierta por pruebas de unidad.
- Referenciada en `AUDITORIA.md` como hallazgo mitigado.
- Lista para merge a `develop`.
