package com.tienda.insegura.dto;

public class RegistroRequest {

    private String username;
    private String email;
    private String password;
    private String fullName;

    // A01: Broken Access Control / A06: Insecure Design.
    // Este campo NO deberia existir en un DTO de auto-registro publico:
    // si el cliente lo envia como "ADMIN", AuthService lo acepta tal cual
    // (ver AuthService.registrar) permitiendo escalamiento de privilegios
    // en el registro.
    private String role;

    public RegistroRequest() {
    }

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
}
