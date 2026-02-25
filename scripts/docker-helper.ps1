# Helper script para ejecutar docker compose desde la raíz del proyecto
# 
# Uso:
#   .\scripts\docker-helper.ps1 up -d                    # Modo producción
#   .\scripts\docker-helper.ps1 up -d --build            # Forzar rebuild
#   .\scripts\docker-helper.ps1 dev up -d                # Modo desarrollo (frontend hot-reload)
#   .\scripts\docker-helper.ps1 dev up -d --build        # Dev con rebuild
#   .\scripts\docker-helper.ps1 down                     # Detener contenedores
#   .\scripts\docker-helper.ps1 logs -f frontend         # Ver logs

$composeFile = "infrastructure/docker/docker-compose.yml"
$composeDevFile = "infrastructure/docker/docker-compose.dev.yml"

if (-not (Test-Path $composeFile)) {
    Write-Error "No se encontró el archivo docker-compose.yml en $composeFile"
    exit 1
}

# Check if first argument is "dev"
if ($args[0] -eq "dev") {
    if (-not (Test-Path $composeDevFile)) {
        Write-Error "No se encontró el archivo docker-compose.dev.yml en $composeDevFile"
        exit 1
    }
    
    Write-Host "🚀 Modo DESARROLLO activado (frontend hot-reload habilitado)" -ForegroundColor Green
    
    # Remove "dev" from args and pass the rest
    $remainingArgs = $args[1..($args.Length - 1)]
    docker compose -f $composeFile -f $composeDevFile $remainingArgs
} else {
    Write-Host "🐳 Modo PRODUCCIÓN" -ForegroundColor Cyan
    docker compose -f $composeFile $args
}

