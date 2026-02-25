# Guía de Uso: Flujo de Desarrollo con Docker Optimizado

## ✅ Mejoras Implementadas

### 1. **Optimización de Dockerfiles con Layer Caching**
Todos los Dockerfiles de backend ahora cachean las dependencias de Maven en una capa separada:

**Beneficio**: Los rebuilds ahora toman **~15-30 segundos** en lugar de 2-5 minutos cuando solo cambias código (sin cambiar `pom.xml`).

#### Cómo funciona:
```dockerfile
# Layer 1: Copiar POMs (se cachea si pom.xml no cambia)
COPY pom.xml .
COPY order-service/pom.xml order-service/pom.xml
# ...

# Layer 2: Descargar dependencias (CACHEADO, no se re-ejecuta)
RUN mvn -pl order-service -am dependency:go-offline

# Layer 3: Copiar código fuente (invalida esta capa en cada cambio)
COPY order-service/src order-service/src

# Layer 4: Compilar (usa dependencias cacheadas)
RUN mvn -pl order-service -am package -o
```

Cuando cambias un archivo `.java`, solo las capas 3 y 4 se rebuilding → **mucho más rápido**.

---

### 2. **docker-compose.dev.yml para Hot-Reload del Frontend**
Nuevo archivo de override para desarrollo que habilita hot-reload instantáneo en el frontend.

**Beneficio**: Los cambios en React/TypeScript se reflejan **inmediatamente** sin rebuild.

#### Qué hace:
- Monta los directorios `src/`, `public/`, y archivos de configuración como volúmenes
- Habilita polling de archivos para Docker en Windows/WSL
- Vite HMR funciona instantáneamente

---

### 3. **vite.config.ts Optimizado para Docker**
Configuración actualizada para soportar hot-reload dentro de contenedores:

```typescript
server: {
  host: '0.0.0.0',      // Permite conexiones externas
  watch: {
    usePolling: true,   // Detecta cambios en Docker/WSL
    interval: 1000,
  }
}
```

---

### 4. **Scripts Helper Actualizados**
Los scripts `docker-helper.ps1` y `docker-helper.sh` ahora soportan modo desarrollo:

```powershell
# Modo desarrollo (frontend hot-reload)
.\scripts\docker-helper.ps1 dev up -d

# Modo producción (comportamiento original)
.\scripts\docker-helper.ps1 up -d --build
```

---

## 🚀 Modos de Uso

### Opción A: Modo Desarrollo (RECOMENDADO para frontend)

**Cuándo usar**: Cuando estás trabajando principalmente en el frontend.

```powershell
# Windows
.\scripts\docker-helper.ps1 dev up -d --build

# Linux/Mac
./scripts/docker-helper.sh dev up -d --build
```

**O directamente**:
```bash
docker compose -f infrastructure/docker/docker-compose.yml -f infrastructure/docker/docker-compose.dev.yml up -d --build
```

#### Flujo de trabajo:
1. **Primera vez**: Ejecuta el comando anterior (con `--build`)
2. **Editar frontend**: Los cambios aparecen **instantaneamente** (Vite HMR)
3. **Editar backend**: Ejecuta rebuild del servicio específico:
   ```powershell
   docker compose -f infrastructure/docker/docker-compose.yml up -d --build order-service
   ```
   ⏱️ Tiempo: ~15-30 segundos (gracias a layer caching)

**Pros**:
- ✅ Frontend hot-reload instantáneo
- ✅ Backend optimizado (rebuilds rápidos)
- ✅ Todo corre en Docker (no requiere Node/Java localmente)

**Contras**:
- ⚠️ Backend aún requiere rebuild manual (pero es rápido)

---

### Opción B: Modo Producción (Sin Hot-Reload)

**Cuándo usar**: Para pruebas de integración, despliegues, o cuando no necesitas hot-reload.

```powershell
# Windows - rebuild todo
.\scripts\docker-helper.ps1 up -d --build

# Windows - rebuild un servicio específico
docker compose -f infrastructure/docker/docker-compose.yml up -d --build kitchen-worker

# Linux/Mac
./scripts/docker-helper.sh up -d --build
```

#### Flujo de trabajo:
1. Hacer cambios en código
2. Ejecutar rebuild (con `--build`)
3. Esperar ~15-30s por servicio (frontend ~30-40s)
4. Los cambios están disponibles

**Pros**:
- ✅ Configuración simple
- ✅ Imagen final optimizada
- ✅ Rebuilds mucho más rápidos que antes

**Contras**:
- ⚠️ Sin hot-reload (hay que rebuilding manualmente)

---

### Opción C: Desarrollo Local sin Docker (Máxima Velocidad)

**Cuándo usar**: Cuando necesitas la máxima velocidad de iteración y tienes las herramientas instaladas localmente.

**Requisitos**: Java 17 + Maven + Node.js instalados.

```bash
# 1. Iniciar solo infraestructura en Docker
docker compose -f infrastructure/docker/docker-compose.yml up -d postgres kitchen-postgres report-postgres rabbitmq

# 2. Backend (terminales separadas)
cd order-service
mvn spring-boot:run

cd kitchen-worker
mvn spring-boot:run

cd report-service
mvn spring-boot:run

# 3. Frontend
npm install
npm run dev
```

**Configurar variables de entorno** en archivos `application.yml` o `.env`:
- `spring.datasource.url=jdbc:postgresql://localhost:5432/restaurant_db`
- `spring.rabbitmq.host=localhost`

**Pros**:
- ✅ **Hot-reload instantáneo** para backend y frontend
- ✅ Debugger completo del IDE
- ✅ Máxima velocidad de iteración

**Contras**:
- ❌ Requiere herramientas instaladas localmente
- ⚠️ Gestión manual de variables de entorno
- ⚠️ No cumple el requisito de "funciona en cualquier computador sin instalar nada"

---

## 📊 Comparación de Tiempos

| Escenario | Antes | Después | Mejora |
|-----------|-------|---------|--------|
| **Build inicial completo** | ~10-15 min | ~5-8 min | 40-50% más rápido |
| **Rebuild backend** (cambio en código Java) | ~2-5 min | **~15-30 seg** | **90% más rápido** ⚡ |
| **Rebuild frontend** (cambio en React) | ~1-2 min | **~30-40 seg** | 60% más rápido |
| **Frontend en modo dev** (hot-reload) | ~1-2 min rebuild | **Instantáneo** 🚀 | Sin rebuild |

---

## 🛠️ Comandos Útiles

### Detener todos los contenedores
```bash
docker compose -f infrastructure/docker/docker-compose.yml down
```

### Ver logs en tiempo real
```bash
# Todos los servicios
docker compose -f infrastructure/docker/docker-compose.yml logs -f

# Un servicio específico
docker compose -f infrastructure/docker/docker-compose.yml logs -f order-service
docker compose -f infrastructure/docker/docker-compose.yml logs -f frontend
```

### Rebuild forzado de un servicio específico
```bash
# Backend
docker compose -f infrastructure/docker/docker-compose.yml up -d --build --no-deps order-service

# Frontend (sin hot-reload)
docker compose -f infrastructure/docker/docker-compose.yml up -d --build --no-deps frontend
```

### Rebuild forzado completo (limpiando caché)
```bash
docker compose -f infrastructure/docker/docker-compose.yml build --no-cache
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

### Verificar imágenes y tamaños
```bash
docker images | grep restaurant
```

---

## 🐛 Troubleshooting

### Problema: Frontend hot-reload no funciona en modo dev

**Síntomas**: Los cambios en `src/` no se reflejan en el navegador.

**Soluciones**:
1. Verifica que estás usando `docker-compose.dev.yml`:
   ```bash
   docker compose -f infrastructure/docker/docker-compose.yml -f infrastructure/docker/docker-compose.dev.yml ps
   ```
2. Verifica los logs del frontend:
   ```bash
   docker compose -f infrastructure/docker/docker-compose.yml -f infrastructure/docker/docker-compose.dev.yml logs -f frontend
   ```
   Deberías ver mensajes de Vite HMR.
3. Verifica que el volumen está montado:
   ```bash
   docker inspect restaurant-frontend | grep -A 10 Mounts
   ```

### Problema: Backend rebuild sigue siendo lento (~2-5 min)

**Síntomas**: Después de optimizar Dockerfiles, los rebuilds siguen tomando mucho tiempo.

**Posibles causas**:
1. **Cambiaste `pom.xml`**: Esto invalida el caché de dependencias → rebuild completo es esperado
2. **Docker está descargando las imágenes base**: La primera vez puede ser lento
3. **Disco lleno o lento**: Verifica espacio con `docker system df`

**Soluciones**:
```bash
# Limpiar imágenes antiguas
docker system prune -a

# Verificar que la capa de dependencias está cacheada
docker history restaurant-order-service:latest
```

### Problema: "Cannot connect to Docker daemon"

**Solución**: Asegúrate de que Docker Desktop está corriendo.

```powershell
# Windows
docker version

# Si falla, inicia Docker Desktop
```

### Problema: Puerto 5432/5672 ya está en uso

**Causa**: Postgres o RabbitMQ corriendo localmente fuera de Docker.

**Solución**:
```powershell
# Windows: Detener servicios locales
Stop-Service postgresql-x64-15

# O cambiar los puertos en docker-compose.yml
ports:
  - "15432:5432"  # PostgreSQL en puerto 15432 en el host
```

---

## 📚 Referencias

- [Documentación de diagnóstico técnico](DOCKER_HOT_RELOAD_DIAGNOSTIC.md)
- [Docker Layer Caching](https://docs.docker.com/build/cache/)
- [Vite Server Options](https://vitejs.dev/config/server-options.html)
- [Maven Dependency Plugin](https://maven.apache.org/plugins/maven-dependency-plugin/go-offline-mojo.html)

---

## 🎯 Recomendación del Equipo

**Para desarrollo diario**: Usa **Opción A (Modo Desarrollo)**
```powershell
.\scripts\docker-helper.ps1 dev up -d --build
```

**Razones**:
- Frontend hot-reload instantáneo ↔ mayoría del tiempo se pasa en UI
- Backend rebuilds rápidos cuando sea necesario ↔ ~15-30s es aceptable
- Todo en Docker ↔ funciona en cualquier computador sin configuración local
- Consistencia con producción ↔ mismo entorno base

**Para CI/CD y producción**: Usa **Opción B (Modo Producción)** sin `docker-compose.dev.yml`
