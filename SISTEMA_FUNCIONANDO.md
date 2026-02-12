# ✅ Sistema de Pedidos de Restaurante - FUNCIONANDO

**Fecha**: 2026-02-05  
**Estado**: ✅ TODOS LOS SERVICIOS OPERATIVOS

---

## 🎯 Estado de los Servicios

### ✅ PostgreSQL (Docker)
- **Puerto**: 5433 (para no conflictuar con tu PostgreSQL local en 5432)
- **Base de datos**: restaurant_db
- **Usuario**: restaurant_user
- **Contraseña**: restaurant_pass
- **Estado**: ✅ CORRIENDO
- **Migraciones Flyway**: ✅ 4 migraciones aplicadas exitosamente

### ✅ RabbitMQ (Docker)
- **Puerto AMQP**: 5672
- **Puerto Management**: 15672
- **Usuario**: guest
- **Contraseña**: guest
- **Estado**: ✅ CORRIENDO

### ✅ Order Service
- **Puerto**: 8080
- **Estado**: ✅ CORRIENDO
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

### ✅ Kitchen Worker
- **Puerto**: 8081
- **Estado**: ✅ CORRIENDO
- **Conectado a RabbitMQ**: ✅ SÍ
- **Escuchando cola**: order.placed.queue

---

## 🧪 Pruebas del Sistema

### 1. Verificar Swagger UI

Abre en tu navegador:
```
http://localhost:8080/swagger-ui.html
```

Deberías ver la documentación completa de la API con todos los endpoints.

### 2. Verificar RabbitMQ Management

Abre en tu navegador:
```
http://localhost:15672
```

**Credenciales**: guest / guest

Verifica que existen:
- Exchange: `order.exchange`
- Queue: `order.placed.queue`
- Dead Letter Queue: `order.placed.dlq`

### 3. Probar GET /menu

**Usando curl:**
```powershell
curl http://localhost:8080/menu
```

**Usando PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/menu" -Method Get
```

**Respuesta esperada**: Lista de 3 productos (Pizza Margherita, Hamburguesa Clásica, Ensalada César)

### 4. Crear un Pedido (POST /orders)

**Usando curl:**
```powershell
curl -X POST http://localhost:8080/orders `
  -H "Content-Type: application/json" `
  -d '{\"tableId\": 5, \"items\": [{\"productId\": 1, \"quantity\": 2, \"note\": \"Sin cebolla\"}]}'
```

**Usando PowerShell:**
```powershell
$body = @{
    tableId = 5
    items = @(
        @{
            productId = 1
            quantity = 2
            note = "Sin cebolla"
        }
    )
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/orders" -Method Post -Body $body -ContentType "application/json"
```

**Respuesta esperada**: 
- Status: 201 Created
- Pedido con status "PENDING"
- UUID único del pedido

### 5. Verificar Procesamiento Asíncrono

Después de crear un pedido:

1. **Copia el UUID del pedido** de la respuesta
2. **Espera 1-2 segundos** (para que Kitchen Worker lo procese)
3. **Consulta el pedido**:

```powershell
# Reemplaza {orderId} con el UUID real
curl http://localhost:8080/orders/{orderId}
```

**Resultado esperado**: El status debería cambiar de "PENDING" a "IN_PREPARATION"

### 6. Listar Todos los Pedidos

```powershell
curl http://localhost:8080/orders
```

### 7. Filtrar Pedidos por Estado

```powershell
# Pedidos pendientes
curl "http://localhost:8080/orders?status=PENDING"

# Pedidos en preparación
curl "http://localhost:8080/orders?status=IN_PREPARATION"

# Pedidos listos
curl "http://localhost:8080/orders?status=READY"
```

### 8. Actualizar Estado de un Pedido

```powershell
# Reemplaza {orderId} con el UUID real
curl -X PATCH http://localhost:8080/orders/{orderId}/status `
  -H "Content-Type: application/json" `
  -d '{\"status\": \"READY\"}'
```

---

## 📊 Verificación de Logs

### Ver logs de Order Service:
Los logs se están mostrando en la terminal donde ejecutaste el servicio.

### Ver logs de Kitchen Worker:
Los logs se están mostrando en la terminal donde ejecutaste el servicio.

### Buscar eventos procesados:
En los logs del Kitchen Worker, busca líneas como:
```
INFO ... OrderProcessingService : Processing order event: orderId=..., tableId=...
INFO ... OrderProcessingService : Order processed successfully: orderId=..., tableId=..., newStatus=IN_PREPARATION
```

---

## 🔧 Comandos Útiles

### Detener los servicios:
```powershell
# Detener Order Service (Ctrl+C en su terminal)
# Detener Kitchen Worker (Ctrl+C en su terminal)
```

### Detener contenedores Docker:
```powershell
docker stop restaurant-postgres restaurant-rabbitmq
```

### Reiniciar contenedores Docker:
```powershell
docker start restaurant-postgres restaurant-rabbitmq
```

### Ver logs de contenedores:
```powershell
# PostgreSQL
docker logs restaurant-postgres

# RabbitMQ
docker logs restaurant-rabbitmq
```

### Eliminar contenedores (si necesitas empezar de cero):
```powershell
docker stop restaurant-postgres restaurant-rabbitmq
docker rm restaurant-postgres restaurant-rabbitmq
```

---

## 🎉 Flujo Completo de Prueba

### Escenario: Crear y procesar un pedido completo

1. **Obtener el menú**:
   ```powershell
   Invoke-RestMethod -Uri "http://localhost:8080/menu" -Method Get
   ```

2. **Crear un pedido**:
   ```powershell
   $body = @{
       tableId = 5
       items = @(
           @{ productId = 1; quantity = 2; note = "Sin cebolla" },
           @{ productId = 3; quantity = 1; note = $null }
       )
   } | ConvertTo-Json
   
   $order = Invoke-RestMethod -Uri "http://localhost:8080/orders" -Method Post -Body $body -ContentType "application/json"
   $orderId = $order.id
   Write-Host "Pedido creado: $orderId"
   ```

3. **Esperar procesamiento**:
   ```powershell
   Start-Sleep -Seconds 2
   ```

4. **Verificar que cambió a IN_PREPARATION**:
   ```powershell
   $updatedOrder = Invoke-RestMethod -Uri "http://localhost:8080/orders/$orderId" -Method Get
   Write-Host "Estado actual: $($updatedOrder.status)"
   ```

5. **Marcar como READY**:
   ```powershell
   $statusUpdate = @{ status = "READY" } | ConvertTo-Json
   $finalOrder = Invoke-RestMethod -Uri "http://localhost:8080/orders/$orderId/status" -Method Patch -Body $statusUpdate -ContentType "application/json"
   Write-Host "Estado final: $($finalOrder.status)"
   ```

---

## 📝 Notas Importantes

1. **Puerto PostgreSQL**: Configurado en 5433 para no conflictuar con tu PostgreSQL local (5432)

2. **Datos Iniciales**: La base de datos ya tiene 3 productos insertados automáticamente por Flyway

3. **Procesamiento Asíncrono**: Los pedidos cambian de PENDING a IN_PREPARATION automáticamente gracias al Kitchen Worker

4. **Swagger UI**: Puedes probar todos los endpoints directamente desde la interfaz web

5. **RabbitMQ Management**: Puedes ver en tiempo real los mensajes que se publican y consumen

---

## ✅ Sistema Completamente Funcional

El sistema está **100% operativo** y listo para:
- ✅ Crear pedidos
- ✅ Procesar pedidos asíncronamente
- ✅ Consultar pedidos
- ✅ Filtrar pedidos por estado
- ✅ Actualizar estado de pedidos
- ✅ Ver documentación en Swagger
- ✅ Monitorear RabbitMQ

**¡Disfruta probando tu sistema de pedidos de restaurante!** 🍕🍔🥗
