package com.tienda.insegura.controller;

import com.tienda.insegura.dto.ApiResponse;
import com.tienda.insegura.service.CartService;
import com.tienda.insegura.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/carrito")
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    public CartController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    public static class AgregarRequest {
        public Long productoId;
        public Integer cantidad;
    }

    // A06: no valida cantidad > 0 ni stock disponible (ver CartService).
    // "cartId" se recibe como parametro simple, no se deriva de una
    // sesion autenticada verificada -> cualquiera puede leer/mutar el
    // carrito de otro si adivina/reusa su cartId.
    @PostMapping("/{cartId}/agregar")
    public ResponseEntity<ApiResponse<String>> agregar(@PathVariable String cartId, @RequestBody AgregarRequest req) {
        cartService.agregar(cartId, req.productoId, req.cantidad);
        return ResponseEntity.ok(ApiResponse.ok("Producto agregado", null));
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<ApiResponse<Map<Long, Integer>>> ver(@PathVariable String cartId) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.obtenerCarrito(cartId)));
    }

    @GetMapping("/{cartId}/total")
    public ResponseEntity<ApiResponse<BigDecimal>> total(@PathVariable String cartId) {
        BigDecimal total = cartService.calcularTotal(cartId, productService.listarTodos());
        return ResponseEntity.ok(ApiResponse.ok(total));
    }
}
