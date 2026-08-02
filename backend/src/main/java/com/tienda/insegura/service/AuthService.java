package com.tienda.insegura.service;

import com.tienda.insegura.dto.LoginRequest;
import com.tienda.insegura.dto.RegistroRequest;
import com.tienda.insegura.model.User;
import com.tienda.insegura.repository.UserRepository;
import com.tienda.insegura.util.LegacyLogger;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

/**
 * VULNERABILIDAD A07:2025 - Identification and Authentication Failures
 *
 * Problemas a proposito en esta clase:
 *  1. El password se compara en TEXTO PLANO (sin BCrypt/Argon2) -> ligado
 *     tambien a A04 Cryptographic Failures.
 *  2. No hay control de intentos fallidos ni bloqueo de cuenta
 *     (fuerza bruta / credential stuffing sin mitigacion).
 *  3. El JWT se firma con un secreto CORTO y HARDCODEADO en el codigo
 *     (deberia venir de un vault / variable de entorno gestionada).
 *  4. El token NO expira (fecha de expiracion muy lejana) y no se
 *     invalida en logout (no hay lista de revocacion).
 *  5. El registro publico permite fijar el "role" que llega en el body
 *     (ver RegistroRequest) -> escalamiento de privilegios (A01).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;

    // A07/A04: secreto debil y hardcodeado. En la v2 esto vendra de una
    // variable de entorno / secret manager y sera de al menos 256 bits.
    @Value("${app.jwt.secret:clave-super-secreta-insegura-2025}")
    private String jwtSecret;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private Key getSigningKey() {
        // Se rellena el string para que jjwt no reviente por longitud,
        // pero conceptualmente el secreto sigue siendo debil y predecible.
        String padded = (jwtSecret + jwtSecret + jwtSecret + jwtSecret).substring(0, 32);
        return Keys.hmacShaKeyFor(padded.getBytes());
    }

    public User registrar(RegistroRequest req) {
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        // A04: se guarda tal cual, sin hash.
        user.setPassword(req.getPassword());
        user.setFullName(req.getFullName());

        // A01: si el cliente manda "role":"ADMIN" en el JSON, se respeta.
        if (req.getRole() != null && !req.getRole().isBlank()) {
            user.setRole(req.getRole());
        } else {
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    /**
     * Login vulnerable: compara password en texto plano y no limita
     * intentos. Devuelve el JWT como String, o null si falla.
     */
    public String login(LoginRequest req) {
        LegacyLogger.loginIntento(req.getUsername(), req.getPassword());

        Optional<User> maybeUser = userRepository.findByUsername(req.getUsername());
        if (maybeUser.isEmpty()) {
            return null;
        }

        User user = maybeUser.get();

        // A04/A07: comparacion en texto plano, sin constant-time compare.
        if (!user.getPassword().equals(req.getPassword())) {
            return null;
        }

        LegacyLogger.loginExitoso(user.getUsername(), user.getRole());
        return generarToken(user);
    }

    private String generarToken(User user) {
        Date ahora = new Date();
        // A07: expiracion absurdamente larga (10 anios) -> tokens robados
        // siguen siendo validos "para siempre" en la practica.
        Date expiracion = new Date(ahora.getTime() + 10L * 365 * 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .claim("userId", user.getId())
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Usado por el filtro de autenticacion (ver config/SecurityConfig). */
    public io.jsonwebtoken.Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
