package com.tienda.insegura.controller;

import com.tienda.insegura.dto.ApiResponse;
import com.tienda.insegura.model.Product;
import com.tienda.insegura.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(productService.listarTodos()));
    }

    /**
     * VULNERABLE A PROPOSITO (A05 - SQL Injection).
     * "q" viaja intacto hasta ProductRepositoryJdbc.buscarPorNombre(),
     * que concatena el string en el SQL. Este es el endpoint objetivo
     * de la prueba automatizada con sqlmap (ver /pentesting/sqlmap).
     *
     * Ejemplo manual:
     *   GET /api/productos/buscar?q=x%') OR 1=1 -- 
     *   GET /api/productos/buscar?q=x%') UNION SELECT id,username,password,0::numeric,0,NULL FROM usuarios -- 
     */
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<Product>>> buscar(@RequestParam("q") String q) {
        return ResponseEntity.ok(ApiResponse.ok(productService.buscar(q)));
    }

    // A05: tambien vulnerable (ver ProductRepositoryJdbc.buscarPorId),
    // el "id" se concatena sin castear a Long ni parametrizar.
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> obtener(@PathVariable String id) {
        Product producto = productService.obtenerPorId(id);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.ok(producto));
    }
}
