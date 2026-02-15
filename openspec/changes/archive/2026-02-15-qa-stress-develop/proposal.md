## Why

Necesitamos validar que `develop` es estable y soporta carga realista antes de considerar un despliegue. El QA + stress test documentado reduce el riesgo de fallos en produccion y deja trazabilidad de calidad.

## What Changes

- Ejecutar pruebas agresivas de carga sobre la API en `develop`.
- Validar flujos de UI (cliente y cocina) y registrar resultados.
- Documentar evidencias en un reporte QA que se mergea a `develop`.

## Capabilities

### New Capabilities
- `qa-stress-report`: Reporte de QA y stress test (comandos, resultados, evidencias) para validar calidad en `develop`.

### Modified Capabilities
- (none)

## Impact

- Documentacion nueva en `docs/qa/`.
- Posible script de prueba de carga (k6) y comandos de verificacion.
- Uso de Docker Compose para levantar el stack completo.
