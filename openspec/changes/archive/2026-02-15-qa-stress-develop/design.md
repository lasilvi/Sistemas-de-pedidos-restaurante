## Context

Necesitamos ejecutar QA y stress tests sobre `develop` antes de promover a `main`. El stack se levanta via Docker Compose y se requiere documentar resultados y evidencia en un reporte que luego se mergea a `develop`.

## Goals / Non-Goals

**Goals:**
- Ejecutar QA manual (UI) y smoke tests API en `develop`.
- Ejecutar stress test agresivo sobre la API con k6.
- Documentar resultados, comandos y evidencia en `docs/qa/QA-YYYY-MM-DD-develop.md`.
- Mantener trazabilidad via un branch de QA y PR hacia `develop`.

**Non-Goals:**
- No cambiar reglas de negocio ni contratos API/eventos.
- No introducir CI/CD o automatizacion permanente.

## Decisions

- **Docker Compose como entorno QA**: garantiza entorno reproducible con front/back/worker/db/broker.
- **k6 via Docker**: evita instalar herramientas locales y permite stress test consistente.
- **Branch de QA separado**: evita contaminar `develop` con experimentos y permite PR con evidencia.
- **Reporte en docs/qa**: centraliza la evidencia y facilita auditoria.

## Risks / Trade-offs

- [Riesgo] Carga agresiva puede degradar la maquina local → Mitigacion: permitir reducir VUs/duracion si hay inestabilidad.
- [Riesgo] Variabilidad de resultados por hardware → Mitigacion: registrar specs del entorno en el reporte.
- [Riesgo] Datos de prueba pueden contaminar DB → Mitigacion: usar `docker compose down -v` antes de pruebas.

## Migration Plan

1. Crear branch `qa/qa-stress-develop` desde `develop`.
2. Ejecutar QA + stress tests.
3. Crear reporte en `docs/qa/QA-YYYY-MM-DD-develop.md`.
4. PR hacia `develop` con el reporte.

## Open Questions

- Definir umbrales de aceptacion (p95, error rate) si se requiere un gate formal.
