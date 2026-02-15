# Evidencia de Implementacion - H-ALTA-03

**Fecha:** 2026-02-13
**Hallazgo:** H-ALTA-03 - Microservicios acoplados por base de datos/tabla compartida
**Rama:** `feature/auditoria-fase-1-ejecucion`
**Estado:** COMPLETADO

---

## Defecto

`order-service` y `kitchen-worker` compartian la misma base y tabla, elevando riesgo de acoplamiento operativo y regresiones cruzadas.

## Resumen de solucion

Se aplico `database-per-service`:
- `order-service` mantiene `restaurant_db`.
- `kitchen-worker` opera sobre `kitchen_db` y tabla propia `kitchen_orders`.

## Commits relacionados

- `81cc2ad` `refactor(auditoria): desacoplar DB por servicio y robustecer contrato de eventos`

## Archivos principales modificados

- `docker-compose.yml`
- `kitchen-worker/src/main/resources/application.yml`
- `kitchen-worker/src/main/resources/db/migration/V1__create_kitchen_orders_table.sql`
- `kitchen-worker/src/main/java/com/restaurant/kitchenworker/entity/Order.java`
- `kitchen-worker/pom.xml`

## Evidencia funcional

- `kitchen-worker` deja `postgres:5432/restaurant_db` y usa `kitchen-postgres:5432/kitchen_db`.
- La persistencia de cocina se mueve a `kitchen_orders`.
- Flyway de `kitchen-worker` gestiona esquema propio sin depender de migraciones de `order-service`.

## Estado

- Mitigacion implementada y probada en rama de ejecucion.
- Referenciada en `AUDITORIA.md`.
- Lista para merge a `develop`.
