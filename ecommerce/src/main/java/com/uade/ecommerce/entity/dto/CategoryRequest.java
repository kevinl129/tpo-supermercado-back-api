package com.uade.ecommerce.entity.dto;

import lombok.Data;

@Data
public class CategoryRequest {
    private String nombre; // nombre de la categoria
    private Integer parentId; // puede ser null- Si es null es una Categoria padre

}
