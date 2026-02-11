## ADDED Requirements

### Requirement: Each auditor records findings in a dedicated file
El proceso de auditoría SHALL usar un archivo individual por auditor dentro de `docs/auditoria/` para registrar hallazgos antes de la consolidación.

#### Scenario: Team starts parallel analysis
- **WHEN** los auditores inician la revisión técnica
- **THEN** cada uno trabaja en su archivo individual sin editar directamente `AUDITORIA.md`

### Requirement: Team local file name is fixed
El equipo local SHALL registrar sus hallazgos en `docs/auditoria/hallazgos-nico.md`.

#### Scenario: Local auditor writes a finding
- **WHEN** se documenta un hallazgo del equipo local
- **THEN** el hallazgo se agrega en `docs/auditoria/hallazgos-nico.md`

### Requirement: Individual findings follow mandatory structure
Cada hallazgo individual SHALL contener componente, ruta/módulo, tipo (SOLID/Smell), evidencia, impacto, recomendación y severidad.

#### Scenario: Auditor submits findings for consolidation
- **WHEN** un auditor entrega su archivo para merge
- **THEN** todos los hallazgos cumplen la estructura mínima requerida
