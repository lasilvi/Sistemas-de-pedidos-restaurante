# 🚀 Guía Rápida de Inicio

Esta es una guía ultra-rápida para poner en marcha el sistema en menos de 5 minutos.

## ⚡ Inicio Rápido (3 Pasos)

### 1️⃣ Asegúrate de tener Docker Desktop corriendo

```powershell
docker ps
```

Si ves un error, abre Docker Desktop y espera a que inicie.

### 2️⃣ Copia el archivo de configuración

```powershell
Copy-Item .env.example .env
```

### 3️⃣ Inicia todo el sistema

```powershell
docker-compose up -d --build
```

**¡Listo!** Espera 30-60 segundos mientras los servicios se inician.

---

## 🌐 Accede a las Aplicaciones

| Aplicación | URL | Descripción |
|------------|-----|-------------|
| **Frontend Cliente** | http://localhost:5173 | Interfaz para hacer pedidos |
| **Frontend Cocina** | http://localhost:5173/kitchen | Interfaz para gestionar pedidos (PIN: 1234) |
| **API Backend** | http://localhost:8080 | API REST |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Documentación interactiva |
| **RabbitMQ** | http://localhost:15672 | Gestión de colas (guest/guest) |

---

## 🧪 Prueba Rápida

### Desde el Navegador

1. Abre http://localhost:5173
2. Ingresa número de mesa (ej: 5)
3. Agrega productos al carrito
4. Realiza el pedido
5. Ve el estado del pedido

### Desde PowerShell

```powershell
# Ver menú
Invoke-RestMethod -Uri "http://localhost:8080/menu" -Method Get

# Crear pedido
$body = '{"tableId": 5, "items": [{"productId": 1, "quantity": 2}]}'
Invoke-RestMethod -Uri "http://localhost:8080/orders" -Method Post -Body $body -ContentType "application/json"
```

---

## 🛠️ Comandos Útiles

```powershell
# Ver estado de los servicios
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f

# Reiniciar todo
docker-compose restart

# Detener todo
docker-compose down

# Limpiar todo y empezar de cero
docker-compose down -v
docker-compose up -d --build
```

---

## 🐛 Problemas Comunes

### "Cannot connect to Docker"
→ Inicia Docker Desktop

### "Port already in use"
→ Ejecuta: `docker-compose down`

### "Frontend no carga"
→ Espera 30 segundos más, los servicios están iniciando

### "Error 500 en la API"
→ Verifica los logs: `docker-compose logs order-service`

---

## 📚 Documentación Completa

- [README.md](README.md) - Documentación completa del proyecto
- [SISTEMA_FUNCIONANDO.md](SISTEMA_FUNCIONANDO.md) - Guía detallada de verificación

---

**¿Necesitas ayuda?** Revisa la documentación completa o los logs de los servicios.
