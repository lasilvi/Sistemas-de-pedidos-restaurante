# DEUDA TECNICA - Fase 5

Proyecto: Sistema de pedidos de restaurante  
Fecha de actualizacion: 2026-02-13  
Estado del registro: activo y trazable con `AUDITORIA.md`

## Objetivo

Registrar y priorizar deuda tecnica de forma accionable para planificacion de sprint.  
Cada item `DT-*` enlaza su hallazgo origen (`H-*`), owner, fecha objetivo y evidencia.

## Estandar por item DT

Campos obligatorios por deuda:
- `ID`: identificador unico (`DT-*`)
- `Origen`: hallazgo en `AUDITORIA.md` (`H-*`)
- `Clasificacion`: cuadrante de Fowler
- `Estado`: `PENDIENTE`, `EN_PROGRESO`, `PAGADA`, `POSTERGADA`
- `Impacto`: `ALTO`, `MEDIO`, `BAJO`
- `Esfuerzo`: horas estimadas o ejecutadas
- `Prioridad`: `P1` (alta) a `P3` (baja)
- `Owner`: responsable de seguimiento
- `Fecha objetivo`: fecha limite de pago o cierre
- `Trigger`: condicion que obliga a ejecutar pago
- `Evidencia/Plan`: enlace de prueba o plan tecnico

## Registro consolidado

| ID | Origen | Clasificacion | Estado | Impacto | Esfuerzo | Prioridad | Owner | Fecha objetivo | Trigger | Evidencia/Plan |
|---|---|---|---|---|---|---|---|---|---|---|
| DT-001 | H-ALTA-01 | Prudente/Inadvertida | PAGADA | ALTO | 8h | P1 | Backend | 2026-02-10 | Hallazgo identificado | `docs/refactor/H-ALTA-01-COMPLETION-SUMMARY.md` |
| DT-002 | H-ALTA-04 | Imprudente/Inadvertida | PAGADA | ALTO | 2h | P1 | Frontend | 2026-02-10 | Contrato inconsistente detectado | `docs/refactor/H-ALTA-04-COMPLETION-SUMMARY.md` |
| DT-003 | H-MEDIA-01 | Imprudente/Inadvertida | PAGADA | MEDIO | 3h | P2 | Kitchen Worker | 2026-02-13 | Refactor DIP requerido | `docs/refactor/H-MEDIA-01-COMPLETION-SUMMARY.md` |
| DT-004 | H-ALTA-02 | Prudente/Deliberada | PAGADA | ALTO | 16h | P1 | Backend + Integracion | 2026-02-13 | Riesgo de inconsistencia por broker | `docs/auditoria/EVIDENCIA_H-ALTA-02.md` |
| DT-005 | H-ALTA-03 | Prudente/Deliberada | PAGADA | ALTO | 40h | P1 | Arquitectura | 2026-02-13 | Acoplamiento por DB compartida | `docs/auditoria/EVIDENCIA_H-ALTA-03.md` |
| DT-006 | H-ALTA-05 | Imprudente/Deliberada | PAGADA | ALTO | 12h | P1 | Seguridad Fullstack | 2026-02-13 | Endpoints de cocina sin control robusto | `docs/auditoria/EVIDENCIA_H-ALTA-05.md` |
| DT-007 | H-ALTA-06 | Imprudente/Inadvertida | PAGADA | ALTO | 32h | P1 | Arquitectura Backend | 2026-02-13 | Falta de fronteras por capas | `docs/auditoria/EVIDENCIA_H-ALTA-06.md` |
| DT-008 | H-MEDIA-02 | Prudente/Inadvertida | PAGADA | MEDIO | 12h | P2 | Frontend Cocina | 2026-02-13 | Componente con exceso de responsabilidades | `docs/auditoria/EVIDENCIA_H-MEDIA-02.md` |
| DT-009 | H-MEDIA-03 | Prudente/Deliberada | PAGADA | MEDIO | 20h | P2 | Integracion Eventos | 2026-02-13 | Contrato de evento poco resiliente | `docs/auditoria/EVIDENCIA_H-MEDIA-03.md` |
| DT-010 | H-BAJA-01 | Imprudente/Inadvertida | PENDIENTE | BAJO | 2h | P3 | Docs/Workflow | 2026-02-27 | Proxima iteracion de documentacion operativa | Actualizar `AI_WORKFLOW.md` y validar comandos |
| DT-011 | H-BAJA-02 | Prudente/Deliberada | PENDIENTE | MEDIO | 24h | P2 | Calidad/Plataforma | 2026-03-31 | Inicio de hardening pre-produccion | Plan de observabilidad, cobertura y controles de abuso |

## Deuda abierta (resumen explicativo)

### DT-010 - Drift documental de workflow IA/OpenSpec
- Motivo: comandos y formato documental desalineados afectan onboarding.
- Accion de pago: normalizar comandos vigentes y ejemplos en `AI_WORKFLOW.md`.
- Resultado esperado: cero ambiguedad operativa para nuevos colaboradores.

### DT-011 - Brechas de calidad no funcional
- Motivo: faltan controles de observabilidad y hardening para crecimiento.
- Accion de pago: definir baseline de metricas, cobertura minima y controles de abuso.
- Resultado esperado: menor riesgo operativo antes de escalar a produccion.

## Priorizacion y roadmap

Prioridad actual:
- `P1`: deuda ya pagada de seguridad, consistencia e integracion.
- `P2`: cerrar `DT-011` en ventana pre-produccion.
- `P3`: cerrar `DT-010` en siguiente ajuste documental.

Roadmap recomendado:
1. Sprint actual: pagar `DT-010` (rapido, bajo esfuerzo).
2. Siguiente sprint: ejecutar `DT-011` por bloques (observabilidad, pruebas, hardening).

## Ciclo de gestion

Reglas de gobierno:
1. Revision minima de deuda en cada sprint review.
2. Ningun `DT-*` de impacto alto puede quedar sin owner o fecha objetivo.
3. Para marcar `PAGADA` se exige evidencia en PR y referencia en `AUDITORIA.md`.
4. Si un `DT-*` pasa a `POSTERGADA`, debe registrar justificacion y nueva fecha objetivo.

Checklist de cierre por item:
- Owner definido.
- Fecha objetivo definida.
- Trigger vigente.
- Evidencia o plan tecnico enlazado.
