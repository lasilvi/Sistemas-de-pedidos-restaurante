# ✅ Checklist de Verificación del Sistema

Usa este checklist para verificar que todo el sistema esté funcionando correctamente.

## 📋 Pre-requisitos

- [ ] Docker Desktop instalado
- [ ] Docker Desktop corriendo (`docker ps` funciona)
- [ ] Repositorio clonado
- [ ] Archivo `.env` creado (copia de `.env.example`)

## 🐳 Contenedores Docker

Ejecuta: `docker-compose ps`

- [ ] `restaurant-frontend` - Estado: Up
- [ ] `restaurant-order-service` - Estado: Up
- [ ] `restaurant-kitchen-worker` - Estado: Up
- [ ] `restaurant-postgres` - Estado: Up (healthy)
- [ ] `restaurant-rabbitmq` - Estado: Up (healthy)

## 🌐 URLs Accesibles

### Frontend
- [ ] http://localhost:5173 - Muestra pantalla de selección de mesa
- [ ] http://localhost:5173/kitchen - Muestra pantalla de login de cocina

### Backend
- [ ] http://localhost:8080/menu - Devuelve lista de productos (JSON)
- [ ] http://localhost:8080/swagger-ui.html - Muestra documentación Swagger

### Infraestructura
- [ ] http://localhost:15672 - Muestra RabbitMQ Management (login: guest/guest)

## 🧪 Pruebas Funcionales

### Test 1: Menú
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/menu" -Method Get
```
- [ ] Devuelve 3 productos
- [ ] Cada producto tiene: id, name, description

### Test 2: Crear Pedido
```powershell
$body = '{"tableId": 5, "items": [{"productId": 1, "quantity": 2}]}'
Invoke-RestMethod -Uri "http://localhost:8080/orders" -Method Post -Body $body -ContentType "application/json"
```
- [ ] Devuelve pedido con status PENDING
- [ ] Devuelve UUID del pedido
- [ ] Devuelve totalAmount calculado

### Test 3: Procesamiento Asíncrono
```powershell
docker-compose logs kitchen-worker --tail=20
```
- [ ] Muestra mensaje "Received order placed event"
- [ ] Muestra mensaje "Order processed successfully"
- [ ] Muestra "newStatus=IN_PREPARATION"

### Test 4: Consultar Pedido
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/orders/{orderId}" -Method Get
```
- [ ] Devuelve el pedido creado
- [ ] Incluye items con detalles
- [ ] Muestra status actualizado

### Test 5: Frontend Cliente
1. Abre http://localhost:5173
2. Ingresa número de mesa (ej: 5)
3. Haz clic en "Continuar"

- [ ] Muestra página del menú
- [ ] Muestra 3 productos con imágenes
- [ ] Botón "Agregar al Carrito" funciona
- [ ] Contador del carrito se actualiza

### Test 6: Carrito y Pedido
1. Agrega 2 productos al carrito
2. Haz clic en el ícono del carrito
3. Haz clic en "Realizar Pedido"

- [ ] Muestra página de confirmación
- [ ] Muestra ID del pedido
- [ ] Muestra botón "Ver Estado del Pedido"

### Test 7: Frontend Cocina
1. Abre http://localhost:5173/kitchen
2. Ingresa PIN: 1234
3. Haz clic en "Ingresar"

- [ ] Muestra dashboard de cocina
- [ ] Muestra pedidos pendientes
- [ ] Muestra pedidos en preparación
- [ ] Botones de filtro funcionan
- [ ] Botón "Marcar como Listo" funciona

## 📊 RabbitMQ

1. Abre http://localhost:15672
2. Login: guest/guest
3. Ve a la pestaña "Queues"

- [ ] Existe cola `order.placed.queue`
- [ ] Cola tiene mensajes procesados (Ready: 0, Unacked: 0)
- [ ] Exchange `order.exchange` existe
- [ ] Binding entre exchange y cola existe

## 💾 Base de Datos

```powershell
docker exec -it restaurant-postgres psql -U restaurant_user -d restaurant_db -c "SELECT COUNT(*) FROM products;"
```
- [ ] Devuelve 3 productos

```powershell
docker exec -it restaurant-postgres psql -U restaurant_user -d restaurant_db -c "SELECT COUNT(*) FROM orders;"
```
- [ ] Devuelve al menos 1 pedido (si creaste uno)

```powershell
docker exec -it restaurant-postgres psql -U restaurant_user -d restaurant_db -c "SELECT status, COUNT(*) FROM orders GROUP BY status;"
```
- [ ] Muestra distribución de pedidos por estado

## 🔍 Logs Sin Errores

```powershell
docker-compose logs order-service --tail=50
```
- [ ] No hay mensajes de ERROR
- [ ] Muestra "Started OrderServiceApplication"
- [ ] Muestra "Flyway successfully applied X migrations"

```powershell
docker-compose logs kitchen-worker --tail=50
```
- [ ] No hay mensajes de ERROR
- [ ] Muestra "Started KitchenWorkerApplication"
- [ ] Muestra "Created new connection" (RabbitMQ)

```powershell
docker-compose logs frontend --tail=20
```
- [ ] No hay mensajes de ERROR
- [ ] Muestra "VITE ready in XXX ms"

## 🎯 Flujo Completo End-to-End

1. Cliente crea pedido desde frontend
2. Pedido aparece en API con status PENDING
3. Kitchen Worker procesa evento
4. Pedido aparece en cocina con status IN_PREPARATION
5. Cocina marca pedido como READY
6. Cliente ve pedido actualizado

- [ ] Todo el flujo funciona sin errores
- [ ] Estados se actualizan correctamente
- [ ] Frontend muestra cambios en tiempo real

## 📈 Rendimiento

- [ ] Frontend carga en menos de 2 segundos
- [ ] API responde en menos de 500ms
- [ ] Pedidos se procesan en menos de 3 segundos
- [ ] No hay memory leaks visibles en logs

## 🔒 Seguridad Básica

- [ ] PIN de cocina funciona (rechaza PIN incorrecto)
- [ ] API valida datos de entrada
- [ ] No hay credenciales expuestas en logs
- [ ] CORS configurado correctamente

## 📝 Documentación

- [ ] README.md está actualizado
- [ ] SISTEMA_FUNCIONANDO.md está actualizado
- [ ] GUIA_RAPIDA.md existe
- [ ] Swagger UI muestra todos los endpoints
- [ ] Variables de entorno documentadas en .env.example

---

## ✅ Resultado Final

**Total de checks completados**: _____ / 70

### Interpretación:
- **70/70**: ✅ Sistema perfecto, listo para producción
- **60-69**: ⚠️ Sistema funcional, revisar items pendientes
- **50-59**: ⚠️ Sistema parcialmente funcional, requiere atención
- **< 50**: ❌ Sistema con problemas, revisar logs y configuración

---

## 🐛 Si algo falla

1. **Revisa los logs**: `docker-compose logs -f`
2. **Verifica puertos**: `netstat -ano | findstr :8080`
3. **Reinicia servicios**: `docker-compose restart`
4. **Limpia y reconstruye**: `docker-compose down -v && docker-compose up --build`
5. **Consulta documentación**: README.md y SISTEMA_FUNCIONANDO.md

---

**Fecha de verificación**: _____________  
**Verificado por**: _____________  
**Notas adicionales**: _____________
