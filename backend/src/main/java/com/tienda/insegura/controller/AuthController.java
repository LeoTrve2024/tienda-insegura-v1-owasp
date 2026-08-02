package com.tienda.insegura.controller;

import com.tienda.insegura.dto.ApiResponse;
import com.tienda.insegura.dto.LoginRequest;
import com.tienda.insegura.dto.RegistroRequest;
import com.tienda.insegura.model.User;
import com.tienda.insegura.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // A01/A06: endpoint publico de registro que acepta "role" desde el
    // cliente (ver RegistroRequest / AuthService.registrar).
    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registro(@RequestBody RegistroRequest req) {
        User user = authService.registrar(req);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole()
        )));
    }

    // A07: sin rate-limit, sin bloqueo de cuenta, password en texto plano.
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody LoginRequest req) {
        String token = authService.login(req);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Usuario o password incorrectos"));
        }
        return ResponseEntity.ok(ApiResponse.ok(Map.of("token", token)));
    }
}
