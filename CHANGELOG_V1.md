# Changelog — Versión 1 mejorada

Fecha: 2026-08-01

## Correcciones

- Se completaron `database/init.sql` y `database/backup-v1.sql`.
- Se agregó restricción única para evitar duplicación de productos.
- Se hicieron idempotentes los datos semilla.
- Se agregaron pedidos semilla para demostrar IDOR.
- Se corrigió el payload UNION para seis columnas y tipos PostgreSQL.
- Se eliminó la referencia incorrecta a Jackson vulnerable.
- Se aclaró que Log4j 2.14.1 se usa para análisis de dependencias.
- Se corrigió Nginx para usar `backend:8080`.
- Se eliminó la dependencia de `host.docker.internal`.
- Se cambió el frontend para usar `/api` detrás de Nginx.
- Se limitaron los puertos Docker a `127.0.0.1`.
- Se agregó red Docker dedicada e interna.
- Se eliminaron `target/`, Maven Wrapper vacío y assets duplicados vacíos.
- Se completaron `.gitignore`, `.env.example` y `frontend/README.md`.
- Se agregó una prueba unitaria mínima.

## Documentación agregada

- arquitectura;
- despliegue;
- mapeo de vulnerabilidades;
- pruebas de penetración;
- comandos PowerShell;
- checklist de entrega;
- guía y scripts de sqlmap;
- estructura de evidencias;
- scripts de verificación del laboratorio.

## Alcance

La aplicación continúa siendo intencionalmente vulnerable. Las mejoras de esta
revisión son de reproducibilidad, documentación, coherencia y contención del
laboratorio; no corresponden todavía a la Versión 2 sanitizada.
