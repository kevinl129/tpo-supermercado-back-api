package com.uade.ecommerce.controller.dto;

public class LoginJwtResponse {
    private String token;
    private UsuarioLoginResponse usuario;

    public LoginJwtResponse(String token, UsuarioLoginResponse usuario) {
        this.token = token;
        this.usuario = usuario;
    }

    public String getToken() { return token; }
    public UsuarioLoginResponse getUsuario() { return usuario; }
}
