package com.tienda.insegura.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Usuario.
 *
 * VULNERABILIDAD A04:2025 - Cryptographic Failures
 * El password se guarda y se compara en TEXTO PLANO (ver AuthService).
 * No hay hashing (BCrypt/Argon2), no hay salt, no hay politica de
 * expiracion ni de complejidad.
 */
@Entity
@Table(name = "usuarios")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    // A04: texto plano, sin hash.
    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    // A01: no es un enum estricto ni se valida contra una lista fija de
    // roles en todos los controllers -> permite escalamiento de privilegios
    // si el cliente logra manipular este campo en el registro.
    @Column(nullable = false)
    private String role = "USER";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {
    }

    public User(String username, String email, String password, String fullName, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
