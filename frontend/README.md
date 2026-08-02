# Frontend — Tienda Insegura v1

Frontend estático en HTML, CSS y JavaScript sin frameworks.

## Opción recomendada: Docker Compose

Desde la raíz del proyecto:

```bash
docker compose --profile full up -d --build
```

Abrir:

- `http://127.0.0.1:3000`

Nginx sirve los archivos y reenvía `/api/*` al servicio `backend`.

## Opción de desarrollo: Live Server

1. Levantar PostgreSQL y el backend.
2. Abrir esta carpeta con VS Code.
3. Ejecutar **Open with Live Server** sobre `index.html`.
4. Abrir normalmente `http://127.0.0.1:5500`.

En ese modo, `js/config.js` utiliza `http://127.0.0.1:8080/api`.

## Advertencia

La aplicación es deliberadamente vulnerable y solo debe usarse en un
laboratorio local autorizado. No publicar este frontend ni su backend en
Internet.
