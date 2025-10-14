package com.uade.ecommerce.entity.dto;


@lombok.Data
public class categoriaResponse {
    private int id;
    private String nombre;
   

    public categoriaResponse(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

}