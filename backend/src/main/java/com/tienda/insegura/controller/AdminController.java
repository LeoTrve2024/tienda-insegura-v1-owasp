package com.tienda.insegura.controller;

import com.tienda.insegura.dto.ApiResponse;
import com.tienda.insegura.model.Order;
import com.tienda.insegura.model.User;
import com.tienda.insegura.repository.UserRepository;
import com.tienda.insegura.service.AuthService;
import com.tienda.insegura.service.OrderService;
import com.tienda.insegura.util.LegacyLogger;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VULNERABILIDAD A01:2025 - Broken Access Control
 *
 * Estos endpoints "de administracion" solo verifican que el JWT sea
 * VALIDO (es decir, que el usuario este logueado con CUALQUIER cuenta),
 * pero nunca revisan claims.get("role").equals("ADMIN").
 *
 * Combinado con que el registro publico permite mandar "role":"ADMIN"
 * (ver AuthController/RegistroRequest), un atacante puede:
 *   1. Registrarse con role=ADMIN.
 *   2. Loguearse normalmente.
 *   3. Usar el token resultante contra estos endpoints sin ningun
 *      chequeo adicional -> acceso total al panel admin.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final OrderService orderService;
    private final AuthService authService;

    public AdminController(UserRepository userRepository, OrderService orderService, AuthService authService) {
        this.userRepository = userRepository;
        this.orderService = orderService;
        this.authService = authService;
    }

    /** Solo valida "hay token" -- NO valida rol. */
    private boolean estaAutenticado(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        try {
            Claims claims = authService.parseToken(authHeader.substring(7));
            LegacyLogger.accesoAdmin((String) claims.getSubject(), "panel-admin");
            return true;
            // FALTA: return "ADMIN".equals(claims.get("role"));
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/usuarios")
    public ResponseEntity<ApiResponse<List<User>>> listarUsuarios(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!estaAutenticado(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("No autenticado"));
        }
        // A04: esto ademas expone el campo "password" en texto plano
        // porque User no tiene @JsonIgnore sobre ese getter.
        return ResponseEntity.ok(ApiResponse.ok(userRepository.findAll()));
    }

    @GetMapping("/pedidos")
    public ResponseEntity<ApiResponse<List<Order>>> listarPedidos(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!estaAutenticado(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("No autenticado"));
        }
        return ResponseEntity.ok(ApiResponse.ok(orderService.listarTodos()));
    }
}
