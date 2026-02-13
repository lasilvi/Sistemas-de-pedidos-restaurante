# Evidencia de Implementacion - H-ALTA-06

## Defecto
`H-ALTA-06 - Ausencia de capas arquitectonicas claras entre dominio/aplicacion/infraestructura`

## Resumen de solucion
Se aplico una refactorizacion guiada por `Ports and Adapters` en el flujo de eventos de pedido:
- Dominio: `OrderPlacedDomainEvent`.
- Aplicacion: puerto de salida `OrderPlacedEventPublisherPort`.
- Infraestructura: adaptador Rabbit `RabbitOrderPlacedEventPublisher` y mapeador de contrato.

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
- `OrderService` ya no conoce detalles de transporte RabbitMQ; depende del puerto de aplicacion.
- El comando `PublishOrderPlacedEventCommand` invoca el puerto y no una clase concreta de infraestructura.
- Cambios de serializacion/headers quedan encapsulados en el adaptador Rabbit sin contaminar dominio.
