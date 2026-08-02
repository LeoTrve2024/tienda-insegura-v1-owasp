package com.tienda.insegura.service;

import com.tienda.insegura.util.LegacyLogger;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * VULNERABILIDAD A05:2025 - Injection (OS Command Injection)
 *
 * ExportController expone un endpoint de diagnostico "para el admin"
 * (GET /api/admin/reportes/ping?host=...) pensado para verificar
 * conectividad hacia un proveedor externo antes de generar un reporte.
 *
 * El problema: el host que escribe el usuario se concatena DIRECTO
 * dentro de un comando de shell ejecutado con Runtime.exec(), sin
 * ninguna validacion de formato ni lista blanca de caracteres.
 *
 * Vector de explotacion real (encadenamiento de comandos):
 *   En Linux/Docker (/bin/sh):
 *     host = "127.0.0.1; cat /etc/passwd"
 *     host = "127.0.0.1 && whoami"
 *     host = "$(id)"
 *   En Windows (cmd.exe -- ';' NO es separador de comandos en cmd,
 *   usar '&' o '&&'):
 *     host = "127.0.0.1 & whoami"
 *     host = "127.0.0.1 && dir"
 *
 * Esto da Remote Code Execution (RCE) con los privilegios del proceso
 * de la aplicacion Spring Boot.
 *
 * En la v2 esto se reemplaza por una libreria de red nativa de Java
 * (InetAddress.isReachable / un cliente HTTP) sin invocar al shell,
 * y si en verdad se necesitara invocar un binario externo, se haria
 * con ProcessBuilder + lista de argumentos (sin shell) + lista blanca
 * estricta de valores permitidos.
 */
@Service
public class ReportService {

    public String pingHost(String host) {
        // Deteccion de SO SOLO para elegir el shell/flag de "ping" correcto
        // (no es una mitigacion: el host sigue concatenandose sin
        // sanear en ambos casos, por lo que la inyeccion de comandos
        // funciona igual en Windows y en Linux/Docker).
        boolean esWindows = System.getProperty("os.name", "")
                .toLowerCase().contains("windows");

        // VULNERABLE A PROPOSITO: concatenacion directa del input del
        // usuario dentro de un comando ejecutado por el shell del SO.
        String comando = (esWindows ? "ping -n 1 " : "ping -c 1 ") + host;
        LegacyLogger.comandoEjecutado(comando);

        try {
            ProcessBuilder pb = esWindows
                    ? new ProcessBuilder("cmd.exe", "/c", comando)
                    : new ProcessBuilder("/bin/sh", "-c", comando);
            pb.redirectErrorStream(true);
            Process proceso = pb.start();

            StringBuilder salida = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proceso.getInputStream(), StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    salida.append(linea).append("\n");
                }
            }

            proceso.waitFor();
            return salida.toString();

        } catch (IOException | InterruptedException e) {
            // A10: se devuelve el detalle crudo de la excepcion al llamador
            // (ver ExportController / GlobalExceptionHandler).
            return "Error ejecutando comando: " + e.getMessage();
        }
    }
}