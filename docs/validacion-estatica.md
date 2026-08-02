# Validación realizada sobre el paquete V1

Fecha: 2026-08-01

## Comprobaciones completadas

- Estructura del proyecto revisada.
- Enlaces del README verificados.
- `docker-compose.yml` analizado como YAML válido.
- Referencias CSS/JS de todas las páginas HTML verificadas.
- Los 13 archivos JavaScript pasaron `node --check`.
- Los scripts Bash pasaron `bash -n`.
- El documento Word fue abierto y guardado correctamente.
- No quedan archivos vacíos salvo `.gitkeep`.
- No se incluye `backend/target/`.
- No se incluyen Maven Wrapper vacíos.
- `database/init.sql` y `database/backup-v1.sql` contienen estructura y datos.

## Validación pendiente en el equipo del estudiante

El entorno usado para preparar el ZIP no dispone de Docker ni Maven con acceso
a Internet, por lo que deben ejecutarse localmente:

```bash
docker compose --profile full up -d --build
bash scripts/verificar-laboratorio.sh
```

o en PowerShell:

```powershell
docker compose --profile full up -d --build
.\scripts\verificar-laboratorio.ps1
```

Después se deben generar las evidencias de sqlmap y las capturas indicadas en
`docs/checklist-entrega-v1.md`.
