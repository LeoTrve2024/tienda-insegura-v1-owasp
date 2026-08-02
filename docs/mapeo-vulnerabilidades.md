# Mapeo de vulnerabilidades — Tienda Insegura v1

## Alcance

Las pruebas se ejecutan exclusivamente contra `127.0.0.1` o `localhost`.

| ID | Categoría | Implementación | Archivo principal | Evidencia esperada | Corrección prevista en V2 |
|---|---|---|---|---|---|
| A01 | Broken Access Control | Registro acepta `role=ADMIN`; admin solo verifica token; IDOR en pedidos | `AuthService.java`, `AdminController.java`, `OrderController.java` | usuario común accede a datos administrativos o a pedido ajeno | Spring Security, roles y validación de propietario |
| A02 | Security Misconfiguration | Actuator `*`, CORS `*`, debug, credenciales débiles | `application.properties`, `WebConfig.java`, `ActuatorConfig.java` | `/actuator/env` y headers CORS visibles | configuración por perfiles, CORS whitelist, Actuator restringido |
| A03 | Supply Chain Failures | Log4j Core 2.14.1 incluido intencionalmente | `pom.xml` | Dependency-Check reporta CVE de la dependencia | actualizar/eliminar dependencia e integrar SCA |
| A04 | Cryptographic Failures | passwords en texto plano; secreto JWT débil | `User.java`, `AuthService.java`, `data.sql` | dump SQL o endpoint admin muestra passwords | BCrypt/Argon2 y secretos robustos |
| A05 | Injection | concatenación SQL y shell | `ProductRepositoryJdbc.java`, `ReportService.java` | SQLi devuelve credenciales; `whoami` se ejecuta | prepared statements; API de red sin shell |
| A06 | Insecure Design | cantidades negativas/altas; cartId no ligado a usuario | `CartService.java`, `OrderService.java` | total negativo o carrito ajeno accesible | reglas de negocio y carrito asociado a sesión |
| A07 | Authentication Failures | sin rate limit; token de 10 años; sin revocación | `AuthService.java`, `AuthController.java` | múltiples intentos sin 429/bloqueo | rate limit, bloqueo, expiración corta |
| A08 | Integrity Failures | upload sin validación de nombre, extensión o contenido | `UploadController.java` | archivo inocuo con extensión no permitida es aceptado | magic bytes, whitelist, UUID, fuera de webroot |
| A09 | Logging Failures | passwords y credenciales semilla en logs | `LegacyLogger.java`, `SecurityConfig.java` | logs muestran password | logging estructurado y enmascarado |
| A10 | Exceptional Conditions | stacktrace completo en respuesta | `GlobalExceptionHandler.java` | JSON con clase, mensaje y traza | error genérico + id de correlación |

## Detalle por vulnerabilidad

### A01 — Broken Access Control

**Superficies:**

- `POST /api/auth/registro`
- `GET /api/admin/usuarios`
- `GET /api/pedidos/{id}`

**Condición vulnerable:** el cliente controla el rol y la API administrativa
solo comprueba que exista un token válido. Además, el pedido se consulta por id
sin verificar su propietario.

### A02 — Security Misconfiguration

**Superficies:**

- `/actuator/env`
- `/actuator/beans`
- `/actuator/heapdump`
- CORS de `/api/**`

**Condición vulnerable:** configuración de desarrollo expuesta.

### A03 — Software Supply Chain Failures

`pom.xml` incorpora `log4j-core:2.14.1` para que una herramienta SCA lo detecte.

**Aclaración:** el logger activo del proyecto es Logback. La práctica de A03
consiste en detectar y gestionar la dependencia vulnerable, no en afirmar que
existe un flujo funcional de explotación Log4Shell.

### A04 — Cryptographic Failures

La columna `usuarios.password` almacena texto plano. El JWT se firma con un
secreto conocido y predecible.

### A05 — Injection

La consulta vulnerable posee seis columnas:

```sql
SELECT id, nombre, descripcion, precio, stock, imagen_url
FROM productos
WHERE LOWER(nombre) LIKE LOWER('%<entrada>%');
```

Por ello, un `UNION` compatible debe respetar tipos y cantidad de columnas:

```text
x%') UNION SELECT id,username,password,0::numeric,0,NULL FROM usuarios -- 
```

La inyección de comandos se demuestra únicamente con comandos inocuos como
`whoami`, `id` o `pwd`.

### A06 — Insecure Design

No se valida:

- cantidad mayor que cero;
- cantidad menor o igual al stock;
- límite de artículos;
- propiedad del `cartId`.

### A07 — Authentication Failures

No existe:

- rate limiting;
- bloqueo por intentos;
- MFA;
- revocación de tokens;
- expiración razonable.

### A08 — Integrity Failures

La subida usa `getOriginalFilename()` y confía en metadatos del cliente.

La evidencia debe usar archivos inocuos. No se requiere crear ni ejecutar una
webshell.

### A09 — Logging Failures

`LegacyLogger.loginIntento()` registra usuario y password. El backend también
imprime la credencial semilla al arrancar.

### A10 — Exceptional Conditions

`GlobalExceptionHandler` devuelve:

- clase de excepción;
- mensaje interno;
- stacktrace completo.

## Matriz de trazabilidad prueba–evidencia

| Prueba | Archivo de evidencia sugerido |
|---|---|
| Docker saludable | `evidencias/capturas/01-docker-ps.png` |
| SQLi manual | `evidencias/capturas/02-sqli-manual.png` |
| sqlmap detección | `evidencias/capturas/03-sqlmap-deteccion.png` |
| sqlmap dump | `evidencias/capturas/04-sqlmap-usuarios.png` |
| IDOR | `evidencias/capturas/05-idor.png` |
| Command injection | `evidencias/capturas/06-command-injection.png` |
| Actuator | `evidencias/capturas/07-actuator.png` |
| Dependency-Check | `evidencias/capturas/08-dependency-check.png` |
| Upload | `evidencias/capturas/09-upload.png` |
| Stacktrace | `evidencias/capturas/10-stacktrace.png` |

