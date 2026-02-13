# DEUDA TÉCNICA - Sistema de Pedidos de Restaurante

**Proyecto:** Sistema de Pedidos de Restaurante  
**Fecha de Creación:** 13 de febrero de 2026  
**Última Actualización:** 13 de febrero de 2026  
**Responsable:** Product Owner

---

## Índice

1. [Marco Conceptual: Cuadrante de Martin Fowler](#marco-conceptual-cuadrante-de-martin-fowler)
2. [Registro de Deuda Técnica](#registro-de-deuda-técnica)
3. [Métricas y Priorización](#métricas-y-priorización)
4. [Plan de Pago](#plan-de-pago)
5. [Proceso de Gestión](#proceso-de-gestión)

---

## Marco Conceptual: Cuadrante de Martin Fowler

### Definición de Deuda Técnica

**Deuda Técnica** es el costo implícito de trabajo adicional causado por elegir una solución fácil/rápida ahora en lugar de usar un mejor enfoque que tomaría más tiempo.

### Cuadrante de Fowler

```
                    DELIBERADA
                        |
        Prudente        |        Imprudente
    "Debemos entregar  |  "No tenemos tiempo
     ahora y refactor  |   para diseño"
     después"          |
  _____________________|_____________________
                        |
    "Ahora sabemos     |  "¿Qué son las
     cómo deberíamos   |   capas?"
     haberlo hecho"    |
                        |
                   INADVERTIDA
```

### Clasificación

#### 1. Prudente y Deliberada
- **Definición:** Decisión consciente de incurrir en deuda por razones de negocio válidas
- **Ejemplo:** "Lanzamos con esta arquitectura simple para validar el mercado, refactorizaremos después"
- **Gestión:** Documentada, con plan de pago definido

#### 2. Prudente e Inadvertida
- **Definición:** Aprendizaje que surge después de implementar
- **Ejemplo:** "Ahora que lo implementamos, vemos que debimos usar un patrón diferente"
- **Gestión:** Documentar lección aprendida, evaluar si vale la pena refactorizar

#### 3. Imprudente y Deliberada
- **Definición:** Decisión consciente de hacer algo mal por presión o negligencia
- **Ejemplo:** "No tenemos tiempo para tests, lo hacemos después"
- **Gestión:** Evitar a toda costa, si ocurre: pagar inmediatamente

#### 4. Imprudente e Inadvertida
- **Definición:** Falta de conocimiento o experiencia
- **Ejemplo:** "No sabíamos que existía un patrón mejor"
- **Gestión:** Capacitación, documentación, refactor cuando se detecte

---

## Registro de Deuda Técnica


### DT-001: OrderService con Responsabilidades Mezcladas (H-ALTA-01)

**Estado:** 🟢 PAGADA (Refactorizada)  
**Clasificación:** Prudente e Inadvertida  
**Fecha de Identificación:** 2026-02-06  
**Fecha de Resolución:** 2026-02-10

**Descripción:**
La clase `OrderService` concentraba validación, persistencia, mapeo y publicación de eventos, violando el Principio de Responsabilidad Única (SRP). Adicionalmente, consultaba productos por cada item durante el mapeo (problema N+1).

**Cuadrante:** Prudente e Inadvertida
- **Por qué Prudente:** Se implementó con las mejores intenciones, siguiendo patrones conocidos
- **Por qué Inadvertida:** Solo después de la auditoría se identificó que debía separarse en componentes especializados

**Impacto:**
- Costo de cambio elevado
- Degradación de rendimiento en pedidos con muchos items
- Dificultad para testing unitario
- Violación de SOLID (SRP)

**Solución Implementada:**
- ✅ Creado `OrderValidator.java` para validaciones de negocio
- ✅ Creado `OrderMapper.java` para mapeo Entity↔DTO con batch loading
- ✅ Creado `OrderEventBuilder.java` para construcción de eventos
- ✅ Refactorizado `OrderService.java` para solo orquestar

**Costo de Pago:**
- Tiempo: ~8 horas (análisis + implementación + testing)
- Archivos modificados: 4 nuevos + 1 refactorizado
- Tests: 41/41 pasando

**Documentación:**
- `docs/refactor/H-ALTA-01-SRP-REFACTOR.md`
- `docs/refactor/H-ALTA-01-COMPLETION-SUMMARY.md`

**Lección Aprendida:**
Aplicar SRP desde el inicio, incluso en servicios "simples". Usar análisis estático (SonarQube) para detectar God Classes tempranamente.

---

### DT-002: Inconsistencia de Tipo productId (H-ALTA-04)

**Estado:** 🟢 PAGADA (Corregida)  
**Clasificación:** Imprudente e Inadvertida  
**Fecha de Identificación:** 2026-02-06  
**Fecha de Resolución:** 2026-02-10

**Descripción:**
Frontend usaba `productId: string` mientras backend esperaba `Long` (number), causando inconsistencia de contratos.

**Cuadrante:** Imprudente e Inadvertida
- **Por qué Imprudente:** No se validó el contrato entre frontend y backend antes de implementar
- **Por qué Inadvertida:** Falta de conocimiento sobre la importancia de contratos estrictos

**Impacto:**
- Potenciales errores 400 en producción
- Deuda de conversiones ad-hoc
- Confusión en el equipo sobre el tipo correcto

**Solución Implementada:**
- ✅ Cambiado `Product.id` de string a number en `src/api/contracts.ts`
- ✅ Cambiado `OrderItem.productId` de string a number
- ✅ Agregada migración en cart store para convertir IDs existentes

**Costo de Pago:**
- Tiempo: ~2 horas
- Archivos modificados: 2
- Compilación TypeScript: exitosa

**Documentación:**
- `docs/refactor/H-ALTA-04-PRODUCTID-TYPE-FIX.md`
- `docs/refactor/H-ALTA-04-COMPLETION-SUMMARY.md`

**Lección Aprendida:**
Definir contratos de API (OpenAPI) ANTES de implementar. Usar generadores de código para garantizar consistencia entre frontend y backend.

---

### DT-003: Inyección por Campo en Kitchen Worker (H-MEDIA-01)

**Estado:** 🟢 PAGADA (Refactorizada)  
**Clasificación:** Imprudente e Inadvertida  
**Fecha de Identificación:** 2026-02-06  
**Fecha de Resolución:** 2026-02-13

**Descripción:**
Dependencias inyectadas por campo con `@Autowired` en lugar de constructor, violando el Principio de Inversión de Dependencias (DIP).

**Cuadrante:** Imprudente e Inadvertida
- **Por qué Imprudente:** Uso de anti-pattern conocido (field injection)
- **Por qué Inadvertida:** Desconocimiento de mejores prácticas de Spring

**Impacto:**
- Menor testabilidad
- Mayor acoplamiento al contenedor
- Campos no inmutables

**Solución Implementada:**
- ✅ Convertido a constructor injection usando `@RequiredArgsConstructor`
- ✅ Campos ahora son `final` para inmutabilidad
- ✅ Tests actualizados y pasando (9/9)

**Costo de Pago:**
- Tiempo: ~3 horas
- Archivos modificados: 3
- Tests: 9/9 pasando

**Documentación:**
- `docs/refactor/H-MEDIA-01-DIP-FIX.md`
- `docs/refactor/H-MEDIA-01-COMPLETION-SUMMARY.md`

**Lección Aprendida:**
Establecer guía de estilo que prohíba field injection. Configurar análisis estático para detectar este anti-pattern.

---


### DT-004: Gap de Consistencia entre Persistencia y Publicación de Eventos (H-ALTA-02)

**Estado:** 🔴 PENDIENTE  
**Clasificación:** Prudente y Deliberada  
**Fecha de Identificación:** 2026-02-06  
**Fecha Estimada de Pago:** 2026-03-15

**Descripción:**
El publisher captura excepciones de RabbitMQ sin cortar el flujo, dejando orden persistida pero potencialmente no publicada, causando inconsistencias entre Order Service y Kitchen Worker.

**Cuadrante:** Prudente y Deliberada
- **Por qué Prudente:** Se decidió lanzar MVP sin Outbox pattern para validar el negocio
- **Por qué Deliberada:** Decisión consciente documentada en ADR (pendiente)

**Impacto:**
- Inconsistencias entre servicios ante fallos de broker
- Pedidos "perdidos" en Kitchen Worker
- Requiere reconciliación manual

**Solución Propuesta:**
- Implementar Outbox Pattern
- Agregar tabla `outbox_events` en Order Service
- Worker dedicado para publicar eventos desde outbox
- Retry policy explícita

**Costo Estimado de Pago:**
- Tiempo: ~16 horas (diseño + implementación + testing)
- Complejidad: Alta
- Riesgo: Medio (requiere migración de datos)

**Plan de Pago:**
1. Diseñar Outbox Pattern (4h)
2. Implementar tabla y worker (6h)
3. Migrar lógica de publicación (4h)
4. Testing y validación (2h)

**Justificación de Deuda:**
Se priorizó lanzar MVP rápido para validar el mercado. El riesgo de inconsistencia es bajo en volúmenes pequeños. Se pagará cuando el volumen aumente o se detecten inconsistencias frecuentes.

**Trigger para Pago:**
- Volumen > 1000 pedidos/día
- Inconsistencias > 5/semana
- Fecha límite: 2026-03-15

---

### DT-005: Microservicios Acoplados por Base de Datos Compartida (H-ALTA-03)

**Estado:** 🔴 PENDIENTE  
**Clasificación:** Prudente y Deliberada  
**Fecha de Identificación:** 2026-02-06  
**Fecha Estimada de Pago:** 2026-04-30

**Descripción:**
Order Service y Kitchen Worker comparten la misma base de datos y tabla `orders`, debilitando la independencia de servicios.

**Cuadrante:** Prudente y Deliberada
- **Por qué Prudente:** Decisión consciente para simplificar MVP
- **Por qué Deliberada:** Se documentó como deuda técnica desde el inicio

**Impacto:**
- Migraciones acopladas
- Riesgo de regresiones cruzadas
- Menor autonomía de despliegue
- Violación de principios de microservicios

**Solución Propuesta:**
- Separar bases de datos por servicio
- Implementar Event-Carried State Transfer
- Kitchen Worker mantiene proyección local de órdenes
- Sincronización vía eventos

**Costo Estimado de Pago:**
- Tiempo: ~40 horas (diseño + implementación + migración + testing)
- Complejidad: Muy Alta
- Riesgo: Alto (requiere migración de datos en producción)

**Plan de Pago:**
1. Diseñar arquitectura de datos separada (8h)
2. Crear base de datos para Kitchen Worker (4h)
3. Implementar proyección local (12h)
4. Migrar datos existentes (8h)
5. Testing exhaustivo (8h)

**Justificación de Deuda:**
Compartir DB simplificó el MVP y permitió lanzar rápido. El acoplamiento es aceptable para volúmenes bajos. Se pagará cuando se requiera escalar servicios independientemente.

**Trigger para Pago:**
- Necesidad de escalar servicios independientemente
- Conflictos frecuentes en migraciones
- Fecha límite: 2026-04-30

---

### DT-006: Seguridad de Cocina No Aplicada de Extremo a Extremo (H-ALTA-05)

**Estado:** 🟡 EN PROGRESO  
**Clasificación:** Imprudente y Deliberada  
**Fecha de Identificación:** 2026-02-06  
**Fecha Estimada de Pago:** 2026-02-28

**Descripción:**
Existe mecanismo de token/header en frontend, pero login de cocina entra directo y backend no valida cabecera de autorización en endpoints críticos.

**Cuadrante:** Imprudente y Deliberada
- **Por qué Imprudente:** Seguridad es crítica, no debería posponerse
- **Por qué Deliberada:** Se decidió lanzar sin autenticación completa por presión de tiempo

**Impacto:**
- Operaciones de cocina sin control de acceso efectivo
- Riesgo de seguridad alto
- Posible acceso no autorizado

**Solución Propuesta:**
- Implementar validación de token en backend
- Agregar interceptor de seguridad en Spring
- Implementar guardas de ruta en frontend
- Agregar tests de seguridad

**Costo Estimado de Pago:**
- Tiempo: ~12 horas
- Complejidad: Media
- Riesgo: Medio

**Plan de Pago:**
1. Implementar interceptor de seguridad (4h)
2. Agregar guardas de ruta (3h)
3. Tests de seguridad (3h)
4. Documentación (2h)

**Justificación de Deuda:**
Se priorizó funcionalidad sobre seguridad para MVP interno. DEBE pagarse antes de producción.

**Trigger para Pago:**
- INMEDIATO antes de producción
- Fecha límite: 2026-02-28

---


### DT-007: Ausencia de Capas Arquitectónicas Claras (H-ALTA-06)

**Estado:** 🔴 PENDIENTE  
**Clasificación:** Imprudente e Inadvertida  
**Fecha de Identificación:** 2026-02-06  
**Fecha Estimada de Pago:** 2026-05-31

**Descripción:**
La estructura actual mezcla lógica de negocio, orquestación y detalles de infraestructura sin fronteras explícitas por capa.

**Cuadrante:** Imprudente e Inadvertida
- **Por qué Imprudente:** Arquitectura limpia es fundamental para mantenibilidad
- **Por qué Inadvertida:** Falta de experiencia en arquitectura hexagonal/clean

**Impacto:**
- Acoplamiento transversal alto
- Costo de evolución elevado
- Dificultad para testing
- Código difícil de entender

**Solución Propuesta:**
- Implementar arquitectura hexagonal/clean
- Separar capas: Domain, Application, Infrastructure
- Definir puertos y adaptadores
- Refactorizar servicios existentes

**Costo Estimado de Pago:**
- Tiempo: ~80 horas (diseño + refactor + testing)
- Complejidad: Muy Alta
- Riesgo: Alto (refactor masivo)

**Plan de Pago:**
1. Diseñar arquitectura objetivo (16h)
2. Crear estructura de capas (8h)
3. Refactorizar Order Service (24h)
4. Refactorizar Kitchen Worker (16h)
5. Testing exhaustivo (16h)

**Justificación de Deuda:**
Se priorizó velocidad de desarrollo sobre arquitectura limpia para MVP. Es aceptable para sistema pequeño. Se pagará cuando el sistema crezca en complejidad.

**Trigger para Pago:**
- Sistema > 10 servicios
- Equipo > 5 desarrolladores
- Fecha límite: 2026-05-31

---

### DT-008: KitchenBoardPage con Múltiples Responsabilidades (H-MEDIA-02)

**Estado:** 🔴 PENDIENTE  
**Clasificación:** Prudente e Inadvertida  
**Fecha de Identificación:** 2026-02-06  
**Fecha Estimada de Pago:** 2026-03-31

**Descripción:**
Componente maneja polling, fetch, agrupación, mutación y render extenso, violando SRP.

**Cuadrante:** Prudente e Inadvertida
- **Por qué Prudente:** Se implementó con buenas intenciones
- **Por qué Inadvertida:** Solo después de la auditoría se identificó la necesidad de separar

**Impacto:**
- Fragilidad ante cambios
- Mayor riesgo de regresión UI/estado
- Dificultad para testing
- Código difícil de mantener

**Solución Propuesta:**
- Extraer custom hook `useKitchenOrders` para polling
- Crear componente `OrderCard` para render de pedidos
- Separar lógica de agrupación en utilidad
- Implementar Observer/Strategy para transiciones

**Costo Estimado de Pago:**
- Tiempo: ~12 horas
- Complejidad: Media
- Riesgo: Bajo

**Plan de Pago:**
1. Extraer custom hook (4h)
2. Crear componentes especializados (4h)
3. Refactorizar lógica de agrupación (2h)
4. Testing (2h)

**Justificación de Deuda:**
Se priorizó funcionalidad sobre arquitectura limpia. Es aceptable para MVP. Se pagará cuando se agreguen más features a Kitchen Board.

**Trigger para Pago:**
- Agregar nueva funcionalidad a Kitchen Board
- Fecha límite: 2026-03-31

---

### DT-009: Contrato de Eventos con Baja Resiliencia (H-MEDIA-03)

**Estado:** 🔴 PENDIENTE  
**Clasificación:** Prudente y Deliberada  
**Fecha de Identificación:** 2026-02-06  
**Fecha Estimada de Pago:** 2026-03-31

**Descripción:**
La publicación/consumo de eventos carece de estrategia robusta de recuperación y versionado de contrato.

**Cuadrante:** Prudente y Deliberada
- **Por qué Prudente:** Se decidió lanzar sin versionado complejo para MVP
- **Por qué Deliberada:** Decisión consciente documentada

**Impacto:**
- Riesgo de inconsistencia entre servicios
- Dificultad para evolucionar contratos
- Falta de estrategia de retry

**Solución Propuesta:**
- Implementar versionado de eventos (schema registry)
- Agregar retry policy explícita
- Implementar Dead Letter Queue
- Documentar contratos con AsyncAPI

**Costo Estimado de Pago:**
- Tiempo: ~20 horas
- Complejidad: Alta
- Riesgo: Medio

**Plan de Pago:**
1. Diseñar estrategia de versionado (4h)
2. Implementar schema registry (6h)
3. Configurar retry policy y DLQ (4h)
4. Documentar con AsyncAPI (4h)
5. Testing (2h)

**Justificación de Deuda:**
Se priorizó simplicidad para MVP. Es aceptable para volúmenes bajos. Se pagará cuando se requiera mayor resiliencia.

**Trigger para Pago:**
- Volumen > 1000 eventos/día
- Necesidad de evolucionar contratos
- Fecha límite: 2026-03-31

---

### DT-010: Drift Documental en AI_WORKFLOW.md (H-BAJA-01)

**Estado:** 🔴 PENDIENTE  
**Clasificación:** Imprudente e Inadvertida  
**Fecha de Identificación:** 2026-02-06  
**Fecha Estimada de Pago:** 2026-02-20

**Descripción:**
Documento operativo tiene comandos desalineados y codificación degradada.

**Cuadrante:** Imprudente e Inadvertida
- **Por qué Imprudente:** Documentación desactualizada causa confusión
- **Por qué Inadvertida:** No se detectó durante desarrollo

**Impacto:**
- Ruido operativo
- Errores de uso para nuevos colaboradores
- Pérdida de tiempo

**Solución Propuesta:**
- Actualizar comandos a versión actual
- Corregir codificación
- Agregar ejemplos actualizados
- Validar con equipo

**Costo Estimado de Pago:**
- Tiempo: ~2 horas
- Complejidad: Baja
- Riesgo: Bajo

**Plan de Pago:**
1. Revisar y actualizar comandos (1h)
2. Validar con equipo (0.5h)
3. Agregar ejemplos (0.5h)

**Justificación de Deuda:**
Baja prioridad, no bloquea desarrollo. Se pagará en próxima iteración de documentación.

**Trigger para Pago:**
- Onboarding de nuevo miembro
- Fecha límite: 2026-02-20

---


### DT-011: Brechas de Calidad No Funcional (H-BAJA-02)

**Estado:** 🔴 PENDIENTE  
**Clasificación:** Prudente y Deliberada  
**Fecha de Identificación:** 2026-02-06  
**Fecha Estimada de Pago:** 2026-06-30

**Descripción:**
Gaps en observabilidad centralizada, cobertura de tests y endurecimiento (rate limiting, control de abuso).

**Cuadrante:** Prudente y Deliberada
- **Por qué Prudente:** Se decidió lanzar MVP sin observabilidad completa
- **Por qué Deliberada:** Decisión consciente por priorización

**Impacto:**
- Detección tardía de incidentes
- Mayor riesgo operativo en crecimiento
- Dificultad para debugging en producción

**Solución Propuesta:**
- Implementar observabilidad centralizada (ELK/Grafana)
- Aumentar cobertura de tests a >80%
- Agregar rate limiting
- Implementar circuit breakers

**Costo Estimado de Pago:**
- Tiempo: ~40 horas
- Complejidad: Alta
- Riesgo: Medio

**Plan de Pago:**
1. Implementar observabilidad (16h)
2. Aumentar cobertura de tests (12h)
3. Agregar rate limiting (6h)
4. Implementar circuit breakers (6h)

**Justificación de Deuda:**
Se priorizó funcionalidad sobre observabilidad para MVP. Es aceptable para volúmenes bajos. Se pagará antes de escalar a producción.

**Trigger para Pago:**
- Lanzamiento a producción
- Volumen > 500 usuarios concurrentes
- Fecha límite: 2026-06-30

---

## Métricas y Priorización

### Resumen por Estado

| Estado | Cantidad | Porcentaje |
|--------|----------|------------|
| 🟢 Pagada | 3 | 27% |
| 🟡 En Progreso | 1 | 9% |
| 🔴 Pendiente | 7 | 64% |
| **TOTAL** | **11** | **100%** |

### Resumen por Cuadrante

| Cuadrante | Cantidad | Porcentaje |
|-----------|----------|------------|
| Prudente y Deliberada | 5 | 45% |
| Prudente e Inadvertida | 3 | 27% |
| Imprudente y Deliberada | 1 | 9% |
| Imprudente e Inadvertida | 2 | 18% |
| **TOTAL** | **11** | **100%** |

### Resumen por Severidad

| Severidad | Cantidad | Porcentaje |
|-----------|----------|------------|
| Alta | 6 | 55% |
| Media | 3 | 27% |
| Baja | 2 | 18% |
| **TOTAL** | **11** | **100%** |

### Costo Total Estimado

| Categoría | Horas | Porcentaje |
|-----------|-------|------------|
| Pagada | 13h | 6% |
| En Progreso | 12h | 6% |
| Pendiente | 206h | 88% |
| **TOTAL** | **231h** | **100%** |

### Priorización (Matriz de Impacto vs Esfuerzo)

```
Alto Impacto
    |
    |  DT-006 (Seguridad)     DT-004 (Outbox)
    |  [12h] 🟡               [16h] 🔴
    |
    |  DT-008 (Kitchen UI)    DT-005 (DB Compartida)
    |  [12h] 🔴               [40h] 🔴
    |
    |  DT-010 (Docs)          DT-007 (Arquitectura)
    |  [2h] 🔴                [80h] 🔴
    |
Bajo Impacto
    |________________________
    Bajo Esfuerzo          Alto Esfuerzo
```

### Orden de Pago Recomendado

1. **DT-010** (Docs) - 2h - Bajo esfuerzo, mejora onboarding
2. **DT-006** (Seguridad) - 12h - Crítico para producción
3. **DT-008** (Kitchen UI) - 12h - Mejora mantenibilidad
4. **DT-004** (Outbox) - 16h - Mejora consistencia
5. **DT-009** (Eventos) - 20h - Mejora resiliencia
6. **DT-005** (DB) - 40h - Desacopla servicios
7. **DT-011** (Observabilidad) - 40h - Mejora operación
8. **DT-007** (Arquitectura) - 80h - Refactor masivo

---

## Plan de Pago

### Q1 2026 (Febrero - Marzo)

**Objetivo:** Pagar deuda crítica y de seguridad

- ✅ **DT-001** (OrderService SRP) - PAGADA
- ✅ **DT-002** (productId Type) - PAGADA
- ✅ **DT-003** (Field Injection) - PAGADA
- 🟡 **DT-006** (Seguridad) - EN PROGRESO → Completar antes de 2026-02-28
- 🔴 **DT-010** (Docs) - Pagar antes de 2026-02-20
- 🔴 **DT-004** (Outbox) - Pagar antes de 2026-03-15
- 🔴 **DT-008** (Kitchen UI) - Pagar antes de 2026-03-31
- 🔴 **DT-009** (Eventos) - Pagar antes de 2026-03-31

**Horas Totales Q1:** 74h

### Q2 2026 (Abril - Junio)

**Objetivo:** Pagar deuda arquitectónica y de escalabilidad

- 🔴 **DT-005** (DB Compartida) - Pagar antes de 2026-04-30
- 🔴 **DT-007** (Arquitectura) - Pagar antes de 2026-05-31
- 🔴 **DT-011** (Observabilidad) - Pagar antes de 2026-06-30

**Horas Totales Q2:** 160h

### Capacidad del Equipo

**Equipo:** 3 desarrolladores  
**Capacidad por Sprint (2 semanas):** 120h (40h/dev)  
**Dedicación a Deuda Técnica:** 20% = 24h/sprint

**Sprints Necesarios:**
- Q1: 74h / 24h = ~3 sprints
- Q2: 160h / 24h = ~7 sprints

---

## Proceso de Gestión

### Identificación de Nueva Deuda

**Cuándo Registrar:**
- Durante auditorías de código
- En retrospectivas de sprint
- Al detectar problemas de rendimiento
- Cuando se identifica un anti-pattern

**Cómo Registrar:**
1. Crear entrada en este documento
2. Clasificar según cuadrante de Fowler
3. Estimar costo de pago
4. Definir trigger para pago
5. Asignar prioridad

### Revisión Periódica

**Frecuencia:** Cada sprint (2 semanas)

**Agenda:**
1. Revisar estado de deuda en progreso
2. Evaluar nuevas deudas identificadas
3. Repriorizar según contexto de negocio
4. Asignar deuda a pagar en próximo sprint

### Criterios de Priorización

**Factores a Considerar:**
1. **Impacto en el negocio** (Alto/Medio/Bajo)
2. **Riesgo técnico** (Alto/Medio/Bajo)
3. **Esfuerzo de pago** (Horas estimadas)
4. **Dependencias** (Bloquea otras tareas?)
5. **Fecha límite** (Trigger definido)

**Fórmula de Prioridad:**
```
Prioridad = (Impacto * 3 + Riesgo * 2) / Esfuerzo
```

### Política de Deuda

**Reglas:**
1. **No agregar deuda Imprudente y Deliberada** sin aprobación del Product Owner
2. **Documentar toda deuda** en este registro
3. **Pagar deuda crítica** antes de agregar nuevas features
4. **Dedicar 20% del tiempo** a pagar deuda técnica
5. **Revisar deuda** en cada retrospectiva

**Límites:**
- Máximo 15 deudas activas simultáneamente
- Máximo 3 deudas de severidad Alta sin plan de pago
- Máximo 6 meses de antigüedad para deuda Alta

---

## Referencias

### Documentación Relacionada

- **AUDITORIA.md:** Hallazgos consolidados de auditoría
- **CALIDAD.md:** Anatomía de incidentes y gestión de calidad
- **AI_WORKFLOW.md:** Protocolo de trabajo con IA (incluye Quality Gate)

### Recursos Externos

- [Martin Fowler - Technical Debt Quadrant](https://martinfowler.com/bliki/TechnicalDebtQuadrant.html)
- [Managing Technical Debt](https://www.infoq.com/articles/managing-technical-debt/)
- [Technical Debt: From Metaphor to Theory and Practice](https://resources.sei.cmu.edu/library/asset-view.cfm?assetid=9012)

---

**Documento Creado:** 13 de febrero de 2026  
**Última Actualización:** 13 de febrero de 2026  
**Responsable:** Product Owner  
**Próxima Revisión:** 27 de febrero de 2026 (Sprint Review)

