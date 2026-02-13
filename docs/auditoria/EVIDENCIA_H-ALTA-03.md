# Evidencia de Implementacion - H-ALTA-03

## Defecto
`H-ALTA-03 - Microservicios acoplados por base de datos/tabla compartida`

## Resumen de solucion
Se aplico `database-per-service` para desacoplar `order-service` y `kitchen-worker` a nivel de persistencia:
- `order-service` mantiene `restaurant_db`.
- `kitchen-worker` migra a `kitchen_db` y tabla propia `kitchen_orders`.

## Commits relacionados
- `81cc2ad` `refactor(auditoria): desacoplar DB por servicio y robustecer contrato de eventos`

## Archivos principales modificados
- `docker-compose.yml`
- `kitchen-worker/src/main/resources/application.yml`
- `kitchen-worker/src/main/resources/db/migration/V1__create_kitchen_orders_table.sql`
- `kitchen-worker/src/main/java/com/restaurant/kitchenworker/entity/Order.java`
- `kitchen-worker/pom.xml`

## Evidencia funcional
- `kitchen-worker` deja de depender de `postgres:5432/restaurant_db` y pasa a `kitchen-postgres:5432/kitchen_db`.
- La entidad de cocina persiste en `kitchen_orders` en lugar de `orders`.
- La migracion de cocina se ejecuta en su propio modulo con Flyway.
- Se elimina el acoplamiento fisico directo entre servicios para evoluciones de esquema.
