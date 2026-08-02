package com.tienda.insegura.dto;

/**
 * Sin @NotBlank / @Size a proposito: el endpoint de login (AuthController)
 * no valida el body de entrada (apoya A05/A07: se puede mandar cualquier
 * cosa, incluyendo payloads de SQLi, sin que nada lo frene antes de llegar
 * a la capa de datos).
 */
public class LoginRequest {

    private String username;
    private String password;

    public LoginRequest() {
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
