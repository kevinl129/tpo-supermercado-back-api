package com.uade.ecommerce.service.controller.dto;

public class UsuarioLoginResponse {
    private int id;
    private String username;
    private String email;
    private String nombre;
    private String apellido;
    private String rol;

    public UsuarioLoginResponse(int id, String username, String email, String nombre, String apellido, String rol) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rol = rol;
    }

    // getters y setters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getRol() { return rol; }
}
