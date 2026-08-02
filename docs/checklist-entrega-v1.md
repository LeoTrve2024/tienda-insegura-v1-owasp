# Checklist de entrega — Versión 1

## Código y ejecución

- [x] Backend Spring Boot incluido.
- [x] Frontend HTML/CSS/JS incluido.
- [x] PostgreSQL incluido.
- [x] Dockerfile y Docker Compose incluidos.
- [x] Nginx funciona como reverse proxy por nombre de servicio Docker.
- [x] Puertos enlazados por defecto a `127.0.0.1`.
- [x] Scripts SQL idempotentes.
- [x] Backup lógico no vacío.
- [x] Datos semilla para usuarios, productos y pedidos.

## Vulnerabilidades

- [x] SQL Injection.
- [x] Command Injection / RCE controlado.
- [x] IDOR.
- [x] Escalamiento de rol.
- [x] Contraseñas en texto plano.
- [x] JWT débil.
- [x] CORS abierto.
- [x] Actuator expuesto.
- [x] Upload inseguro.
- [x] Carrito sin reglas de negocio.
- [x] Logging inseguro.
- [x] Stacktraces expuestos.
- [x] Dependencia vulnerable para análisis SCA.

## Documentación

- [x] README principal.
- [x] Arquitectura de despliegue.
- [x] Guía para levantar el entorno.
- [x] Mapeo de vulnerabilidades.
- [x] Guía de pentesting.
- [x] Guía específica de sqlmap.
- [x] Scripts sqlmap para Bash y PowerShell.
- [x] Informe técnico Word actualizado.
- [x] Plantilla de evidencias.

## Evidencias que el estudiante debe generar localmente

- [ ] Captura de `docker compose ps`.
- [ ] Captura del frontend.
- [ ] Captura de sqlmap detectando el parámetro `q`.
- [ ] Captura del dump de `usuarios`.
- [ ] Captura de IDOR.
- [ ] Captura de command injection con comando inocuo.
- [ ] Captura de Actuator.
- [ ] Captura de Dependency-Check.
- [ ] Captura de upload inseguro.
- [ ] Captura de stacktrace.

## Cierre de la V1

Antes de comenzar la V2:

1. Ejecutar el laboratorio en el equipo del estudiante.
2. Completar las capturas.
3. Guardar la salida de sqlmap.
4. Congelar esta carpeta como `version1`.
5. Crear la V2 en una carpeta o rama separada.

