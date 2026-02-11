## Why

La auditoría de Fase 1 se ejecutará en paralelo por tres integrantes y requiere un mecanismo uniforme para capturar hallazgos sin perder trazabilidad ni calidad de evidencia. Definir ahora el flujo de consolidación evita conflictos de merge y asegura que `AUDITORIA.md` final represente un diagnóstico coherente y verificable.

## What Changes

- Definir un flujo colaborativo para registrar hallazgos individuales en archivos separados y consolidarlos en un único reporte final.
- Establecer reglas de validación para que cada hallazgo incluya evidencia técnica, severidad e impacto.
- Formalizar el proceso de integración de hallazgos en `AUDITORIA.md`, incluyendo resolución de duplicados y priorización.
- Incluir explícitamente el archivo individual de trabajo del equipo local: `docs/auditoria/hallazgos-nico.md`.

## Capabilities

### New Capabilities
- `audit-individual-finding-tracks`: Gestión de hallazgos por auditor en archivos individuales con estructura común.
- `audit-collaborative-consolidation`: Consolidación de hallazgos de múltiples auditores en `AUDITORIA.md` con criterios de calidad y priorización.

### Modified Capabilities
- Ninguna.

## Impact

- Documentos afectados: `docs/auditoria/*.md` y `AUDITORIA.md`.
- Flujo de equipo: trabajo paralelo por auditor + merge final de evidencia.
- Proceso OpenSpec: habilita ejecución y cierre formal de la Fase 1 con trazabilidad.
