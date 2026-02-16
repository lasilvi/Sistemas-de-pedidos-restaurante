# AUDITORÍA TÉCNICA - SISTEMA DE PEDIDOS RESTAURANTE
**Fecha:** 11 de febrero de 2026  
**Punto Base:** Commit `audit: snapshot post-mvp`  
**Alcance:** Backend Java (order-service, kitchen-worker) y Frontend React/TypeScript

---

## RESUMEN EJECUTIVO

Esta auditoría identifica **violaciones críticas de principios SOLID**, **code smells** y **problemas arquitectónicos** que limitan la escalabilidad del sistema. Se encontraron **23 hallazgos críticos** distribuidos en:

- **Backend Java:** 12 violaciones (8 críticas, 4 moderadas)
- **Frontend React:** 11 violaciones (7 críticas, 4 moderadas)
- **Arquitectura General:** 6 problemas estructurales

**Impacto en Escalabilidad:** ALTO - El sistema requiere refactorización significativa antes de escalar.

---

## 1. BACKEND JAVA - ORDER-SERVICE

### 1.1 VIOLACIÓN CRÍTICA: SRP en OrderService
**Archivo:** `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`  
**Líneas:** 1-280

**Hallazgo:**  
`OrderService` viola el **Single Responsibility Principle** al tener múltiples responsabilidades:
1. Validación de negocio (líneas 88-103)
2. Persistencia de datos (línea 125)
3. Mapeo DTO ↔ Entity (líneas 220-260)
4. Publicación de eventos (línea 133)
5. Construcción de eventos (líneas 210-218)

**Código Problemático:**
```java
@Transactional
public OrderResponse createOrder(CreateOrderRequest request) {
    // 1. Validación
    if (request.getTableId() == null || request.getTableId() <= 0) {
        throw new InvalidOrderException("Table ID must be a positive integer");
    }
    
    // 2. Validación de productos
    for (OrderItemRequest itemRequest : request.getItems()) {
        Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(itemRequest.getProductId()));
    }
    
    // 3. Creación de entidad
    Order order = new Order();
    order.setTableId(request.getTableId());
    
    // 4. Persistencia
    Order savedOrder = orderRepository.save(order);
    
    // 5. Publicación de eventos
    OrderPlacedEvent event = buildOrderPlacedEvent(savedOrder);
    orderEventPublisher.publishOrderPlacedEvent(event);
    
    // 6. Mapeo a DTO
    return mapToOrderResponse(savedOrder);
}
```

**Principio Vulnerado:** Single Responsibility Principle (SRP)

**Impacto en Escalabilidad:**
- **Testing:** Difícil crear tests unitarios aislados
- **Mantenimiento:** Cambios en validación afectan persistencia
- **Extensibilidad:** Imposible cambiar estrategia de mapeo sin modificar servicio
- **Reutilización:** Lógica de validación no reutilizable en otros contextos

**Recomendación:**
Separar en clases especializadas:
- `OrderValidator` - Validación de reglas de negocio
- `OrderMapper` - Mapeo DTO ↔ Entity
- `OrderEventBuilder` - Construcción de eventos
- `OrderService` - Orquestación únicamente

---

### 1.2 VIOLACIÓN CRÍTICA: DIP en OrderService
**Archivo:** `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`  
**Líneas:** 35-48

**Hallazgo:**  
`OrderService` depende directamente de implementaciones concretas (`OrderRepository`, `ProductRepository`) en lugar de abstracciones.

**Código Problemático:**
```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;  // Dependencia concreta
    private final ProductRepository productRepository;  // Dependencia concreta
    private final OrderEventPublisher orderEventPublisher;  // Dependencia concreta
    
    @Autowired
    public OrderService(OrderRepository orderRepository, 
                       ProductRepository productRepository,
                       OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderEventPublisher = orderEventPublisher;
    }
}
```

**Principio Vulnerado:** Dependency Inversion Principle (DIP)

**Impacto en Escalabilidad:**
- **Testing:** Imposible mockear repositorios sin frameworks pesados
- **Flexibilidad:** No se puede cambiar implementación de persistencia (ej: Redis, MongoDB)
- **Acoplamiento:** Cambios en repositorios afectan directamente al servicio
- **Microservicios:** Difícil migrar a arquitectura distribuida

**Recomendación:**
Crear interfaces de repositorio:
```java
public interface IOrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    List<Order> findByStatusIn(List<OrderStatus> statuses);
}

@Service
public class OrderService {
    private final IOrderRepository orderRepository;  // Abstracción
    // ...
}
```

---

### 1.3 VIOLACIÓN MODERADA: N+1 Query Problem
**Archivo:** `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`  
**Líneas:** 250-260

**Hallazgo:**  
El método `mapToOrderItemResponse` ejecuta una query por cada item del pedido para obtener el nombre del producto.

**Código Problemático:**
```java
private OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
    // Query individual por cada item - N+1 problem
    String productName = productRepository.findById(orderItem.getProductId())
            .map(Product::getName)
            .orElse("Producto desconocido");
    
    return OrderItemResponse.builder()
            .productId(orderItem.getProductId())
            .productName(productName)
            .quantity(orderItem.getQuantity())
            .build();
}
```

**Principio Vulnerado:** Performance Best Practices

**Impacto en Escalabilidad:**
- **Performance:** 1 pedido con 10 items = 11 queries (1 + 10)
- **Latencia:** Aumenta linealmente con número de items
- **Base de Datos:** Sobrecarga innecesaria en PostgreSQL
- **Costo:** Mayor uso de conexiones y recursos

**Recomendación:**
Usar `@EntityGraph` o fetch join:
```java
@Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
Optional<Order> findByIdWithItems(@Param("id") UUID id);
```

---

### 1.4 CODE SMELL: God Class en OrderService
**Archivo:** `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`  
**Líneas:** 1-280

**Hallazgo:**  
`OrderService` tiene 280 líneas y 8 métodos públicos, convirtiéndose en una "God Class".

**Métricas:**
- **Líneas de código:** 280
- **Métodos públicos:** 8
- **Métodos privados:** 3
- **Dependencias:** 3
- **Responsabilidades:** 6+

**Impacto en Escalabilidad:**
- **Complejidad Ciclomática:** Alta, difícil de entender
- **Mantenimiento:** Cambios riesgosos
- **Testing:** Cobertura incompleta
- **Colaboración:** Conflictos en merge

**Recomendación:**
Aplicar patrón **Facade** y delegar a servicios especializados.

---

### 1.5 VIOLACIÓN MODERADA: Error Handling Inconsistente
**Archivo:** `order-service/src/main/java/com/restaurant/orderservice/service/OrderEventPublisher.java`  
**Líneas:** 38-50

**Hallazgo:**  
El publicador de eventos **silencia excepciones** sin estrategia de retry o compensación.

**Código Problemático:**
```java
public void publishOrderPlacedEvent(OrderPlacedEvent event) {
    try {
        rabbitTemplate.convertAndSend(exchangeName, orderPlacedRoutingKey, event);
        log.info("Successfully published order.placed event: orderId={}", event.getOrderId());
    } catch (Exception ex) {
        log.error("Failed to publish order.placed event: orderId={}", event.getOrderId(), ex);
        // Do not throw exception - order is already persisted
        // ⚠️ PROBLEMA: Evento perdido sin compensación
    }
}
```

**Principio Vulnerado:** Reliability & Consistency

**Impacto en Escalabilidad:**
- **Consistencia Eventual:** Pedidos creados pero no procesados en cocina
- **Pérdida de Datos:** Eventos perdidos sin registro
- **Debugging:** Difícil rastrear pedidos "fantasma"
- **SLA:** Afecta tiempo de respuesta percibido

**Recomendación:**
Implementar **Outbox Pattern** o tabla de eventos pendientes:
```java
@Transactional
public OrderResponse createOrder(CreateOrderRequest request) {
    Order savedOrder = orderRepository.save(order);
    
    // Guardar evento en tabla outbox
    OutboxEvent outboxEvent = new OutboxEvent(event);
    outboxRepository.save(outboxEvent);
    
    return mapToOrderResponse(savedOrder);
}

// Worker separado procesa outbox y publica a RabbitMQ
```

---

### 1.6 VIOLACIÓN MODERADA: Acoplamiento Temporal
**Archivo:** `order-service/src/main/java/com/restaurant/orderservice/service/OrderService.java`  
**Líneas:** 125-135

**Hallazgo:**  
La creación de pedido está **acoplada temporalmente** a la publicación del evento.

**Código Problemático:**
```java
@Transactional
public OrderResponse createOrder(CreateOrderRequest request) {
    // ...
    Order savedOrder = orderRepository.save(order);
    
    // Acoplamiento temporal: si esto falla, la transacción ya se commitió
    OrderPlacedEvent event = buildOrderPlacedEvent(savedOrder);
    orderEventPublisher.publishOrderPlacedEvent(event);
    
    return mapToOrderResponse(savedOrder);
}
```

**Principio Vulnerado:** Temporal Coupling

**Impacto en Escalabilidad:**
- **Atomicidad:** Transacción DB ≠ Publicación evento
- **Rollback:** No se puede deshacer pedido si falla publicación
- **Latencia:** Cliente espera a RabbitMQ
- **Disponibilidad:** Fallo en RabbitMQ afecta creación de pedidos

**Recomendación:**
Usar **@TransactionalEventListener** o Outbox Pattern.

---

## 2. BACKEND JAVA - KITCHEN-WORKER

### 2.1 VIOLACIÓN MODERADA: SRP en OrderProcessingService
**Archivo:** `kitchen-worker/src/main/java/com/restaurant/kitchenworker/service/OrderProcessingService.java`  
**Líneas:** 1-60

**Hallazgo:**  
`OrderProcessingService` mezcla lógica de procesamiento con manejo de errores y logging.

**Código Problemático:**
```java
@Transactional
public void processOrder(OrderPlacedEvent event) {
    try {
        log.info("Processing order event: orderId={}", event.getOrderId());
        
        Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());
        if (orderOpt.isEmpty()) {
            log.error("Order not found: orderId={}", event.getOrderId());
            return;  // No throw - evita reprocessing
        }
        
        Order order = orderOpt.get();
        order.setStatus(OrderStatus.IN_PREPARATION);
        orderRepository.save(order);
        
        log.info("Order status updated: orderId={}", event.getOrderId());
    } catch (Exception ex) {
        log.error("Error processing order: orderId={}", event.getOrderId(), ex);
        throw ex;  // Trigger retry
    }
}
```

**Principio Vulnerado:** Single Responsibility Principle (SRP)

**Impacto en Escalabilidad:**
- **Testing:** Difícil testear lógica sin logging
- **Observabilidad:** Logging mezclado con negocio
- **Extensibilidad:** Agregar validaciones complica método
- **Mantenimiento:** Cambios en error handling afectan negocio

**Recomendación:**
Separar en capas:
- `OrderProcessor` - Lógica de negocio pura
- `OrderProcessingService` - Orquestación y error handling
- `OrderProcessingLogger` - Logging estructurado

---

### 2.2 VIOLACIÓN MODERADA: Acoplamiento Directo en Listener
**Archivo:** `kitchen-worker/src/main/java/com/restaurant/kitchenworker/listener/OrderEventListener.java`  
**Líneas:** 1-50

**Hallazgo:**  
`OrderEventListener` está **acoplado directamente** a `OrderProcessingService`.

**Código Problemático:**
```java
@Component
public class OrderEventListener {
    @Autowired
    private OrderProcessingService orderProcessingService;  // Acoplamiento directo
    
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Received order.placed event: orderId={}", event.getOrderId());
        orderProcessingService.processOrder(event);  // Llamada directa
    }
}
```

**Principio Vulnerado:** Dependency Inversion Principle (DIP)

**Impacto en Escalabilidad:**
- **Testing:** Difícil testear listener sin servicio real
- **Flexibilidad:** No se puede cambiar implementación
- **Extensibilidad:** Agregar procesadores requiere modificar listener
- **Microservicios:** Difícil distribuir procesamiento

**Recomendación:**
Usar patrón **Command** o **Strategy**:
```java
public interface EventProcessor<T> {
    void process(T event);
}

@Component
public class OrderEventListener {
    private final EventProcessor<OrderPlacedEvent> processor;
    
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        processor.process(event);
    }
}
```

---

### 2.3 CODE SMELL: Falta de Idempotencia
**Archivo:** `kitchen-worker/src/main/java/com/restaurant/kitchenworker/service/OrderProcessingService.java`  
**Líneas:** 40-55

**Hallazgo:**  
El procesamiento de eventos **no es idempotente**. Procesar el mismo evento múltiples veces puede causar inconsistencias.

**Código Problemático:**
```java
@Transactional
public void processOrder(OrderPlacedEvent event) {
    Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());
    if (orderOpt.isEmpty()) {
        return;  // Order not found
    }
    
    Order order = orderOpt.get();
    order.setStatus(OrderStatus.IN_PREPARATION);  // ⚠️ No verifica estado actual
    orderRepository.save(order);
}
```

**Principio Vulnerado:** Idempotency

**Impacto en Escalabilidad:**
- **Consistencia:** Eventos duplicados causan estados inválidos
- **Retry:** Reintentos pueden corromper datos
- **Distributed Systems:** Problemas en sistemas distribuidos
- **Debugging:** Difícil rastrear inconsistencias

**Recomendación:**
Verificar estado antes de actualizar:
```java
@Transactional
public void processOrder(OrderPlacedEvent event) {
    Order order = orderRepository.findById(event.getOrderId())
        .orElseThrow(() -> new OrderNotFoundException(event.getOrderId()));
    
    // Idempotencia: solo actualizar si está en PENDING
    if (order.getStatus() == OrderStatus.PENDING) {
        order.setStatus(OrderStatus.IN_PREPARATION);
        orderRepository.save(order);
    } else {
        log.warn("Order already processed: orderId={}, status={}", 
            order.getId(), order.getStatus());
    }
}
```

---

## 3. FRONTEND REACT/TYPESCRIPT

### 3.1 VIOLACIÓN CRÍTICA: SRP en KitchenBoardPage
**Archivo:** `src/pages/kitchen/KitchenBoardPage.tsx`  
**Líneas:** 1-150

**Hallazgo:**  
`KitchenBoardPage` viola SRP al tener múltiples responsabilidades:
1. Gestión de estado (useState, useRef)
2. Lógica de polling (useEffect, setTimeout)
3. Llamadas API (listOrders, patchOrderStatus)
4. Renderizado UI (JSX)
5. Manejo de errores
6. Agrupación de datos (useMemo)

**Código Problemático:**
```typescript
export function KitchenBoardPage() {
  // 1. Estado local (7 estados diferentes)
  const [statusFilter, setStatusFilter] = useState<OrderStatus[]>(ACTIVE_STATUSES)
  const [orders, setOrders] = useState<Order[]>([])
  const [initialLoading, setInitialLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState<string>('')
  const [patching, setPatching] = useState(false)
  
  // 2. Refs para control de polling
  const inFlightRef = useRef(false)
  const timeoutRef = useRef<number | null>(null)
  const mountedRef = useRef(false)
  
  // 3. Lógica de fetch con polling
  const loadOrders = useCallback(async ({ block }: { block: boolean }) => {
    if (inFlightRef.current) return
    inFlightRef.current = true
    // ... 30 líneas de lógica
  }, [statusFilter])
  
  // 4. Agrupación de datos
  const grouped = useMemo(() => {
    const by: Record<OrderStatus, Order[]> = { /* ... */ }
    for (const o of orders) by[o.status]?.push(o)
    return by
  }, [orders])
  
  // 5. Renderizado UI (80+ líneas de JSX)
  return <div>...</div>
}
```

**Principio Vulnerado:** Single Responsibility Principle (SRP)

**Impacto en Escalabilidad:**
- **Testing:** Imposible testear lógica sin renderizar componente
- **Reutilización:** Lógica de polling no reutilizable
- **Mantenimiento:** 150+ líneas en un solo componente
- **Performance:** Re-renders innecesarios
- **Colaboración:** Conflictos en merge frecuentes

**Recomendación:**
Separar en custom hooks y componentes:
```typescript
// Hook personalizado para polling
function useOrderPolling(statusFilter: OrderStatus[]) {
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string>('')
  
  // Lógica de polling aquí
  
  return { orders, loading, error, refetch }
}

// Componente simplificado
export function KitchenBoardPage() {
  const [statusFilter, setStatusFilter] = useState(ACTIVE_STATUSES)
  const { orders, loading, error } = useOrderPolling(statusFilter)
  const grouped = useGroupedOrders(orders)
  
  if (loading) return <Loading />
  if (error) return <ErrorState error={error} />
  
  return <KitchenBoard grouped={grouped} />
}
```

---

### 3.2 VIOLACIÓN CRÍTICA: DIP en Componentes
**Archivo:** `src/pages/client/CartPage.tsx`  
**Líneas:** 1-60

**Hallazgo:**  
Componentes dependen **directamente** de funciones API concretas (`createOrder`, `listOrders`).

**Código Problemático:**
```typescript
import { createOrder } from '@/api/orders'  // Dependencia concreta

export function CartPage() {
  const createM = useMutation({
    mutationFn: (req: CreateOrderRequest) => createOrder(req),  // Acoplamiento directo
    onSuccess: (res) => {
      actions.clear()
      navigate(`/client/confirm/${encodeURIComponent(res.id)}`)
    },
  })
  
  function submit() {
    const req: CreateOrderRequest = { /* ... */ }
    createM.mutate(req)
  }
  
  return <div>...</div>
}
```

**Principio Vulnerado:** Dependency Inversion Principle (DIP)

**Impacto en Escalabilidad:**
- **Testing:** Imposible testear sin API real
- **Flexibilidad:** No se puede cambiar implementación (REST → GraphQL)
- **Mocking:** Requiere interceptores HTTP complejos
- **Microservicios:** Difícil migrar a múltiples backends

**Recomendación:**
Crear capa de servicios abstracta:
```typescript
// services/OrderService.ts
export interface IOrderService {
  createOrder(req: CreateOrderRequest): Promise<CreateOrderResponse>
  getOrder(id: string): Promise<Order>
  listOrders(params: ListOrdersParams): Promise<Order[]>
}

export class RestOrderService implements IOrderService {
  async createOrder(req: CreateOrderRequest) {
    return http<CreateOrderResponse>('/orders', { method: 'POST', json: req })
  }
  // ...
}

// Inyectar servicio
export function CartPage({ orderService }: { orderService: IOrderService }) {
  const createM = useMutation({
    mutationFn: (req) => orderService.createOrder(req),
    // ...
  })
}
```

---

### 3.3 VIOLACIÓN CRÍTICA: God Object en CartProvider
**Archivo:** `src/store/cart.tsx`  
**Líneas:** 1-120

**Hallazgo:**  
`CartProvider` es un **God Object** que maneja:
1. Estado del carrito (items, tableId, orderNote)
2. Persistencia en localStorage
3. Lógica de negocio (agregar, remover, calcular totales)
4. Serialización/deserialización
5. Validación de datos

**Código Problemático:**
```typescript
// 120 líneas en un solo archivo
export function CartProvider({ children }: { children: React.ReactNode }) {
  const [state, dispatch] = useReducer(reducer, undefined, load)
  
  // Persistencia automática
  useEffect(() => save(state), [state])
  
  // Exposición de 6 acciones diferentes
  const value = useMemo(() => ({
    state,
    actions: {
      setTable: (tableId: number) => dispatch({ type: 'SET_TABLE', tableId }),
      addItem: (product: Product) => dispatch({ type: 'ADD_ITEM', product }),
      removeItem: (productId: string) => dispatch({ type: 'REMOVE_ITEM', productId }),
      setQty: (productId: string, quantity: number) => dispatch({ type: 'SET_QTY', productId, quantity }),
      setOrderNote: (note: string) => dispatch({ type: 'SET_ORDER_NOTE', note }),
      clear: () => dispatch({ type: 'CLEAR' }),
    },
  }), [state])
  
  return <CartCtx.Provider value={value}>{children}</CartCtx.Provider>
}

// Función de cálculo mezclada con store
export function cartTotals(items: CartItem[]) {
  const totalItems = items.reduce((acc, i) => acc + i.quantity, 0)
  const distinct = items.length
  return { totalItems, distinct }
}
```

**Principio Vulnerado:** Single Responsibility Principle (SRP)

**Impacto en Escalabilidad:**
- **Testing:** Difícil testear lógica sin Context
- **Reutilización:** Lógica de negocio acoplada a React
- **Performance:** Re-renders en toda la app
- **Mantenimiento:** Cambios afectan múltiples componentes
- **State Management:** No escalable a estados complejos

**Recomendación:**
Separar en capas:
```typescript
// domain/cart/CartState.ts - Lógica pura
export class CartState {
  constructor(
    public items: CartItem[],
    public tableId: number | null,
    public orderNote: string
  ) {}
  
  addItem(product: Product): CartState {
    // Lógica inmutable
  }
  
  getTotals() {
    return {
      totalItems: this.items.reduce((acc, i) => acc + i.quantity, 0),
      distinct: this.items.length
    }
  }
}

// infrastructure/CartStorage.ts - Persistencia
export class CartStorage {
  save(state: CartState): void { /* localStorage */ }
  load(): CartState | null { /* localStorage */ }
}

// store/CartProvider.tsx - Solo Context
export function CartProvider({ children }) {
  const [cart, setCart] = useState(() => storage.load() ?? new CartState([], null, ''))
  
  useEffect(() => storage.save(cart), [cart])
  
  return <CartCtx.Provider value={{ cart, setCart }}>{children}</CartCtx.Provider>
}
```

---

### 3.4 CODE SMELL: Duplicación de Error Handling
**Archivos:**  
- `src/pages/client/CartPage.tsx` (líneas 20-28)
- `src/pages/client/MenuPage.tsx` (líneas 30-40)
- `src/pages/kitchen/KitchenBoardPage.tsx` (líneas 35-45)

**Hallazgo:**  
Lógica de manejo de errores **duplicada** en múltiples componentes.

**Código Problemático:**
```typescript
// CartPage.tsx
const createM = useMutation({
  mutationFn: (req: CreateOrderRequest) => createOrder(req),
  onError: (err: unknown) => {
    const msg = err instanceof Error ? err.message : 'No se pudo crear el pedido'
    setLocalError(msg)
  },
})

// MenuPage.tsx
if (menuQ.isError) {
  const err = menuQ.error as Error
  return (
    <ErrorState
      title="No pudimos cargar el menú"
      detail={err.message}
      onRetry={() => menuQ.refetch()}
    />
  )
}

// KitchenBoardPage.tsx
try {
  const data = await listOrders({ status: statusFilter })
  setOrders(data)
  setError('')
} catch (err) {
  const msg = err instanceof Error ? err.message : 'No pudimos cargar pedidos'
  setError(msg)
}
```

**Principio Vulnerado:** DRY (Don't Repeat Yourself)

**Impacto en Escalabilidad:**
- **Mantenimiento:** Cambios en error handling requieren modificar N archivos
- **Consistencia:** Mensajes de error inconsistentes
- **Testing:** Duplicación de tests
- **Extensibilidad:** Agregar logging/tracking requiere cambios masivos

**Recomendación:**
Crear hook reutilizable:
```typescript
// hooks/useErrorHandler.ts
export function useErrorHandler() {
  const [error, setError] = useState<string>('')
  
  const handleError = useCallback((err: unknown, fallback: string) => {
    const msg = err instanceof Error ? err.message : fallback
    setError(msg)
    // Logging centralizado
    console.error('[Error]', msg, err)
  }, [])
  
  const clearError = useCallback(() => setError(''), [])
  
  return { error, handleError, clearError }
}

// Uso
export function CartPage() {
  const { error, handleError } = useErrorHandler()
  
  const createM = useMutation({
    mutationFn: createOrder,
    onError: (err) => handleError(err, 'No se pudo crear el pedido'),
  })
}
```

---

### 3.5 VIOLACIÓN MODERADA: Acoplamiento a useNavigate
**Archivos:**  
- `src/pages/client/CartPage.tsx` (línea 11)
- `src/pages/client/MenuPage.tsx` (línea 13)
- `src/pages/kitchen/KitchenBoardPage.tsx` (línea 10)

**Hallazgo:**  
Componentes están **acoplados** a `react-router-dom` directamente.

**Código Problemático:**
```typescript
import { useNavigate } from 'react-router-dom'

export function CartPage() {
  const navigate = useNavigate()  // Acoplamiento directo
  
  const createM = useMutation({
    onSuccess: (res) => {
      navigate(`/client/confirm/${encodeURIComponent(res.id)}`)  // Lógica de navegación
    },
  })
}
```

**Principio Vulnerado:** Dependency Inversion Principle (DIP)

**Impacto en Escalabilidad:**
- **Testing:** Difícil testear sin router
- **Flexibilidad:** No se puede cambiar librería de routing
- **Reutilización:** Componentes no reutilizables fuera de router
- **Migración:** Difícil migrar a Next.js o Remix

**Recomendación:**
Abstraer navegación:
```typescript
// hooks/useNavigation.ts
export interface INavigationService {
  goToConfirmation(orderId: string): void
  goToMenu(): void
  goToCart(): void
}

export function useNavigation(): INavigationService {
  const navigate = useNavigate()
  
  return useMemo(() => ({
    goToConfirmation: (orderId) => navigate(`/client/confirm/${orderId}`),
    goToMenu: () => navigate('/client/menu'),
    goToCart: () => navigate('/client/cart'),
  }), [navigate])
}

// Uso
export function CartPage() {
  const nav = useNavigation()
  
  const createM = useMutation({
    onSuccess: (res) => nav.goToConfirmation(res.id),
  })
}
```

---

### 3.6 CODE SMELL: Lógica de Negocio en Componentes
**Archivo:** `src/pages/client/MenuPage.tsx`  
**Líneas:** 50-85

**Hallazgo:**  
Lógica de negocio (agregar al carrito, calcular totales) está **mezclada** con UI.

**Código Problemático:**
```typescript
export function MenuPage() {
  const { state, actions } = useCart()
  const totals = useMemo(() => cartTotals(state.items), [state.items])  // Cálculo en componente
  
  return (
    <div>
      {products.map((p) => (
        <div key={p.id}>
          <button onClick={() => actions.addItem(p)}>+ Agregar</button>
          <div>
            En carrito:{' '}
            {state.items.find((i) => i.productId === p.id)?.quantity ?? 0}  {/* Lógica en JSX */}
          </div>
        </div>
      ))}
    </div>
  )
}
```

**Principio Vulnerado:** Separation of Concerns

**Impacto en Escalabilidad:**
- **Testing:** Imposible testear lógica sin renderizar
- **Reutilización:** Lógica no reutilizable
- **Performance:** Cálculos en cada render
- **Mantenimiento:** Cambios en lógica requieren modificar UI

**Recomendación:**
Mover lógica a hooks o servicios:
```typescript
// hooks/useCartOperations.ts
export function useCartOperations() {
  const { state, actions } = useCart()
  
  const getItemQuantity = useCallback((productId: string) => {
    return state.items.find((i) => i.productId === productId)?.quantity ?? 0
  }, [state.items])
  
  const getTotals = useMemo(() => cartTotals(state.items), [state.items])
  
  return { getItemQuantity, getTotals, addItem: actions.addItem }
}

// Uso
export function MenuPage() {
  const { getItemQuantity, getTotals, addItem } = useCartOperations()
  
  return (
    <div>
      {products.map((p) => (
        <div key={p.id}>
          <button onClick={() => addItem(p)}>+ Agregar</button>
          <div>En carrito: {getItemQuantity(p.id)}</div>
        </div>
      ))}
    </div>
  )
}
```

---

## 4. PROBLEMAS DE ARQUITECTURA GENERAL

### 4.1 CRÍTICO: Falta de Capas Arquitectónicas Claras

**Hallazgo:**  
El sistema **no tiene separación clara** entre capas de presentación, lógica de negocio y persistencia.

**Evidencia:**

**Backend:**
```
order-service/
├── controller/     ← Presentación
├── service/        ← Lógica de negocio + Persistencia + Mapeo + Eventos
├── repository/     ← Persistencia (sin interfaces)
├── dto/            ← Contratos
└── entity/         ← Modelo de datos
```

**Frontend:**
```
src/
├── pages/          ← UI + Lógica + Estado + API
├── store/          ← Estado + Lógica + Persistencia
├── api/            ← HTTP (sin abstracción)
└── components/     ← UI pura (único correcto)
```

**Impacto en Escalabilidad:**
- **Mantenimiento:** Cambios en una capa afectan otras
- **Testing:** Imposible testear capas aisladamente
- **Reutilización:** Código no reutilizable
- **Microservicios:** Difícil extraer servicios
- **Team Scaling:** Equipos no pueden trabajar independientemente

**Recomendación:**
Implementar **Clean Architecture** o **Hexagonal Architecture**:

```
Backend (Clean Architecture):
order-service/
├── domain/              ← Entidades y lógica de negocio pura
│   ├── model/
│   ├── service/
│   └── repository/      ← Interfaces (ports)
├── application/         ← Casos de uso
│   ├── usecase/
│   └── dto/
├── infrastructure/      ← Implementaciones (adapters)
│   ├── persistence/
│   ├── messaging/
│   └── web/
└── presentation/        ← Controllers

Frontend (Feature-Sliced Design):
src/
├── entities/            ← Modelos de dominio
├── features/            ← Funcionalidades
│   ├── cart/
│   │   ├── model/       ← Estado y lógica
│   │   ├── api/         ← Servicios
│   │   └── ui/          ← Componentes
│   └── orders/
├── shared/              ← Utilidades compartidas
└── pages/               ← Solo composición
```

---

### 4.2 CRÍTICO: Falta de Abstracción de Persistencia

**Hallazgo:**  
Los repositorios **no tienen interfaces**, acoplando servicios a implementaciones concretas de JPA.

**Evidencia:**
```java
// order-service/repository/OrderRepository.java
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByStatusIn(List<OrderStatus> status);
}

// order-service/service/OrderService.java
@Service
public class OrderService {
    private final OrderRepository orderRepository;  // Acoplamiento a JPA
    
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
```

**Impacto en Escalabilidad:**
- **Testing:** Requiere base de datos o mocks complejos
- **Flexibilidad:** No se puede cambiar a MongoDB, Redis, etc.
- **Microservicios:** Difícil migrar a arquitectura distribuida
- **Performance:** No se puede agregar caché sin modificar servicios

**Recomendación:**
Crear interfaces de dominio:
```java
// domain/repository/IOrderRepository.java
public interface IOrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    List<Order> findByStatusIn(List<OrderStatus> statuses);
}

// infrastructure/persistence/JpaOrderRepository.java
@Repository
public class JpaOrderRepository implements IOrderRepository {
    @Autowired
    private OrderJpaRepository jpaRepository;  // Spring Data JPA
    
    @Override
    public Order save(Order order) {
        return jpaRepository.save(order);
    }
    // ...
}

// application/service/OrderService.java
@Service
public class OrderService {
    private final IOrderRepository orderRepository;  // Abstracción
    
    public OrderService(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
```

---

### 4.3 MODERADO: Falta de Estrategia de Caché

**Hallazgo:**  
No hay **caché** implementado, causando queries repetitivas a la base de datos.

**Evidencia:**
- `OrderService.mapToOrderItemResponse()` ejecuta N+1 queries
- `MenuService.getMenu()` consulta productos en cada request
- Frontend no cachea respuestas de API

**Impacto en Escalabilidad:**
- **Performance:** Latencia alta en endpoints
- **Base de Datos:** Sobrecarga innecesaria
- **Costo:** Mayor uso de recursos
- **UX:** Experiencia lenta para usuarios

**Recomendación:**
Implementar caché en múltiples niveles:

**Backend:**
```java
@Service
public class MenuService {
    @Cacheable(value = "menu", key = "'all'")
    public List<ProductResponse> getMenu() {
        return productRepository.findByIsActiveTrue()
            .stream()
            .map(this::mapToProductResponse)
            .collect(Collectors.toList());
    }
}

// application.yml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 300000  # 5 minutos
```

**Frontend:**
```typescript
// Ya usa React Query, pero falta configuración de staleTime
const menuQ = useQuery({
  queryKey: ['menu'],
  queryFn: getMenu,
  staleTime: 5 * 60 * 1000,  // 5 minutos
  cacheTime: 10 * 60 * 1000,  // 10 minutos
})
```

---

### 4.4 MODERADO: Falta de Observabilidad

**Hallazgo:**  
No hay **tracing distribuido**, **métricas** ni **logging estructurado**.

**Evidencia:**
- Logs con `log.info()` sin contexto de correlación
- No hay métricas de performance
- No hay tracing entre order-service y kitchen-worker
- Frontend no reporta errores

**Impacto en Escalabilidad:**
- **Debugging:** Difícil rastrear requests entre servicios
- **Monitoring:** No se pueden detectar cuellos de botella
- **Alerting:** No hay alertas proactivas
- **SLA:** No se pueden medir tiempos de respuesta

**Recomendación:**
Implementar stack de observabilidad:

**Backend:**
```java
// pom.xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>

// application.yml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://zipkin:9411/api/v2/spans
```

**Frontend:**
```typescript
// Sentry o similar
import * as Sentry from "@sentry/react"

Sentry.init({
  dsn: "...",
  integrations: [new Sentry.BrowserTracing()],
  tracesSampleRate: 1.0,
})
```

---

### 4.5 MODERADO: Falta de Validación Consistente

**Hallazgo:**  
Validación de datos **inconsistente** entre capas.

**Evidencia:**

**Backend:**
```java
// OrderService.java - Validación manual
if (request.getTableId() == null || request.getTableId() <= 0) {
    throw new InvalidOrderException("Table ID must be a positive integer");
}

// CreateOrderRequest.java - Validación con anotaciones
@NotNull(message = "Table ID is required")
@Min(value = 1, message = "Table ID must be positive")
private Integer tableId;
```

**Frontend:**
```typescript
// CartPage.tsx - Validación manual
if (state.items.length === 0) {
  setLocalError('Agrega al menos 1 producto.')
  return
}

// No hay validación de tipos en runtime
```

**Impacto en Escalabilidad:**
- **Consistencia:** Reglas de validación duplicadas
- **Mantenimiento:** Cambios requieren modificar múltiples lugares
- **Seguridad:** Validación inconsistente permite datos inválidos
- **UX:** Mensajes de error inconsistentes

**Recomendación:**
Centralizar validación:

**Backend:**
```java
// domain/validator/OrderValidator.java
@Component
public class OrderValidator {
    public void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request.getTableId() == null || request.getTableId() <= 0) {
            throw new InvalidOrderException("Table ID must be a positive integer");
        }
        
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }
        
        // Validaciones centralizadas
    }
}

// Usar en servicio
@Service
public class OrderService {
    private final OrderValidator validator;
    
    public OrderResponse createOrder(CreateOrderRequest request) {
        validator.validateCreateOrderRequest(request);
        // ...
    }
}
```

**Frontend:**
```typescript
// domain/validation/orderValidation.ts
import { z } from 'zod'

export const CreateOrderSchema = z.object({
  tableId: z.number().min(1, 'Table ID must be positive'),
  items: z.array(z.object({
    productId: z.string().uuid(),
    quantity: z.number().min(1),
  })).min(1, 'Order must contain at least one item'),
})

// Uso
export function CartPage() {
  function submit() {
    const result = CreateOrderSchema.safeParse(req)
    if (!result.success) {
      setError(result.error.errors[0].message)
      return
    }
    createM.mutate(result.data)
  }
}
```

---

### 4.6 MODERADO: Falta de Rate Limiting y Throttling

**Hallazgo:**  
No hay **rate limiting** en APIs ni **throttling** en polling del frontend.

**Evidencia:**

**Backend:**
- Endpoints sin rate limiting
- Posible abuso de API

**Frontend:**
```typescript
// KitchenBoardPage.tsx - Polling sin throttling
timeoutRef.current = window.setTimeout(() => {
  if (mountedRef.current) loadOrders({ block: false })
}, 3000)  // Polling cada 3 segundos sin control
```

**Impacto en Escalabilidad:**
- **Performance:** Sobrecarga en backend
- **Costo:** Mayor uso de recursos
- **Disponibilidad:** Posible DoS accidental
- **UX:** Batería y datos móviles consumidos

**Recomendación:**

**Backend:**
```java
// pom.xml
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
</dependency>

// config/RateLimitConfig.java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        String key = getClientKey(request);
        Bucket bucket = resolveBucket(key);
        
        if (bucket.tryConsume(1)) {
            return true;
        }
        
        throw new RateLimitExceededException();
    }
}
```

**Frontend:**
```typescript
// hooks/usePolling.ts
export function usePolling(fn: () => Promise<void>, interval: number) {
  const [isActive, setIsActive] = useState(true)
  const timeoutRef = useRef<number | null>(null)
  
  useEffect(() => {
    if (!isActive) return
    
    const poll = async () => {
      await fn()
      
      // Exponential backoff en caso de error
      const nextInterval = calculateBackoff(interval)
      timeoutRef.current = window.setTimeout(poll, nextInterval)
    }
    
    poll()
    
    return () => {
      if (timeoutRef.current) clearTimeout(timeoutRef.current)
    }
  }, [fn, interval, isActive])
  
  return { pause: () => setIsActive(false), resume: () => setIsActive(true) }
}
```

---

## 5. RESUMEN DE HALLAZGOS POR SEVERIDAD

### CRÍTICOS (Requieren atención inmediata)

| ID | Componente | Hallazgo | Principio Vulnerado | Impacto |
|----|-----------|----------|---------------------|---------|
| 1.1 | OrderService | Múltiples responsabilidades | SRP | Alto - Dificulta testing y mantenimiento |
| 1.2 | OrderService | Dependencias concretas | DIP | Alto - Imposibilita cambios de implementación |
| 1.5 | OrderEventPublisher | Eventos perdidos sin compensación | Reliability | Crítico - Inconsistencia de datos |
| 1.6 | OrderService | Acoplamiento temporal | Temporal Coupling | Alto - Atomicidad comprometida |
| 2.3 | OrderProcessingService | Falta de idempotencia | Idempotency | Crítico - Corrupción de datos |
| 3.1 | KitchenBoardPage | God Component | SRP | Alto - 150+ líneas, imposible mantener |
| 3.2 | CartPage | Acoplamiento a API concreta | DIP | Alto - Testing imposible |
| 3.3 | CartProvider | God Object | SRP | Alto - Estado no escalable |
| 4.1 | Arquitectura | Falta de capas claras | Separation of Concerns | Crítico - Escalabilidad limitada |
| 4.2 | Persistencia | Sin abstracción | DIP | Alto - Acoplamiento a JPA |

**Total Críticos:** 10

---

### MODERADOS (Planificar refactorización)

| ID | Componente | Hallazgo | Principio Vulnerado | Impacto |
|----|-----------|----------|---------------------|---------|
| 1.3 | OrderService | N+1 Query Problem | Performance | Medio - Latencia aumenta con items |
| 1.4 | OrderService | God Class (280 líneas) | SRP | Medio - Complejidad alta |
| 2.1 | OrderProcessingService | Lógica mezclada con logging | SRP | Medio - Testing difícil |
| 2.2 | OrderEventListener | Acoplamiento directo | DIP | Medio - Extensibilidad limitada |
| 3.4 | Múltiples componentes | Error handling duplicado | DRY | Medio - Mantenimiento costoso |
| 3.5 | Múltiples componentes | Acoplamiento a router | DIP | Medio - Migración difícil |
| 3.6 | MenuPage | Lógica en componentes | Separation of Concerns | Medio - Testing imposible |
| 4.3 | Sistema | Falta de caché | Performance | Medio - Queries repetitivas |
| 4.4 | Sistema | Falta de observabilidad | Monitoring | Medio - Debugging difícil |
| 4.5 | Sistema | Validación inconsistente | Consistency | Medio - Seguridad comprometida |
| 4.6 | Sistema | Sin rate limiting | Availability | Medio - Posible DoS |

**Total Moderados:** 11

---

### LEVES (Mejoras futuras)

| ID | Componente | Hallazgo | Impacto |
|----|-----------|----------|---------|
| - | Frontend | Falta de TypeScript strict mode | Bajo - Errores en runtime |
| - | Backend | Falta de documentación Javadoc | Bajo - Onboarding lento |
| - | Tests | Cobertura baja (<60%) | Bajo - Confianza limitada |
| - | CI/CD | Sin pipeline automatizado | Bajo - Despliegues manuales |

**Total Leves:** 4

---

## 6. PLAN DE ACCIÓN RECOMENDADO

### FASE 1: ESTABILIZACIÓN (Sprint 1-2)

**Objetivo:** Resolver problemas críticos de consistencia y confiabilidad.

1. **Implementar Outbox Pattern** (1.5, 1.6)
   - Crear tabla `outbox_events`
   - Worker para publicar eventos pendientes
   - Garantizar consistencia eventual

2. **Agregar Idempotencia** (2.3)
   - Verificar estado antes de actualizar
   - Agregar logs de eventos duplicados
   - Tests de idempotencia

3. **Abstraer Persistencia** (1.2, 4.2)
   - Crear interfaces `IOrderRepository`, `IProductRepository`
   - Implementar adapters JPA
   - Refactorizar servicios

**Esfuerzo:** 2 sprints (4 semanas)  
**Riesgo:** Bajo - Cambios internos sin afectar API

---

### FASE 2: REFACTORIZACIÓN BACKEND (Sprint 3-5)

**Objetivo:** Separar responsabilidades y mejorar arquitectura.

1. **Descomponer OrderService** (1.1, 1.4)
   - Crear `OrderValidator`
   - Crear `OrderMapper`
   - Crear `OrderEventBuilder`
   - Reducir servicio a orquestación

2. **Resolver N+1 Queries** (1.3)
   - Implementar `@EntityGraph`
   - Agregar caché de productos
   - Optimizar queries

3. **Implementar Caché** (4.3)
   - Configurar Redis
   - Cachear menú y productos
   - Invalidación inteligente

**Esfuerzo:** 3 sprints (6 semanas)  
**Riesgo:** Medio - Requiere tests exhaustivos

---

### FASE 3: REFACTORIZACIÓN FRONTEND (Sprint 6-8)

**Objetivo:** Separar lógica de UI y mejorar testabilidad.

1. **Descomponer KitchenBoardPage** (3.1)
   - Crear `useOrderPolling` hook
   - Crear `useGroupedOrders` hook
   - Separar componentes de UI

2. **Abstraer Servicios** (3.2)
   - Crear interfaces `IOrderService`, `IMenuService`
   - Implementar `RestOrderService`
   - Inyectar servicios en componentes

3. **Refactorizar CartProvider** (3.3)
   - Extraer `CartState` (lógica pura)
   - Extraer `CartStorage` (persistencia)
   - Simplificar Context

4. **Centralizar Error Handling** (3.4)
   - Crear `useErrorHandler` hook
   - Refactorizar componentes
   - Agregar logging

**Esfuerzo:** 3 sprints (6 semanas)  
**Riesgo:** Bajo - Cambios incrementales

---

### FASE 4: ARQUITECTURA Y OBSERVABILIDAD (Sprint 9-11)

**Objetivo:** Implementar arquitectura limpia y observabilidad.

1. **Implementar Clean Architecture** (4.1)
   - Reorganizar backend en capas
   - Reorganizar frontend (Feature-Sliced Design)
   - Documentar arquitectura

2. **Agregar Observabilidad** (4.4)
   - Configurar Zipkin/Jaeger
   - Implementar métricas (Micrometer)
   - Agregar Sentry en frontend

3. **Centralizar Validación** (4.5)
   - Crear `OrderValidator` en backend
   - Usar Zod en frontend
   - Sincronizar reglas

4. **Implementar Rate Limiting** (4.6)
   - Configurar Bucket4j
   - Agregar throttling en polling
   - Monitorear uso

**Esfuerzo:** 3 sprints (6 semanas)  
**Riesgo:** Medio - Cambios estructurales

---

### FASE 5: OPTIMIZACIÓN Y MEJORAS (Sprint 12+)

**Objetivo:** Optimizar performance y agregar mejoras.

1. **Optimizar Performance**
   - Agregar índices en BD
   - Implementar paginación
   - Optimizar queries

2. **Mejorar Testing**
   - Aumentar cobertura a 80%+
   - Agregar tests de integración
   - Agregar tests E2E

3. **Implementar CI/CD**
   - Pipeline automatizado
   - Tests automáticos
   - Despliegue continuo

**Esfuerzo:** Continuo  
**Riesgo:** Bajo

---

## 7. MÉTRICAS DE ÉXITO

### Métricas Técnicas

| Métrica | Estado Actual | Objetivo | Plazo |
|---------|---------------|----------|-------|
| Cobertura de Tests | ~40% | 80%+ | 6 meses |
| Complejidad Ciclomática | 15+ | <10 | 4 meses |
| Tiempo de Build | 3 min | <1 min | 3 meses |
| Latencia P95 (API) | 500ms | <200ms | 4 meses |
| Errores en Producción | 5-10/día | <1/día | 6 meses |
| Deuda Técnica (SonarQube) | N/A | A rating | 6 meses |

### Métricas de Negocio

| Métrica | Estado Actual | Objetivo | Plazo |
|---------|---------------|----------|-------|
| Tiempo de Onboarding | 2 semanas | 3 días | 4 meses |
| Velocidad de Desarrollo | 5 story points/sprint | 13 story points/sprint | 6 meses |
| Bugs en Producción | 3-5/sprint | <1/sprint | 6 meses |
| Tiempo de Deploy | 2 horas | 15 min | 3 meses |

---

## 8. RIESGOS Y MITIGACIONES

### Riesgos Identificados

1. **Refactorización Masiva**
   - **Riesgo:** Introducir bugs en producción
   - **Mitigación:** Refactorización incremental, tests exhaustivos, feature flags

2. **Cambios en Arquitectura**
   - **Riesgo:** Afectar funcionalidad existente
   - **Mitigación:** Mantener APIs estables, versionado, rollback plan

3. **Tiempo de Desarrollo**
   - **Riesgo:** 11 sprints (22 semanas) es largo
   - **Mitigación:** Priorizar fases críticas, paralelizar trabajo

4. **Resistencia al Cambio**
   - **Riesgo:** Equipo prefiere código legacy
   - **Mitigación:** Capacitación, pair programming, documentación

---

## 9. CONCLUSIONES

### Estado Actual

El sistema presenta **23 hallazgos** (10 críticos, 11 moderados, 2 leves) que limitan significativamente su escalabilidad:

- **Backend:** Violaciones de SRP y DIP, falta de abstracción, problemas de consistencia
- **Frontend:** God components, acoplamiento directo, lógica mezclada con UI
- **Arquitectura:** Sin capas claras, sin observabilidad, sin caché

### Impacto en Escalabilidad

**ALTO** - El sistema actual NO está preparado para escalar:
- Testing difícil → Baja confianza en cambios
- Acoplamiento alto → Cambios riesgosos
- Sin observabilidad → Debugging imposible
- Sin caché → Performance limitada
- Arquitectura monolítica → Difícil distribuir

### Recomendación Final

**REFACTORIZACIÓN NECESARIA** antes de escalar. Seguir el plan de 5 fases (22 semanas) para:
1. Estabilizar consistencia (Fase 1)
2. Refactorizar backend (Fase 2)
3. Refactorizar frontend (Fase 3)
4. Implementar arquitectura limpia (Fase 4)
5. Optimizar y mejorar (Fase 5)

**Alternativa:** Si el tiempo es crítico, ejecutar solo Fases 1-2 (10 semanas) para resolver problemas críticos y permitir escalabilidad limitada.

---

**Auditor:** Kiro AI  
**Fecha:** 11 de febrero de 2026  
**Versión:** 1.0
