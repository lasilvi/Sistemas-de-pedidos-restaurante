# GitHub Copilot Instructions — Sistema de Pedidos de Restaurante

## 1. Contexto del Proyecto (OBLIGATORIO)
Este repositorio corresponde a un sistema full-stack de gestión de pedidos para restaurante,
recibido en contexto de **legacy handover (Brownfield)**.

⚠️ Regla crítica:
- NO asumir greenfield
- NO reescribir arquitectura
- NO romper contratos existentes (REST, eventos AMQP, DB)

El sistema es **event-driven** y los servicios **NO se comunican entre sí vía REST**.
La única integración entre `order-service` y `kitchen-worker` es **RabbitMQ**.

---

## 2. Arquitectura General (NO VIOLAR)

### Backend
- Microservicios Java con **bases de datos separadas**
- `order-service`
  - API REST
  - Publica eventos `order.placed` (v1 exacta)
  - Persiste en `restaurant_db`
- `kitchen-worker`
  - Consumer AMQP
  - Proyecta órdenes en `kitchen_db`
  - NO expone endpoints HTTP

### Comunicación
- REST → solo frontend ↔ order-service
- AMQP → order-service → RabbitMQ → kitchen-worker
- ❌ Prohibido: llamadas REST directas entre servicios

---

## 3. Reglas de Negocio CRÍTICAS (NO ROMPER)

### Validaciones
- `tableId` debe ser entero positivo entre 1 y 12
- Un pedido debe tener **al menos un ítem**
- Todos los `productId` deben existir y estar activos (`is_active = true`)

### Estados
- Nuevas órdenes siempre inician en `PENDING`
- Estados válidos: `PENDING`, `IN_PREPARATION`, `READY`
- ⚠️ El backend **no valida la secuencia** actualmente
- El frontend valida transiciones vía `orderStatus.ts`

### Eventos
- Evento `order.placed`:
  - `eventVersion` debe ser exactamente `1`
  - Otro valor → **DLQ sin reintentos**
- El kitchen-worker es **idempotente**
  - Si la orden no existe, la crea (upsert)

---

## 4. Seguridad (EXTREMADAMENTE SENSIBLE)

- La cocina se autentica vía header `X-Kitchen-Token`
- Token actual: `cocina123` (⚠️ embebido en frontend)
- Implementación: **Chain of Responsibility**
  - `KitchenEndpointScopeHandler`
  - `KitchenTokenPresenceHandler`
  - `KitchenTokenValueHandler`
- ❌ NO introducir Spring Security, JWT o OAuth sin requerimiento explícito

Copilot debe:
- Señalar riesgos de seguridad
- NO proponer cambios disruptivos sin contexto
- Preferir mitigaciones documentadas antes que refactors grandes

---

## 5. Estilo de Código — Backend (Java / Spring)

- Java 17
- Spring Boot 3.2.x
- Lombok permitido (pero no abusar)
- Preferir:
  - Métodos pequeños
  - Nombres explícitos
  - Inmutabilidad cuando sea posible

### Capas (respetar estrictamente)
- controller → solo HTTP
- application → casos de uso
- domain → lógica pura
- infrastructure → detalles técnicos
- repository → acceso a datos

❌ Prohibido:
- Lógica de negocio en controllers
- Acceso directo a repositorios desde controllers
- Acoplar AMQP al dominio

---

## 6. Estilo de Código — Frontend (React + TypeScript)

- React 18 + TypeScript estricto
- Vite como bundler
- TailwindCSS para estilos
- TanStack Query para datos remotos

### Arquitectura
- `pages/` → vistas
- `components/` → UI reutilizable
- `domain/` → reglas de negocio frontend
- `api/` → contratos HTTP
- `store/` → estado global (Context API)

❌ Prohibido:
- Lógica de negocio en componentes UI
- Hardcodear estados fuera de `orderStatus.ts`
- Duplicar contratos que ya existen en `contracts.ts`

---

## 7. Testing (MANDATORIO)

### Enfoque
- **TDD obligatorio**
- Tests antes del código
- El historial de commits debe reflejar RED → GREEN → REFACTOR

### Backend
- JUnit 5
- Mockito
- H2 para tests
- spring-rabbit-test para AMQP
- jqwik para property-based testing (cuando aplique)

### Frontend
- Tests enfocados en lógica y estados
- Evitar tests frágiles de UI pura

Copilot debe:
- Proponer tests primero
- Diseñar casos usando:
  - Partición de equivalencia
  - Valores límite
  - Tablas de decisión si hay lógica compleja

---

## 8. Uso Permitido de Copilot

Copilot puede:
- Generar boilerplate
- Sugerir tests
- Explicar código heredado
- Proponer refactors **incrementales**

Copilot NO debe:
- Reescribir módulos completos
- Cambiar contratos existentes
- Introducir nuevas dependencias sin justificación
- Asumir conocimiento fuera del repositorio

---

## 9. Manejo de Incertidumbre

Si falta información:
- Declarar supuestos explícitamente
- Marcar dudas para el `HANDOVER_REPORT.md`
- NO inventar comportamiento del sistema

---

## 10. Objetivo Final

El objetivo NO es:
❌ maximizar cambios  
❌ modernizar todo  
❌ “arreglar” el sistema  

El objetivo es:
✅ entender  
✅ extender sin romper  
✅ asegurar calidad  
✅ documentar correctamente  

Copilot debe comportarse como **ingeniero senior en contexto de handover**.
