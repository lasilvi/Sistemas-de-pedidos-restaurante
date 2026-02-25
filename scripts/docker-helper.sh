#!/bin/bash
# Helper script para ejecutar docker compose desde la raíz del proyecto
# 
# Uso:
#   ./scripts/docker-helper.sh up -d                    # Modo producción
#   ./scripts/docker-helper.sh up -d --build            # Forzar rebuild
#   ./scripts/docker-helper.sh dev up -d                # Modo desarrollo (frontend hot-reload)
#   ./scripts/docker-helper.sh dev up -d --build        # Dev con rebuild
#   ./scripts/docker-helper.sh down                     # Detener contenedores
#   ./scripts/docker-helper.sh logs -f frontend         # Ver logs

COMPOSE_FILE="infrastructure/docker/docker-compose.yml"
COMPOSE_DEV_FILE="infrastructure/docker/docker-compose.dev.yml"

if [ ! -f "$COMPOSE_FILE" ]; then
    echo "Error: No se encontró el archivo docker-compose.yml en $COMPOSE_FILE"
    exit 1
fi

# Check if first argument is "dev"
if [ "$1" = "dev" ]; then
    if [ ! -f "$COMPOSE_DEV_FILE" ]; then
        echo "Error: No se encontró el archivo docker-compose.dev.yml en $COMPOSE_DEV_FILE"
        exit 1
    fi
    
    echo "🚀 Modo DESARROLLO activado (frontend hot-reload habilitado)"
    
    # Remove "dev" from args and pass the rest
    shift
    docker compose -f "$COMPOSE_FILE" -f "$COMPOSE_DEV_FILE" "$@"
else
    echo "🐳 Modo PRODUCCIÓN"
    docker compose -f "$COMPOSE_FILE" "$@"
fi

