# Guía de despliegue local — Tienda Insegura v1

## 0. Advertencia

Este proyecto es deliberadamente vulnerable. Debe ejecutarse únicamente en:

- `localhost`;
- una VM aislada;
- una red de laboratorio autorizada.

No debe publicarse en Internet ni enlazarse a `0.0.0.0`.

## 1. Requisitos

### Opción recomendada

- Docker Desktop 4.x o Docker Engine reciente;
- Docker Compose v2;
- 2 GB de memoria libre;
- puertos 3000, 8080 y 5432 disponibles.

### Opción de desarrollo

- Java 17;
- Maven 3.8 o superior;
- Docker para PostgreSQL;
- Live Server o Python 3 para el frontend.

## 2. Verificación previa

```bash
docker --version
docker compose version
```

En Windows, Docker Desktop debe estar iniciado.

## 3. Levantar todo con Docker

Desde la carpeta raíz:

```bash
docker compose --profile full up -d --build
```

Verificar:

```bash
docker compose ps
docker compose logs postgres
docker compose logs backend
docker compose logs frontend
```

Los servicios deben quedar accesibles en:

- frontend: `http://127.0.0.1:3000`;
- backend: `http://127.0.0.1:8080`;
- PostgreSQL: `127.0.0.1:5432`.

## 4. Prueba de salud

Linux, macOS o WSL:

```bash
curl http://127.0.0.1:8080/actuator/health
curl http://127.0.0.1:8080/api/productos
curl -I http://127.0.0.1:3000
```

PowerShell:

```powershell
Invoke-RestMethod http://127.0.0.1:8080/actuator/health
Invoke-RestMethod http://127.0.0.1:8080/api/productos
Invoke-WebRequest http://127.0.0.1:3000 -Method Head
```

También se pueden ejecutar los scripts:

```bash
bash scripts/verificar-laboratorio.sh
```

```powershell
.\scripts\verificar-laboratorio.ps1
```

## 5. Ejecutar solo PostgreSQL en Docker

```bash
docker compose up -d postgres
```

Comprobar:

```bash
docker compose ps
```

## 6. Ejecutar backend con Maven

```bash
cd backend
mvn clean spring-boot:run
```

La API queda en `http://127.0.0.1:8080`.

El paquete no incluye archivos vacíos de Maven Wrapper. Para esta modalidad se
usa Maven instalado en el sistema.

## 7. Ejecutar frontend con Live Server

Abrir `frontend/index.html` con Live Server. El puerto habitual es 5500:

```text
http://127.0.0.1:5500
```

En ese modo, `frontend/js/config.js` consume
`http://127.0.0.1:8080/api`.

## 8. Variables configurables

Copiar `.env.example` como `.env`:

```bash
cp .env.example .env
```

En PowerShell:

```powershell
Copy-Item .env.example .env
```

Valores:

```dotenv
BIND_ADDRESS=127.0.0.1
FRONTEND_PORT=3000
BACKEND_PORT=8080
POSTGRES_PORT=5432
POSTGRES_DB=tienda_insegura
POSTGRES_USER=tienda_user
POSTGRES_PASSWORD=tienda_pass_123
```

No cambiar `BIND_ADDRESS` a `0.0.0.0`.

## 9. Detener el entorno

```bash
docker compose --profile full down
```

## 10. Reinicio limpio de la base de datos

```bash
docker compose --profile full down -v
docker compose --profile full up -d --build
```

Esto elimina el volumen y vuelve a ejecutar `schema.sql` y `data.sql`.

## 11. Restauración manual del backup

Con el contenedor de PostgreSQL activo:

```bash
docker exec -i tienda_v1_postgres \
  psql -U tienda_user -d tienda_insegura \
  < database/backup-v1.sql
```

PowerShell:

```powershell
Get-Content .\database\backup-v1.sql -Raw |
  docker exec -i tienda_v1_postgres `
  psql -U tienda_user -d tienda_insegura
```

## 12. Solución de problemas

### Puerto ocupado

Cambiar el puerto correspondiente en `.env`, por ejemplo:

```dotenv
BACKEND_PORT=8081
```

### La base conserva datos antiguos

```bash
docker compose --profile full down -v
```

### El frontend devuelve 502

Revisar el backend:

```bash
docker compose ps
docker compose logs backend
```

Nginx usa `http://backend:8080`, por lo que ambos servicios deben estar en la
red `tienda_insegura_v1_lab`.

### El build de Maven demora

La primera construcción descarga dependencias. Las siguientes usan caché.

### sqlmap no detecta el parámetro

1. comprobar que el endpoint responde;
2. ejecutar con `-p q`;
3. usar `--flush-session`;
4. probar `--level=3 --risk=2`;
5. revisar `pentesting/sqlmap/GUIA.md`.

