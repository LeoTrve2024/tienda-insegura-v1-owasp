package com.tienda.insegura.controller;

import com.tienda.insegura.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * VULNERABILIDAD A08:2025 - Software or Data Integrity Failures
 * (Unrestricted File Upload)
 *
 * Problemas a proposito:
 *  - No se valida el tipo MIME real del archivo (solo se confia en lo
 *    que el cliente declara, que es trivial de falsear).
 *  - No se valida la extension contra una lista blanca (se podria subir
 *    un .jsp, .php, .sh, etc.).
 *  - El nombre de archivo original se usa TAL CUAL para guardarlo en
 *    disco -> Path Traversal si contiene "../../".
 *  - El archivo se guarda dentro de una carpeta servida como estatica,
 *    lo que en un servidor mal configurado podria derivar en ejecucion
 *    remota si el tipo de archivo lo permite.
 *
 * En la v2: validacion de magic bytes, whitelist de extensiones,
 * renombrado con UUID, y almacenamiento fuera del webroot (o en un
 * bucket S3-like con permisos restringidos).
 */
@RestController
@RequestMapping("/api/productos")
public class UploadController {

    private static final String CARPETA_UPLOADS = "uploads";

    @PostMapping("/{id}/imagen")
    public ResponseEntity<ApiResponse<String>> subirImagen(@PathVariable Long id,
                                                             @RequestParam("archivo") MultipartFile archivo) {
        try {
            File carpeta = new File(CARPETA_UPLOADS);
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            // VULNERABLE A PROPOSITO: se usa el nombre original sin
            // sanear (path traversal) y sin validar extension/tipo real.
            String nombreOriginal = archivo.getOriginalFilename();
            Path destino = Path.of(CARPETA_UPLOADS, nombreOriginal);

            archivo.transferTo(destino);

            return ResponseEntity.ok(ApiResponse.ok("Archivo disponible", "/uploads/" + nombreOriginal));

        } catch (IOException e) {
            // A10: se expone el mensaje crudo de la excepcion.
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }
}
