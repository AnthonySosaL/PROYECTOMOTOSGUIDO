package com.moto.dtos;

public class LoginRequest {
    private String username;
    private String clave;

    // Getters y setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}
