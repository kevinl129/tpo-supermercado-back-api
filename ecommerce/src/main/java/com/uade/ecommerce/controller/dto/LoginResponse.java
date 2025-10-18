package com.uade.ecommerce.controller.dto;

import com.uade.ecommerce.entity.Usuario;

public class LoginResponse {
    private String token;
    private UsuarioLoginResponse usuario; 

    public LoginResponse(String token, Usuario usuario) {
        this.token = token;
        this.usuario = new UsuarioLoginResponse(
            usuario.getId(),
            usuario.getUsername(),
            usuario.getEmail(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getRol()
        );
    }

    public String getToken() { return token; }
    public UsuarioLoginResponse getUsuario() { return usuario; }
}