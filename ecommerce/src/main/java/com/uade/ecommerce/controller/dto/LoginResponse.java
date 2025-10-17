package com.uade.ecommerce.controller.dto;

import com.uade.ecommerce.entity.Usuario;

// Esta clase es la "caja" que contiene todo lo que el frontend necesita al hacer login.
// Genera un JSON así: { "token": "...", "usuario": { ... } }
public class LoginResponse {
    private String token;
    private UsuarioLoginResponse usuario; // ✅ Reutilizamos tu DTO existente

    public LoginResponse(String token, Usuario usuario) {
        this.token = token;
        // Creamos una instancia de tu DTO con los datos del usuario de la base de datos
        this.usuario = new UsuarioLoginResponse(
            usuario.getId(),
            usuario.getUsername(),
            usuario.getEmail(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getRol()
        );
    }

    // Getters para que Spring pueda convertir esto a JSON
    public String getToken() { return token; }
    public UsuarioLoginResponse getUsuario() { return usuario; }
}