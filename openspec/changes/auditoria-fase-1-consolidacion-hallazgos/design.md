## Context

Ya existe una preparación de auditoría (alcance y plantilla) y el equipo decidió operar con tres fuentes de hallazgos antes de consolidar. El riesgo principal en esta fase no es técnico de runtime, sino de calidad del diagnóstico: hallazgos inconsistentes, duplicados o sin evidencia pueden degradar decisiones de arquitectura en Fase 2 y refactor en Fase 3.

## Goals / Non-Goals

**Goals:**
- Permitir que cada auditor documente hallazgos de forma autónoma y homogénea.
- Garantizar que `AUDITORIA.md` final consolide únicamente hallazgos verificables y priorizados.
- Definir un proceso reproducible para resolver duplicados y diferencias de criterio.

**Non-Goals:**
- Ejecutar refactorizaciones de código en este cambio.
- Cerrar decisiones de patrones de diseño (eso corresponde a Fase 2).
- Reemplazar el alcance base definido en `docs/auditoria/ALCANCE_FASE1.md`.

## Decisions

1. Registrar hallazgos individuales en archivos separados bajo `docs/auditoria/`.
   - Ruta fija para este equipo: `docs/auditoria/hallazgos-nico.md`.
   - Rutas análogas para otros auditores.
   - Alternativa descartada: editar simultáneamente `AUDITORIA.md`.
   - Rationale: reduce conflictos y preserva autoría por evidencia.

2. Exigir estructura obligatoria de hallazgo basada en la plantilla existente.
   - Campos mínimos: componente, ruta/módulo, evidencia, principio/smell, impacto, recomendación, severidad.
   - Alternativa descartada: notas libres por persona.
   - Rationale: mejora comparabilidad y consolidación objetiva.

3. Consolidar en `AUDITORIA.md` por severidad y dominio técnico, no por autor.
   - Orden recomendado: Alta -> Media -> Baja, con agrupación backend/frontend/integración.
   - Alternativa descartada: consolidación por orden cronológico.
   - Rationale: priorización orientada a riesgo y plan de evolución.

4. Tratar hallazgos duplicados mediante fusión de evidencia en una sola entrada maestra.
   - Regla: si dos hallazgos describen el mismo problema raíz, mantener uno y anexar referencias cruzadas.
   - Alternativa descartada: mantener duplicados con distinto autor.
   - Rationale: evita inflar deuda y sesgar priorización.

## Risks / Trade-offs

- [Risk] Hallazgos subjetivos o sin evidencia reproducible -> Mitigation: bloqueo de consolidación si falta referencia técnica.
- [Risk] Divergencia de criterios entre auditores -> Mitigation: sesión corta de calibración antes del merge final.
- [Trade-off] Tiempo extra de consolidación editorial -> Mitigation: formato fijo y criterios de deduplicación predefinidos.

## Migration Plan

1. Crear/actualizar archivos de hallazgos individuales.
2. Ejecutar auditoría por dominio con plantilla común.
3. Revisar calidad mínima de cada hallazgo.
4. Consolidar y deduplicar en `AUDITORIA.md`.
5. Validar trazabilidad hacia Fase 2 y Fase 3.

## Open Questions

- ¿Quién será editor final de `AUDITORIA.md` y quién aprobará versión final?
- ¿Se limitará número de hallazgos por severidad para evitar ruido?
- ¿Se incluirán evidencias de comandos/logs además de rutas de código?
