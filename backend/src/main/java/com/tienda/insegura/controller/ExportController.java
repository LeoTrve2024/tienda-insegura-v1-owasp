package com.tienda.insegura.controller;

import com.tienda.insegura.dto.ApiResponse;
import com.tienda.insegura.service.ReportService;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint de "diagnostico" pensado para que un admin verifique
 * conectividad hacia un proveedor externo antes de generar un reporte.
 *
 * VULNERABILIDAD A05:2025 - OS Command Injection (ver ReportService
 * para el detalle completo del vector y payloads de ejemplo).
 *
 * Nota: a proposito este endpoint NO valida rol de administrador
 * (encadena tambien con A01, igual que AdminController).
 */
@RestController
@RequestMapping("/api/admin/reportes")
public class ExportController {

    private final ReportService reportService;

    public ExportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/ping")
    public ApiResponse<String> ping(@RequestParam String host) {
        String resultado = reportService.pingHost(host);
        return ApiResponse.ok(resultado);
    }
}
