# Base de datos — Tienda Insegura v1

## Archivos

- `init.sql`: crea tablas, índices y datos semilla sin eliminar datos previos.
- `backup-v1.sql`: elimina y recrea las tablas del laboratorio.
- `../backend/src/main/resources/db/schema.sql`: esquema usado por la app.
- `../backend/src/main/resources/db/data.sql`: seed idempotente.

## Credenciales por defecto

```text
base: tienda_insegura
usuario: tienda_user
password: tienda_pass_123
host: 127.0.0.1
puerto: 5432
```

Son credenciales deliberadamente débiles para la V1.

## Restaurar backup con Docker

```bash
docker compose up -d postgres
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

## Reinicio más sencillo

```bash
docker compose --profile full down -v
docker compose --profile full up -d --build
```

