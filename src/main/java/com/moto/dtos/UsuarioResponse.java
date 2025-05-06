package com.moto.dtos;

public class UsuarioResponse {
    private Long id;
    private String username;
    private String rol;

    public UsuarioResponse(Long id, String username, String rol) {
        this.id = id;
        this.username = username;
        this.rol = rol;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getRol() { return rol; }
}
