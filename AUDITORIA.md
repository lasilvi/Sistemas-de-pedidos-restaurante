# AUDITORIA

## Hallazgos

### H-ALTA-02 - Gap de consistencia entre persistencia y publicacion de eventos

- Dominio: Integracion/Eventos
- Problema: la orden podia persistirse aun cuando la publicacion del evento fallara.
- Impacto: inconsistencia entre estado de base de datos y procesamiento asincrono.

#### Aciertos identificados

- `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`: `createOrder` ya usa `@Transactional`.
- `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`: el `OrderPlacedEvent` se construye desde la entidad persistida.
- `order-service/src/main/java/com/restaurant/orderservice/service/OrderEventPublisher.java`: la publicacion estaba encapsulada en un servicio dedicado.
