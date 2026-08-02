$ErrorActionPreference = "Stop"

$BaseUrl = "http://127.0.0.1:8080"
$FrontUrl = "http://127.0.0.1:3000"

Write-Host "[1/5] Docker Compose"
docker compose ps

Write-Host "[2/5] Backend health"
Invoke-RestMethod "$BaseUrl/actuator/health" | ConvertTo-Json -Depth 4

Write-Host "[3/5] Productos"
$productos = Invoke-RestMethod "$BaseUrl/api/productos"
Write-Host "Productos encontrados: $($productos.data.Count)"

Write-Host "[4/5] Frontend"
$response = Invoke-WebRequest $FrontUrl -Method Head
Write-Host "HTTP $($response.StatusCode)"

Write-Host "[5/5] Base de datos"
$usuarios = docker exec tienda_v1_postgres `
  psql -U tienda_user -d tienda_insegura `
  -tAc "SELECT COUNT(*) FROM usuarios;"
Write-Host "Usuarios: $usuarios"

Write-Host "[OK] Laboratorio disponible en localhost." -ForegroundColor Green
