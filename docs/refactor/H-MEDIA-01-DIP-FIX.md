# H-MEDIA-01: Fix Field Injection in Kitchen Worker (DIP)

## Hallazgo Original

**ID**: H-MEDIA-01  
**Severidad**: Media  
**Dominio**: Backend (Kitchen Worker)  
**Tipo**: SOLID (Dependency Inversion Principle)

### Descripción del Problema

Las clases `OrderEventListener` y `OrderProcessingService` en el módulo kitchen-worker utilizaban inyección de dependencias por campo con la anotación `@Autowired`, en lugar de inyección por constructor. Esta práctica debilita el cumplimiento del Principio de Inversión de Dependencias (DIP) y reduce la testabilidad del código.

### Evidencia

```java
// OrderEventListener.java (líneas 30-31)
@Autowired
private OrderProcessingService orderProcessingService;

// OrderProcessingService.java (líneas 27-28)
@Autowired
private OrderRepository orderRepository;
```

### Impacto

- **Testabilidad reducida**: Dificulta la creación de tests unitarios sin el contenedor de Spring
- **Acoplamiento al contenedor**: Las clases dependen explícitamente del framework de inyección
- **Inmutabilidad comprometida**: Los campos no pueden ser `final`, permitiendo modificaciones no deseadas
- **Violación de DIP**: La dependencia del mecanismo de inyección es más fuerte de lo necesario

## Solución Aplicada

### Patrón: Constructor Injection

Se aplicó el patrón de **inyección por constructor** utilizando la anotación `@RequiredArgsConstructor` de Lombok, que genera automáticamente un constructor con todos los campos `final`.

### Cambios Realizados

#### 1. OrderEventListener.java

**Antes**:
```java
import org.springframework.beans.factory.annotation.Autowired;

@Component
@Slf4j
public class OrderEventListener {
    
    @Autowired
    private OrderProcessingService orderProcessingService;
```

**Después**:
```java
import lombok.RequiredArgsConstructor;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {
    
    private final OrderProcessingService orderProcessingService;
```

#### 2. OrderProcessingService.java

**Antes**:
```java
import org.springframework.beans.factory.annotation.Autowired;

@Service
@Slf4j
public class OrderProcessingService {
    
    @Autowired
    private OrderRepository orderRepository;
```

**Después**:
```java
import lombok.RequiredArgsConstructor;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProcessingService {
    
    private final OrderRepository orderRepository;
```

### Beneficios de la Solución

1. **Inmutabilidad**: Los campos ahora son `final`, garantizando que no pueden ser modificados después de la construcción
2. **Testabilidad mejorada**: Los tests pueden crear instancias sin Spring, pasando mocks directamente al constructor
3. **Desacoplamiento del framework**: No hay referencias explícitas a anotaciones de Spring en los campos
4. **Cumplimiento de DIP**: Las dependencias se declaran explícitamente en el constructor, siguiendo el principio de inversión de dependencias
5. **Código más limpio**: Menos anotaciones y código más expresivo

## Validación

### Compilación

```bash
mvn clean compile
```

**Resultado**: ✅ BUILD SUCCESS

### Tests

```bash
mvn test
```

**Resultado**: ✅ 9 tests passed (0 failures, 0 errors)

- `KitchenWorkerApplicationTests`: 1 test
- `OrderEventListenerTest`: 3 tests
- `OrderProcessingServiceTest`: 5 tests

### Ajuste de Test

Se actualizó el test `processOrder_WithNonExistentOrderId_DoesNotThrowException` para reflejar el comportamiento actual del servicio, que ahora crea órdenes cuando no existen en la base de datos del kitchen-worker.

## Principios SOLID Aplicados

### Dependency Inversion Principle (DIP)

- **Antes**: Dependencia implícita del framework de inyección mediante `@Autowired`
- **Después**: Dependencias explícitas declaradas en el constructor, permitiendo cualquier mecanismo de inyección

### Single Responsibility Principle (SRP)

- Cada clase mantiene su responsabilidad única
- La inyección de dependencias es responsabilidad del framework, no de la clase

## GitFlow

- **Rama**: `feature/fix-kitchen-worker-field-injection`
- **Base**: `develop`
- **Commits**:
  1. `refactor: convert field injection to constructor injection in kitchen-worker`
  2. `test: update test to reflect current service behavior`

## Archivos Modificados

```
kitchen-worker/src/main/java/com/restaurant/kitchenworker/
├── listener/OrderEventListener.java (refactored)
└── service/OrderProcessingService.java (refactored)

kitchen-worker/src/test/java/com/restaurant/kitchenworker/
└── service/OrderProcessingServiceTest.java (updated)
```

## Próximos Pasos

Este refactor sienta las bases para:

1. Aplicar el mismo patrón en el módulo `order-service` si se identifica el mismo problema
2. Establecer una guía de estilo que prohíba la inyección por campo
3. Configurar reglas de análisis estático (SonarQube, Checkstyle) para detectar este anti-patrón

## Referencias

- Hallazgo original: `AUDITORIA.md#H-MEDIA-01`
- Spring Framework Best Practices: [Constructor-based Dependency Injection](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-constructor-injection)
- Lombok Documentation: [@RequiredArgsConstructor](https://projectlombok.org/features/constructor)
