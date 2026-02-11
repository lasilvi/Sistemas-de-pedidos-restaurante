## ADDED Requirements

### Requirement: Consolidation produces a single prioritized report
La consolidación SHALL generar un `AUDITORIA.md` único con hallazgos deduplicados y priorizados por severidad.

#### Scenario: Consolidation session completes
- **WHEN** se integran los hallazgos de los tres auditores
- **THEN** `AUDITORIA.md` contiene una versión única y priorizada del diagnóstico

### Requirement: Duplicate findings are merged by root problem
Si múltiples auditores reportan el mismo problema raíz, el proceso SHALL fusionar esas entradas en un único hallazgo maestro con evidencia consolidada.

#### Scenario: Two auditors report same issue
- **WHEN** se detectan hallazgos que describen la misma causa técnica
- **THEN** se conserva una sola entrada con referencias de evidencia de ambas fuentes

### Requirement: Final report is traceable to individual sources
`AUDITORIA.md` SHALL mantener trazabilidad a los archivos individuales de origen para cada hallazgo consolidado.

#### Scenario: Reviewer inspects consolidated report
- **WHEN** se revisa un hallazgo consolidado
- **THEN** se puede identificar de qué archivos individuales provino la evidencia

### Requirement: Consolidation maps to next phases
El reporte consolidado SHALL incluir mapeo de hallazgos críticos hacia patrones candidatos (Fase 2) y puntos de refactor (Fase 3).

#### Scenario: Team prepares phase transition
- **WHEN** finaliza la consolidación
- **THEN** los hallazgos críticos incluyen una propuesta de continuidad hacia Fase 2 y Fase 3
