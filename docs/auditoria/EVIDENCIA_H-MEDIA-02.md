# Evidencia de Implementacion - H-MEDIA-02

## Hallazgo

- `H-MEDIA-02 - KitchenBoardPage concentra demasiadas responsabilidades`

## Objetivo

Reducir acoplamiento en `KitchenBoardPage` separando:
- orquestacion de flujo de cocina (polling, auth, fetch y mutacion)
- acciones de negocio de UI
- renderizado

## Patrones aplicados

1. `Facade`
- `src/pages/kitchen/KitchenBoardFacade.ts`
- Encapsula autenticacion de cocina, manejo de 401 y navegacion de seguridad.
- Orquesta operaciones de lectura y mutacion para el tablero.

2. `Command`
- `src/pages/kitchen/commands/KitchenCommand.ts`
- `src/pages/kitchen/commands/LoadKitchenOrdersCommand.ts`
- `src/pages/kitchen/commands/ChangeKitchenOrderStatusCommand.ts`
- Encapsula acciones de cocina como comandos ejecutables y testeables por separado.

## Cambios por archivo

1. `src/pages/kitchen/KitchenBoardPage.tsx`
- Se simplifica a componente de presentacion.
- Se delega la logica de datos y transiciones al controlador.
- Se elimina logica directa de fetch/mutacion/autenticacion desde el render.

2. `src/pages/kitchen/useKitchenBoardController.ts`
- Nuevo controlador (hook) para ciclo de vida del tablero:
  - polling
  - manejo de estados de carga y error
  - refresco post-mutacion

3. `src/pages/kitchen/KitchenBoardFacade.ts`
- Nueva fachada para coordinar comandos y reglas de seguridad.
- Maneja token ausente/expirado y redireccion a login.

4. `src/pages/kitchen/commands/KitchenCommand.ts`
- Contrato comun de comandos para la capa de cocina.

5. `src/pages/kitchen/commands/LoadKitchenOrdersCommand.ts`
- Comando de consulta de pedidos por filtro de estado.

6. `src/pages/kitchen/commands/ChangeKitchenOrderStatusCommand.ts`
- Comando de transicion de estado de pedido.

## Aciertos existentes preservados

- Control anti-solapamiento de polling (`inFlightRef`).
- Limpieza de timeout al desmontar para evitar fugas.
- Agrupacion memoizada de pedidos por estado.

## Resultado

- Se reduce el acoplamiento del componente de vista.
- Se mejora mantenibilidad al separar responsabilidades por capa.
- Se conserva el flujo funcional de la pantalla de cocina.
