package com.tienda.insegura;

import com.tienda.insegura.dto.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba mínima que no requiere conexión a PostgreSQL.
 * La validación funcional del laboratorio se realiza con Docker Compose.
 */
class TiendaInseguraApplicationTests {

    @Test
    void apiResponseOkMantieneLaEstructuraEsperada() {
        ApiResponse<String> response = ApiResponse.ok("laboratorio");

        assertTrue(response.isSuccess());
        assertEquals("OK", response.getMessage());
        assertEquals("laboratorio", response.getData());
    }
}
