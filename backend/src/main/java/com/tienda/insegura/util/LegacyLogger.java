package com.tienda.insegura.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VULNERABILIDAD A09:2025 - Security Logging and Alerting Failures
 *
 * Este "logger" es un ejemplo real de mal manejo de logs:
 *  - Registra credenciales (usuario + password) EN TEXTO PLANO.
 *  - No distingue niveles de severidad para eventos de seguridad
 *    (login fallido, acceso admin, etc.) -> todo va como INFO.
 *  - No dispara ninguna alerta ni integra con un SIEM.
 *  - No hay rate-limiting de intentos, por lo que tampoco se detectan
 *    patrones de fuerza bruta en estos logs.
 *
 * En la version 2 esto se reemplaza por logging estructurado que
 * enmascara datos sensibles y por un mecanismo real de alertas.
 */
public class LegacyLogger {

    private static final Logger log = LoggerFactory.getLogger("AUDITORIA");

    public static void loginIntento(String username, String password) {
        // NUNCA hacer esto en produccion: credenciales en el log.
        log.info("Intento de login -> usuario='{}' password='{}'", username, password);
    }

    public static void loginExitoso(String username, String role) {
        log.info("Login exitoso -> usuario='{}' role='{}'", username, role);
    }

    public static void accesoAdmin(String username, String recurso) {
        log.info("Acceso a recurso admin -> usuario='{}' recurso='{}'", username, recurso);
    }

    public static void comandoEjecutado(String comando) {
        // Se loguea el comando pero no se audita ni se restringe nada
        // con base en esta informacion.
        log.info("Comando de sistema ejecutado: {}", comando);
    }
}
