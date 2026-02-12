# Evidencia de Implementacion - H-ALTA-02

## Defecto
`H-ALTA-02 - Gap de consistencia entre persistencia y publicacion de eventos`

## Resumen de solucion
Se implemento un **Command Pattern (comportamiento)** para encapsular la publicacion de eventos y forzar propagacion de errores de broker, evitando persistencia silenciosa sin evento publicado.

## Commits relacionados
- `82beb21` `refactor: implementado patrón Command para resolver gap de consistencia en OrderService`
- `d834df8` `test: agregado coverage para comando de publicación y manejo de falla de broker`
- `8f5e01e` `docs: documentados aciertos existentes para H-ALTA-02 en AUDITORIA`

## Archivos principales modificados
- `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`
- `order-service/src/main/java/com/restaurant/orderservice/service/OrderEventPublisher.java`
- `order-service/src/main/java/com/restaurant/orderservice/exception/EventPublicationException.java`
- `order-service/src/main/java/com/restaurant/orderservice/service/command/OrderCommand.java`
- `order-service/src/main/java/com/restaurant/orderservice/service/command/OrderCommandExecutor.java`
- `order-service/src/main/java/com/restaurant/orderservice/service/command/PublishOrderPlacedEventCommand.java`
- `order-service/src/main/java/com/restaurant/orderservice/exception/GlobalExceptionHandler.java`
- `order-service/src/test/java/com/restaurant/orderservice/service/OrderServiceTest.java`
- `order-service/src/test/java/com/restaurant/orderservice/service/OrderEventPublisherTest.java`
- `order-service/src/test/java/com/restaurant/orderservice/service/command/PublishOrderPlacedEventCommandTest.java`
- `order-service/src/test/java/com/restaurant/orderservice/exception/GlobalExceptionHandlerTest.java`
- `AUDITORIA.md`

## Evidencia funcional
- La publicacion fallida en `OrderEventPublisher` ya no se ignora; ahora lanza `EventPublicationException`.
- `OrderService#createOrder` delega en comando (`OrderCommandExecutor`) y ante error propaga excepcion para rollback transaccional.
- `GlobalExceptionHandler` responde `503` cuando hay falla de broker.
- Se agrego cobertura unitaria para:
  - Falla de publicacion de evento.
  - Delegacion del comando concreto.
  - Mapeo de excepcion a HTTP.

## Aciertos documentados
Se registro en `AUDITORIA.md`:
- `@Transactional` preexistente en `createOrder`.
- Construccion de evento desde entidad persistida.
- Publisher previamente encapsulado en servicio dedicado.
