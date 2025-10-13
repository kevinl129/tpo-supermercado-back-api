package com.uade.ecommerce.entity.dto;

import java.util.List;

@lombok.Data
public class categoriaResponse {
    private int id;
    private String nombre;
    private Integer parentId;
    private String parentNombre;
    private List<categoriaResponse> subcategorias;

    public categoriaResponse(int id, String nombre, Integer parentId, String parentNombre,
            List<categoriaResponse> subcategorias) {
        this.id = id;
        this.nombre = nombre;
        this.parentId = parentId;
        this.parentNombre = parentNombre;
        this.subcategorias = subcategorias;
    }

}