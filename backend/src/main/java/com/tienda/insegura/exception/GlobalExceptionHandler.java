package com.tienda.insegura.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * VULNERABILIDAD A10:2025 - Mishandling of Exceptional Conditions
 *
 * Este manejador global "atrapa todo" pero en lugar de devolver un
 * mensaje generico, expone:
 *  - La clase exacta de la excepcion.
 *  - El mensaje interno (que puede incluir fragmentos de SQL, rutas de
 *    archivos del servidor, nombres de tablas/columnas, etc.).
 *  - El stacktrace COMPLETO en la respuesta JSON.
 *
 * Esto le regala a un atacante informacion valiosisima para afinar
 * otros ataques (SQLi, path traversal, version de librerias, etc.)
 * simplemente forzando errores (ej: mandando un "id" no numerico a un
 * endpoint que espera Long).
 *
 * En la v2: respuestas de error genericas y estables de cara al
 * cliente + logging detallado SOLO server-side (sin credenciales,
 * ver A09) correlacionado con un id de traza.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarCualquierExcepcion(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));

        Map<String, Object> body = Map.of(
                "success", false,
                "exception", ex.getClass().getName(),
                "message", String.valueOf(ex.getMessage()),
                "stacktrace", sw.toString()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
