# Catálogo de endpoints — Tienda Insegura v1

Base URL directa:

```text
http://127.0.0.1:8080
```

Base URL por Nginx:

```text
http://127.0.0.1:3000
```

| Método | Ruta | Autenticación | Descripción | Vulnerabilidad asociada |
|---|---|---|---|---|
| GET | `/api/productos` | no | listar productos | — |
| GET | `/api/productos/buscar?q=` | no | buscar por nombre | SQL Injection |
| GET | `/api/productos/{id}` | no | detalle de producto | SQL Injection |
| POST | `/api/productos/{id}/imagen` | no | subir archivo | upload inseguro |
| POST | `/api/auth/registro` | no | crear usuario | rol manipulable |
| POST | `/api/auth/login` | no | iniciar sesión | sin rate limit |
| POST | `/api/carrito/{cartId}/agregar` | no | agregar al carrito | diseño inseguro |
| GET | `/api/carrito/{cartId}` | no | ver carrito | cartId predecible |
| GET | `/api/carrito/{cartId}/total` | no | calcular total | cantidades negativas |
| POST | `/api/pedidos/checkout/{cartId}` | JWT | crear pedido | reglas de negocio débiles |
| GET | `/api/pedidos/mis-pedidos` | JWT | pedidos del usuario | — |
| GET | `/api/pedidos/{id}` | JWT | detalle de pedido | IDOR |
| GET | `/api/admin/usuarios` | cualquier JWT | listar usuarios | falta de control por rol |
| GET | `/api/admin/pedidos` | cualquier JWT | listar pedidos | falta de control por rol |
| GET | `/api/admin/reportes/ping?host=` | no | diagnóstico | command injection |
| GET | `/actuator/*` | no | administración Spring | configuración insegura |
| GET | `/uploads/{nombre}` | no | archivo subido | publicación insegura |

## Formato de respuesta

La mayoría de endpoints usa:

```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

Los errores no controlados se devuelven con stacktrace completo en la V1.

