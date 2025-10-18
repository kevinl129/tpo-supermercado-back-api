package com.uade.ecommerce.entity.dto;

import lombok.Data;

@Data
public class CategoryRequest {
    private String nombre; 
    private Integer parentId; 

}
