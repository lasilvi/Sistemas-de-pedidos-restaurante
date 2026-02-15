# Evidencia de Implementacion - H-ALTA-05

## Defecto
`H-ALTA-05 - Seguridad de cocina no aplicada de extremo a extremo`

## Resumen de solucion
Se implemento **Chain of Responsibility (comportamiento)** en backend para control de acceso a endpoints de cocina, y se reforzo frontend con login, guard de ruta y uso de token de sesion.

## Commits relacionados
- `1ae9d51` `refactor: implementado Chain of Responsibility para seguridad de cocina en frontend y order-service`
- `6b19f81` `test: agregar cobertura para interceptor CoR de cocina y respuesta 401`
- `86efffc` `docs: documentar aciertos existentes para H-ALTA-05 en AUDITORIA`

## Archivos principales modificados
- `order-service/src/main/java/com/restaurant/orderservice/security/KitchenSecurityHandler.java`
- `order-service/src/main/java/com/restaurant/orderservice/security/AbstractKitchenSecurityHandler.java`
- `order-service/src/main/java/com/restaurant/orderservice/security/KitchenEndpointScopeHandler.java`
- `order-service/src/main/java/com/restaurant/orderservice/security/KitchenTokenPresenceHandler.java`
- `order-service/src/main/java/com/restaurant/orderservice/security/KitchenTokenValueHandler.java`
- `order-service/src/main/java/com/restaurant/orderservice/security/KitchenSecurityInterceptor.java`
- `order-service/src/main/java/com/restaurant/orderservice/exception/KitchenAccessDeniedException.java`
- `order-service/src/main/java/com/restaurant/orderservice/config/WebConfig.java`
- `order-service/src/main/java/com/restaurant/orderservice/exception/GlobalExceptionHandler.java`
- `order-service/src/main/resources/application.yml`
- `.env.example`
- `src/pages/kitchen/KitchenLoginPage.tsx`
- `src/components/RequireKitchenAuth.tsx`
- `src/App.tsx`
- `src/api/http.ts`
- `src/pages/kitchen/KitchenBoardPage.tsx`
- `order-service/src/test/java/com/restaurant/orderservice/security/KitchenSecurityInterceptorTest.java`
- `order-service/src/test/java/com/restaurant/orderservice/exception/GlobalExceptionHandlerTest.java`
- `AUDITORIA.md`

## Evidencia funcional
- Endpoints criticos de cocina protegidos por cadena:
  - `GET /orders`
  - `PATCH /orders/{id}/status`
- Cadena aplicada:
  1. Scope del endpoint.
  2. Presencia de token.
  3. Validez de token.
- Falla de seguridad retorna `401 Unauthorized` (`KitchenAccessDeniedException`).
- Frontend:
  - Login de cocina exige PIN.
  - Guarda token en sesion.
  - Ruta `/kitchen/board` protegida con guard.
  - Si backend responde `401`, se limpia token y se redirige a login.

## Aciertos documentados
Se registro en `AUDITORIA.md`:
- `src/store/kitchenAuth.ts` ya proveia sesion de token.
- `src/api/http.ts` ya soportaba header de token por request.
- `src/api/env.ts` ya tenia configuracion de header/PIN/token.
