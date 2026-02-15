# Refactor H-ALTA-01: Aplicación de SRP en OrderService

**Fecha:** 12 de febrero de 2026  
**Hallazgo:** H-ALTA-01 - OrderService con responsabilidades mezcladas y riesgo N+1  
**Rama:** `feature/refactor-order-service-srp`  
**Estado:** COMPLETADO

---

## Problema Identificado

`OrderService` violaba el **Single Responsibility Principle (SRP)** al tener 6+ responsabilidades:
1. Validación de negocio
2. Persistencia de datos
3. Mapeo DTO ↔ Entity
4. Publicación de eventos
5. Construcción de eventos
6. Orquestación

**Problema adicional:** N+1 query problem en `mapToOrderItemResponse()` que ejecutaba una query por cada item del pedido.

---

## ¿Por qué SRP y no otro patrón?

### Análisis de Alternativas

| Patrón | ¿Por qué NO? |
|--------|--------------|
| **Facade** | Solo oculta complejidad, no separa responsabilidades. No resuelve el problema de fondo. |
| **Strategy** | Es para algoritmos intercambiables, no para separar concerns diferentes. |
| **Template Method** | No necesitamos variaciones de un algoritmo, necesitamos separación de responsabilidades. |
| **Decorator** | Agrega comportamiento dinámicamente, no separa responsabilidades existentes. |

### ¿Por qué SRP es el más conveniente?

1. **Causa raíz**: El problema es tener múltiples razones para cambiar en una sola clase
2. **Testabilidad**: Cada clase se testea independientemente sin mocks complejos
3. **Mantenibilidad**: Cambios localizados en una sola clase
4. **Reutilización**: Validadores y mappers reutilizables en otros contextos
5. **Claridad**: Cada clase tiene un propósito claro y único
6. **Resuelve N+1**: Al separar el mapper, podemos optimizar queries sin afectar lógica de negocio

---

## Solución Implementada

### Clases Creadas

#### 1. OrderValidator
**Responsabilidad única:** Validación de reglas de negocio

```java
@Component
public class OrderValidator {
    public void validateCreateOrderRequest(CreateOrderRequest request) {
        validateTableId(request.getTableId());
        validateItemsList(request.getItems());
        validateProducts(request.getItems());
    }
}
```

**Beneficios:**
- Testeable sin Spring Context
- Reutilizable en otros servicios
- Fácil agregar nuevas validaciones

---

#### 2. OrderMapper
**Responsabilidad única:** Mapeo Entity ↔ DTO

```java
@Component
public class OrderMapper {
    public OrderResponse mapToOrderResponse(Order order) {
        // Batch load all products to avoid N+1 query problem
        List<Long> productIds = order.getItems().stream()
                .map(OrderItem::getProductId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Product> productsMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        
        // Map items using pre-loaded products
        // ...
    }
}
```

**Beneficios:**
- **Resuelve N+1**: Batch loading de productos
- Performance mejorada: 1 query en lugar de N+1
- Testeable con datos mock
- Fácil cambiar estrategia de mapeo

**Optimización N+1:**
- **Antes:** 1 pedido con 10 items = 11 queries (1 + 10)
- **Después:** 1 pedido con 10 items = 2 queries (1 + 1 batch)

---

#### 3. OrderEventBuilder
**Responsabilidad única:** Construcción de eventos

```java
@Component
public class OrderEventBuilder {
    public OrderPlacedEvent buildOrderPlacedEvent(Order order) {
        // Construye evento desde entidad
    }
}
```

**Beneficios:**
- Facilita evolución del schema de eventos
- Testeable independientemente
- Centraliza lógica de construcción de eventos

---

#### 4. OrderService (Refactorizado)
**Responsabilidad única:** Orquestación

```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderValidator orderValidator;
    private final OrderMapper orderMapper;
    private final OrderEventBuilder orderEventBuilder;
    private final OrderEventPublisher orderEventPublisher;
    
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. Delegar validación
        orderValidator.validateCreateOrderRequest(request);
        
        // 2. Crear y persistir entidad
        Order savedOrder = orderRepository.save(order);
        
        // 3. Delegar construcción y publicación de evento
        OrderPlacedEvent event = orderEventBuilder.buildOrderPlacedEvent(savedOrder);
        orderEventPublisher.publishOrderPlacedEvent(event);
        
        // 4. Delegar mapeo
        return orderMapper.mapToOrderResponse(savedOrder);
    }
}
```

**Beneficios:**
- Código limpio y legible
- Fácil entender el flujo
- Cada dependencia tiene un propósito claro
- Testeable con mocks simples

---

## Métricas de Mejora

### Antes del Refactor
- **Líneas de código:** 280
- **Métodos públicos:** 8
- **Métodos privados:** 3
- **Dependencias:** 3
- **Responsabilidades:** 6+
- **Complejidad ciclomática:** Alta
- **N+1 queries:** Sí (1 + N)

### Después del Refactor
- **OrderService líneas:** ~120 (reducción 57%)
- **Métodos públicos:** 4 (solo orquestación)
- **Métodos privados:** 0
- **Dependencias:** 5 (especializadas)
- **Responsabilidades:** 1 (orquestación)
- **Complejidad ciclomática:** Baja
- **N+1 queries:** No (1 + 1 batch)

### Clases Especializadas
- **OrderValidator:** ~60 líneas, 1 responsabilidad
- **OrderMapper:** ~80 líneas, 1 responsabilidad
- **OrderEventBuilder:** ~40 líneas, 1 responsabilidad

---

## Impacto en Testing

### Antes
```java
@Test
void testCreateOrder() {
    // Necesita: DB, RabbitMQ, ProductRepository mock
    // Difícil aislar validación de persistencia
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(orderRepository.save(any())).thenReturn(savedOrder);
    // Lógica mezclada, difícil de testear
}
```

### Después
```java
@Test
void testOrderValidator() {
    // Solo necesita ProductRepository mock
    // Test unitario puro de validación
    validator.validateCreateOrderRequest(request);
}

@Test
void testOrderMapper() {
    // Solo necesita ProductRepository mock
    // Test de mapeo aislado con batch loading
    OrderResponse response = mapper.mapToOrderResponse(order);
}

@Test
void testOrderService() {
    // Mocks simples de componentes especializados
    when(orderValidator.validateCreateOrderRequest(request)).thenReturn();
    when(orderMapper.mapToOrderResponse(order)).thenReturn(response);
    // Test de orquestación limpio
}
```

### Actualización de Tests

**OrderServiceTest actualizado:**
- ✅ Agregados mocks para `OrderValidator`, `OrderMapper`, `OrderEventBuilder`
- ✅ Tests actualizados para verificar delegación correcta
- ✅ Eliminadas dependencias directas de `ProductRepository` (ahora en `OrderValidator`)
- ✅ 11 tests de `OrderServiceTest` pasando
- ✅ 5 tests de `OrderControllerTest` pasando
- ✅ Total: 16/16 tests relacionados con OrderService pasando

**Tests nuevos para componentes especializados:**
- ✅ **OrderValidatorTest** (10 tests): Validación de reglas de negocio
  - Validación de tableId (null, cero, negativo, válido)
  - Validación de items (null, vacío, válido)
  - Validación de productos (inexistente, inactivo, múltiples)
- ✅ **OrderMapperTest** (8 tests): Mapeo Entity↔DTO con optimización N+1
  - Mapeo de items individuales y múltiples
  - Verificación de batch loading (prevención N+1)
  - Deduplicación de productos
  - Manejo de productos faltantes
- ✅ **OrderEventBuilderTest** (7 tests): Construcción de eventos
  - Eventos con items individuales y múltiples
  - Preservación de metadata
  - Mantenimiento del orden de items

**Resumen de cobertura:**
- Tests totales relacionados con refactor: 41/41 ✅
- Tests de componentes especializados: 25/25 ✅
- Tests de integración (OrderService + Controller): 16/16 ✅

**Cambios principales:**
```java
// Antes: OrderService hacía validación directamente
when(productRepository.findById(1L)).thenReturn(Optional.of(product));

// Después: OrderService delega a OrderValidator
doNothing().when(orderValidator).validateCreateOrderRequest(request);
when(orderMapper.mapToOrderResponse(savedOrder)).thenReturn(expectedResponse);
verify(orderValidator).validateCreateOrderRequest(request);
verify(orderMapper).mapToOrderResponse(savedOrder);
```

---

## Compatibilidad

✅ **Sin breaking changes**
- API pública de `OrderService` sin cambios
- Contratos de entrada/salida idénticos
- Tests existentes siguen funcionando

---

## Próximos Pasos

1. ✅ Compilación exitosa
2. ✅ Tests unitarios actualizados y pasando (16/16)
3. ✅ Tests para nuevas clases agregados y pasando (25/25)
   - OrderValidatorTest: 10 tests
   - OrderMapperTest: 8 tests
   - OrderEventBuilderTest: 7 tests
4. ⏳ Verificar performance en entorno de pruebas
5. ⏳ Code review
6. ⏳ Merge a `develop`

---

## Conclusión

El refactor aplicando SRP ha logrado:
- ✅ Separación clara de responsabilidades
- ✅ Resolución del problema N+1
- ✅ Mejora en testabilidad
- ✅ Reducción de complejidad
- ✅ Código más mantenible y extensible
- ✅ Sin breaking changes

**Hallazgo H-ALTA-01:** RESUELTO ✅
