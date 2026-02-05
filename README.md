# Restaurant Order System

Sistema backend MVP de pedidos de restaurante utilizando arquitectura de microservicios con Spring Boot 3 y Java 17.

## Arquitectura

El sistema consta de dos microservicios que se comunican de forma asíncrona a través de RabbitMQ:

- **Order Service**: API REST para gestión de pedidos (Puerto 8080)
- **Kitchen Worker**: Servicio consumidor que procesa eventos de pedidos (Puerto 8081)

Ambos servicios comparten una base de datos PostgreSQL para persistencia de datos.

## Estructura del Proyecto

```
restaurant-order-system/
├── order-service/          # Microservicio de API REST
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/restaurant/orderservice/
│   │   │   │   └── OrderServiceApplication.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/    # Scripts de migración Flyway
│   │   └── test/
│   └── pom.xml
├── kitchen-worker/         # Microservicio consumidor de eventos
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/restaurant/kitchenworker/
│   │   │   │   └── KitchenWorkerApplication.java
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   └── pom.xml
└── pom.xml                 # POM padre del proyecto multi-módulo
```

## Tecnologías

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Maven**: Gestión de dependencias
- **PostgreSQL**: Base de datos
- **RabbitMQ**: Broker de mensajes
- **Flyway**: Migraciones de base de datos
- **Lombok**: Reducción de boilerplate
- **SpringDoc OpenAPI**: Documentación de API (Swagger)
- **JUnit 5**: Pruebas unitarias
- **jqwik**: Pruebas basadas en propiedades

## Dependencias Principales

### Order Service
- Spring Web
- Spring Data JPA
- Spring AMQP (RabbitMQ)
- Spring Validation
- Flyway Core
- SpringDoc OpenAPI
- PostgreSQL Driver
- Lombok

### Kitchen Worker
- Spring Boot Starter
- Spring Data JPA
- Spring AMQP (RabbitMQ)
- PostgreSQL Driver
- Lombok

### Dependencias de Prueba (Ambos Servicios)
- Spring Boot Test
- Spring Rabbit Test
- jqwik (Property-Based Testing)
- H2 Database (para pruebas)

## Requisitos Previos

- Java 17 o superior
- Maven 3.6 o superior
- PostgreSQL 15 o superior
- RabbitMQ 3.x

## Configuración

### Iniciar PostgreSQL (Docker)

```bash
docker run --name restaurant-postgres \
  -e POSTGRES_DB=restaurant_db \
  -e POSTGRES_USER=restaurant_user \
  -e POSTGRES_PASSWORD=restaurant_pass \
  -p 5432:5432 \
  -d postgres:15
```

### Iniciar RabbitMQ (Docker)

```bash
docker run --name restaurant-rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -d rabbitmq:3-management
```

## Compilación

Compilar todo el proyecto:

```bash
mvn clean install
```

Compilar solo un módulo:

```bash
cd order-service
mvn clean install
```

## Ejecución

### Order Service

```bash
cd order-service
mvn spring-boot:run
```

El servicio estará disponible en: http://localhost:8080

### Kitchen Worker

```bash
cd kitchen-worker
mvn spring-boot:run
```

El servicio estará disponible en: http://localhost:8081

## Documentación de API

Una vez iniciado el Order Service, la documentación Swagger estará disponible en:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Pruebas

Ejecutar todas las pruebas:

```bash
mvn test
```

Ejecutar pruebas de un módulo específico:

```bash
cd order-service
mvn test
```

## Verificación de RabbitMQ

Acceder a la consola de administración de RabbitMQ:

- URL: http://localhost:15672
- Usuario: guest
- Contraseña: guest

## Próximos Pasos

1. Implementar entidades JPA y migraciones Flyway
2. Crear repositorios
3. Implementar servicios y controladores
4. Configurar RabbitMQ
5. Escribir pruebas unitarias y basadas en propiedades

## Notas

- Las migraciones Flyway se ejecutarán automáticamente al iniciar el Order Service
- El Kitchen Worker escuchará eventos de RabbitMQ automáticamente al iniciar
- Para desarrollo, se recomienda usar perfiles de Spring para diferentes entornos
