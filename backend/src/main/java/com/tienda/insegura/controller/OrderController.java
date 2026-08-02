package com.tienda.insegura.controller;

import com.tienda.insegura.dto.ApiResponse;
import com.tienda.insegura.model.Order;
import com.tienda.insegura.model.User;
import com.tienda.insegura.repository.UserRepository;
import com.tienda.insegura.service.AuthService;
import com.tienda.insegura.service.OrderService;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, AuthService authService, UserRepository userRepository) {
        this.orderService = orderService;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    /**
     * Extrae el usuario autenticado a partir del header Authorization.
     * No lanza error si el token es invalido/ausente: simplemente
     * devuelve null y los endpoints de abajo siguen ejecutandose sin
     * usuario (ver A01 en cada metodo).
     */
    private User usuarioActual(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            Claims claims = authService.parseToken(authHeader.substring(7));
            Long userId = ((Number) claims.get("userId")).longValue();
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/checkout/{cartId}")
    public ResponseEntity<ApiResponse<Order>> checkout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String cartId) {

        User usuario = usuarioActual(authHeader);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("No autenticado"));
        }

        Order pedido = orderService.crearPedidoDesdeCarrito(usuario, cartId);
        return ResponseEntity.ok(ApiResponse.ok(pedido));
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<ApiResponse<List<Order>>> misPedidos(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User usuario = usuarioActual(authHeader);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("No autenticado"));
        }
        return ResponseEntity.ok(ApiResponse.ok(orderService.listarPorUsuario(usuario.getId())));
    }

    /**
     * VULNERABILIDAD A01:2025 - Broken Access Control (IDOR)
     *
     * Solo verifica que exista ALGUN token valido (usuario autenticado
     * con CUALQUIER cuenta), pero jamas compara pedido.getUsuario().getId()
     * contra el id del usuario autenticado. Cualquier cliente logueado
     * puede leer pedidos ajenos iterando el id:
     *
     *   GET /api/pedidos/1
     *   GET /api/pedidos/2
     *   GET /api/pedidos/3  ...
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> obtener(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User usuario = usuarioActual(authHeader);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("No autenticado"));
        }

        // FALTA: if (!pedido.getUsuario().getId().equals(usuario.getId())) -> 403
        Optional<Order> pedido = orderService.obtenerPorIdSinValidarDueno(id);
        return pedido.map(o -> ResponseEntity.ok(ApiResponse.ok(o)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
